package com.appversal.appstorys.api

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import okio.ByteString

internal class WebSocketClient(private val context: Context) {
    private var webSocket: WebSocket? = null
    private var client: OkHttpClient = OkHttpClient()

    private val _messageFlow = MutableSharedFlow<String>(extraBufferCapacity = 100)
    val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    private var isConnected = false

    fun connectWithConfig(config: WebSocketConfig): Boolean {
        try {
            val request = Request.Builder()
                .url(config.url)
                .addHeader("Authorization", "Bearer ${config.token}")
                .addHeader("Session-ID", config.sessionID)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("WebSocketClient", "WebSocket connection opened")
                    isConnected = true
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d("WebSocketClient", "WebSocket message received: $text")
                    _messageFlow.tryEmit(text)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    Log.d("WebSocketClient", "WebSocket binary message received")
                    _messageFlow.tryEmit(bytes.utf8())
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("WebSocketClient", "WebSocket closing: $code $reason")
                    isConnected = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("WebSocketClient", "WebSocket closed: $code $reason")
                    isConnected = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("WebSocketClient", "WebSocket error: ${t.message}", t)
                    isConnected = false
                }
            })

            return true
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Error connecting to WebSocket: ${e.message}", e)
            return false
        }
    }

    fun isConnected(): Boolean {
        return isConnected && webSocket != null
    }

    fun sendMessage(message: String): Boolean {
        return try {
            webSocket?.send(message) ?: false
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Error sending message: ${e.message}", e)
            false
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Disconnecting")
            webSocket = null
            isConnected = false
            Log.d("WebSocketClient", "WebSocket disconnected")
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Error disconnecting WebSocket: ${e.message}", e)
        }
    }
}