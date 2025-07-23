package com.appversal.appstorys.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object RetrofitClient {
    private const val BASE_URL = "https://backend.appstorys.com/"
    private const val MQTT_BASE_URL = "https://users.appstorys.com/"

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(CampaignResponse::class.java, CampaignResponseDeserializer())
        .create()

    private val client by lazy {
        OkHttpClient.Builder()
//            .addInterceptor(
//                HttpLoggingInterceptor().apply {
//                    level = HttpLoggingInterceptor.Level.BODY
//                }
//            )
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    val mqttApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MQTT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
