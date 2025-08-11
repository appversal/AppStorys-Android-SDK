package com.appversal.appstorys.api

import android.content.Context
import android.util.Log
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class MqttWebSocketClient(private val context: Context) {
    private var mqttClient: MqttAndroidClient? = null
    private val messageChannel = Channel<String>(Channel.UNLIMITED)
    private var userTopic: String? = null
    private var isConnectionInProgress = false

    val messageFlow: Flow<String> = messageChannel.receiveAsFlow()

    suspend fun connectWithConfig(mqttConfig: MqttConfig): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            if (isConnectionInProgress) {
                continuation.resumeWithException(Exception("Connection already in progress"))
                return@suspendCancellableCoroutine
            }

            isConnectionInProgress = true

            try {
                val correctedBrokerUrl = mqttConfig.broker

                mqttClient = MqttAndroidClient(context, correctedBrokerUrl, mqttConfig.clientID)
                userTopic = mqttConfig.topic

                val connOpts = MqttConnectOptions().apply {
                    isCleanSession = true
                    connectionTimeout = 30
                    keepAliveInterval = 60
                    isAutomaticReconnect = false
                }

                mqttClient?.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        isConnectionInProgress = false
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        message?.let {
                            val messageContent = String(it.payload)
                            messageChannel.trySend(messageContent)
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    }
                })

                continuation.invokeOnCancellation {
                    isConnectionInProgress = false
                    try {
                        mqttClient?.disconnect()
                    } catch (e: Exception) {
                        Log.e("MqttClient", "Error during cancellation cleanup", e)
                    }
                }

                mqttClient?.connect(connOpts, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        try {
                            if (continuation.isActive) {
                                isConnectionInProgress = false
                                continuation.resume(true)
                                subscribeToTopicWhenReady()
                            }
                        } catch (e: Exception) {
                            Log.e("MqttClient", "Error in onSuccess callback", e)
                            isConnectionInProgress = false
                        }
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        try {
                            if (continuation.isActive) {
                                Log.e("MqttClient", "Failed to connect to MQTT broker", exception)
                                isConnectionInProgress = false
                                continuation.resumeWithException(
                                    exception ?: Exception("MQTT connection failed")
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("MqttClient", "Error in onFailure callback", e)
                            isConnectionInProgress = false
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("MqttClient", "Exception during MQTT connection", e)
                isConnectionInProgress = false
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    private fun subscribeToUserTopic() {
        userTopic?.let { topic ->
            if (mqttClient?.isConnected != true) {
                return
            }

            try {
                mqttClient?.subscribe(topic, 1, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e("MqttClient", "Failed to subscribe to topic: $topic", exception)
                    }
                })
            } catch (e: Exception) {
                Log.e("MqttClient", "Exception subscribing to topic: $topic", e)
            }
        }
    }

    fun subscribeToTopicWhenReady() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            subscribeToUserTopic()
        }, 100)
    }

    fun disconnect() {
        try {
            isConnectionInProgress = false
            userTopic?.let {
                mqttClient?.unsubscribe(it, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e("MqttClient", "Failed to unsubscribe", exception)
                    }
                })
            }
//            messageChannel.close()
            mqttClient?.disconnect()
            mqttClient?.close()
        } catch (e: Exception) {
            Log.e("MqttClient", "Error during disconnect", e)
        } finally {
            mqttClient = null
            userTopic = null
        }
    }

    fun isConnected(): Boolean = mqttClient?.isConnected ?: false

    private val gson = com.google.gson.Gson()
}