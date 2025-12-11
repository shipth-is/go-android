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
import java.util.concurrent.atomic.AtomicLong
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

    // Atomic counter for sequence numbers (thread-safe for concurrent logging)
    // The sequence must start from 1
    private val sequenceCounter = AtomicLong(1)

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

        handlerScope.launch {
            try {
                // Split message by newlines and filter out empty lines
                val lines = message.lines().filter { it.isNotBlank() }

                // Send each line as a separate log entry
                for (line in lines) {
                    val sequence = sequenceCounter.getAndIncrement()
                    val logData = BuildRuntimeLogData(
                        buildId = buildId,
                        level = level,
                        message = "[$tag] $line",
                        details = null,
                        sentAt = Instant.now().toString(),
                        sequence = sequence
                    )

                    val json = JSONObject(gson.toJson(logData))
                    socketInstance.emit("build:runtime-log", json)
                }
            } catch (e: Exception) {
                LogInterceptor.raw("SocketLogHandler", "Failed to send log: ${e.message}")
            }
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

