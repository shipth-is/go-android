package com.shipthis.go.util

import android.content.Context
import com.google.gson.Gson
import com.shipthis.go.LogInterceptor
import com.shipthis.go.data.model.BuildRuntimeLogData
import com.shipthis.go.data.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.net.URISyntaxException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class SocketLogHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val gson: Gson
) : LogInterceptor.LogHandler {

    private val handlerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var socket: Socket? = null

    @Volatile
    private var currentBuildId: String? = null

    @Volatile
    private var isConnecting = false

    // Buffer for crash logs waiting for socket connection
    private val pendingCrashLogs = mutableListOf<Pair<String, String>>() // Pair<tag, message>

    // Sequence counter for crash logs (server will reorder by sequence)
    private var crashLogSequenceCounter: Long = 0

    init {
        // Observe auth state changes
        authRepository.isLoggedIn
            .onEach { isLoggedIn ->
                if (isLoggedIn) {
                    connect()
                } else {
                    disconnect()
                }
            }
            .launchIn(handlerScope)

        // Observe JWT changes to reconnect if needed
        authRepository.currentUser
            .onEach { user ->
                if (user != null && socket?.connected() != true) {
                    connect()
                }
            }
            .launchIn(handlerScope)
    }

    fun setBuildId(buildId: String) {
        currentBuildId = buildId
        // No longer saving to file - CrashMarker handles this
    }

    fun checkAndProcessCrashLogs() {
        handlerScope.launch {
            val log = { msg: String -> LogInterceptor.raw("SocketLogHandler", msg) }
            try {
                log("Checking for crash logs...")

                if (!CrashMarker.wasCrashedLastTime(context)) {
                    log("Last run exited cleanly")
                    return@launch
                }

                val crashedBuildId = CrashMarker.getCrashedBuildId(context)
                log("Last run crashed! BuildId: $crashedBuildId")

                if (crashedBuildId == null) {
                    log("No buildId found for crashed run")
                    return@launch
                }

                currentBuildId = crashedBuildId
                crashLogSequenceCounter = 0  // Reset sequence counter for this crash replay
                log("Set buildId in handler: $crashedBuildId")

                val crashesDir = File(context.filesDir, "crashes")

                // Process last10.txt
                val last10File = File(crashesDir, "last10.txt")
                if (last10File.exists()) {
                    val last10Lines = last10File.readLines()
                    log("Found ${last10Lines.size} lines in last10.txt")
                    pendingCrashLogs.add("CrashRecovery" to  "-".repeat(20))
                    pendingCrashLogs.add("CrashRecovery" to "Replaying the last ten captured log lines")
                    pendingCrashLogs.add("CrashRecovery" to  "-".repeat(20))
                    last10Lines
                        .filter { it.isNotBlank() }
                        .forEach { pendingCrashLogs.add("CrashRecovery" to it) }
                } else {
                    log("last10.txt does not exist, skipping")
                }

                // Process crash.txt
                val crashFile = File(crashesDir, "crash.txt")
                if (crashFile.exists()) {
                    val crashLines = crashFile.readLines()
                    log("Found ${crashLines.size} lines in crash.txt")
                    pendingCrashLogs.add("CrashRecovery" to  "-".repeat(20))
                    pendingCrashLogs.add("CrashRecovery" to "Replaying crash log")
                    pendingCrashLogs.add("CrashRecovery" to  "-".repeat(20))
                    crashLines
                        .filter { it.isNotBlank() }
                        .forEach { pendingCrashLogs.add("CrashRecovery" to it) }
                    log("Deleted crash.txt: ${crashFile.delete()}")
                } else {
                    log("crash.txt does not exist, skipping")
                }

                if (socket?.connected() == true) {
                    flushPendingCrashLogs()
                }
            } catch (e: Exception) {
                log("Error processing crash logs: ${e.message}")
                log("Stack trace: ${e.stackTraceToString()}")
            }
        }
    }

    private fun flushPendingCrashLogs(socketInstance: Socket? = null) {
        val logs = pendingCrashLogs.toList()
        pendingCrashLogs.clear()

        // If we have a socket instance from EVENT_CONNECT, trust it's connected
        // Otherwise check the stored socket
        val connectedSocket = socketInstance ?: socket
        if (connectedSocket?.connected() != true) {
            LogInterceptor.raw("SocketLogHandler", "Socket not connected, re-adding ${logs.size} logs to buffer")
            pendingCrashLogs.addAll(logs)
            return
        }

        // Send logs concurrently with sequence numbers (server will reorder)
        handlerScope.launch {
            logs.forEach { (tag, message) ->
                val sequence = crashLogSequenceCounter++
                launch {
                    sendLog("ERROR", tag, message, connectedSocket, sequence)
                }
            }
        }
    }

    override fun handleLog(level: String, tag: String, message: String) {
        handleLog(level, tag, message, socket)
    }

    private fun handleLog(level: String, tag: String, message: String, socketInstance: Socket?) {
        val buildId = currentBuildId
        if (buildId == null) {
            LogInterceptor.raw("SocketLogHandler", "No buildId set, skipping log")
            return
        }

        if (socketInstance?.connected() != true) {
            LogInterceptor.raw("SocketLogHandler", "Socket not connected, skipping log")
            return
        }

        // Launch concurrent coroutine for real-time logs (non-blocking, order doesn't matter)
        handlerScope.launch {
            sendLog(level, tag, message, socketInstance)
        }
    }

    // Shared suspend function for sending logs - executes synchronously within a coroutine
    private suspend fun sendLog(
        level: String,
        tag: String,
        message: String,
        socketInstance: Socket,
        sequence: Long? = null
    ) {
        try {
            val buildId = currentBuildId
            if (buildId == null) {
                LogInterceptor.raw("SocketLogHandler", "No buildId set, skipping log")
                return
            }

            val logData = BuildRuntimeLogData(
                buildId = buildId,
                level = level,
                message = "[$tag] $message",
                details = null,
                sentAt = Instant.now().toString(),
                sequence = sequence
            )

            val json = JSONObject(gson.toJson(logData))
            socketInstance.emit("build:runtime-log", json)
        } catch (e: Exception) {
            LogInterceptor.raw("SocketLogHandler", "Failed to send log: ${e.message}")
        }
    }

    private fun connect() {
        LogInterceptor.raw("SocketLogHandler", "Attempting to connect to log socket")
        if (isConnecting || socket?.connected() == true) {
            LogInterceptor.raw("SocketLogHandler", "Already connected or connecting to log socket")
            return
        }

        handlerScope.launch {
            try {
                isConnecting = true
                val jwt = authRepository.getJwt()
                if (jwt == null) {
                    isConnecting = false
                    return@launch
                }

                val wsUrl = BackendUrlProvider.getWsUrl()
                val options = IO.Options().apply {
                    forceNew = true
                    reconnection = false
                    transports = arrayOf("websocket")
                    auth = mapOf("token" to jwt)
                }

                val socketInstance = IO.socket(wsUrl, options)
                socket = socketInstance
                socketInstance.connect()

                socketInstance.on(Socket.EVENT_CONNECT) {
                    LogInterceptor.raw("SocketLogHandler", "Connected to log socket")
                    isConnecting = false
                    flushPendingCrashLogs(socketInstance)
                }

                socketInstance.on(Socket.EVENT_DISCONNECT) {
                    LogInterceptor.raw("SocketLogHandler", "Disconnected from log socket")
                    isConnecting = false
                }

                socketInstance.on(Socket.EVENT_CONNECT_ERROR) {
                    LogInterceptor.e("SocketLogHandler", "Error connecting to log socket: ${it.getOrNull(0)}")
                    isConnecting = false
                }
            } catch (e: URISyntaxException) {
                isConnecting = false
            } catch (e: Exception) {
                isConnecting = false
            }
        }
    }

    private fun disconnect() {
        handlerScope.launch {
            try {
                socket?.disconnect()
                socket?.close()
                socket = null
                currentBuildId = null
            } catch (e: Exception) {
                // Ignore errors during disconnect
            }
        }
    }
}

