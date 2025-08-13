package com.appversal.appstorys.api

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

internal class WebSocketClient() {
    private var webSocket: WebSocket? = null

    private var client: OkHttpClient = OkHttpClient()

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 100)

    val message: SharedFlow<String> = _message.asSharedFlow()

    private var isConnected = false

    fun connect(config: WebSocketConfig) = try {
        val request = Request.Builder()
            .url(config.url)
            .addHeader("Authorization", "Bearer ${config.token}")
            .addHeader("Session-ID", config.sessionID)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connection opened")
                isConnected = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "WebSocket message received: $text")
                _message.tryEmit(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d(TAG, "WebSocket binary message received")
                _message.tryEmit(bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code $reason")
                isConnected = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code $reason")
                isConnected = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}", t)
                isConnected = false
            }
        })

        true
    } catch (e: Exception) {
        Log.e(TAG, "Error connecting to WebSocket: ${e.message}", e)
        false
    }

    fun isConnected(): Boolean {
        return isConnected && webSocket != null
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Disconnecting")
            webSocket = null
            isConnected = false
            Log.d(TAG, "WebSocket disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting WebSocket: ${e.message}", e)
        }
    }

    companion object {
        private val TAG = WebSocketClient::class.java.simpleName
    }
}