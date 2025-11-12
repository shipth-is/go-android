package com.shipthis.go.util

import com.google.gson.Gson
import com.shipthis.go.LogInterceptor
import com.shipthis.go.data.model.BuildRuntimeLogData
import com.shipthis.go.data.repository.AuthRepository
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URISyntaxException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class SocketLogHandler @Inject constructor(
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
    }

    override fun handleLog(level: String, tag: String, message: String) {
        val buildId = currentBuildId
        if (buildId == null) {
            // Skip logging if no buildId is set
            return
        }

        val socketInstance = socket
        if (socketInstance?.connected() != true) {
            // Skip if not connected
            return
        }

        handlerScope.launch {
            try {
                val logData = BuildRuntimeLogData(
                    buildId = buildId,
                    level = level,
                    message = "[$tag] $message",
                    details = null,
                    sentAt = Instant.now().toString()
                )

                // Convert to Map for Socket.IO serialization
                val json = JSONObject(gson.toJson(logData))
                socketInstance.emit("build:runtime-log", json)
            } catch (e: Exception) {
                // Silently fail - don't log errors about logging
            }
        }
    }

    private fun connect() {
        LogInterceptor.i("SocketLogHandler", "Attempting to connect to log socket")


        if (isConnecting || socket?.connected() == true) {
            LogInterceptor.i("SocketLogHandler", "Already connected or connecting to log socket")
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

                    // Use 'auth' just like in Node.js
                    auth = mapOf("token" to jwt)
                }

                val socketInstance = IO.socket(wsUrl, options)
                socket = socketInstance
                socketInstance.connect()

                socketInstance.on(Socket.EVENT_CONNECT) {
                    // Loud log here
                    LogInterceptor.i("SocketLogHandler", "Connected to log socket")
                    isConnecting = false
                }

                socketInstance.on(Socket.EVENT_DISCONNECT) {
                    LogInterceptor.i("SocketLogHandler", "Disconnected from log socket")
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

