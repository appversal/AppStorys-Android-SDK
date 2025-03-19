package com.appversal.appstorys.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object RetrofitClient {
    private const val BASE_URL = "https://backend.appstorys.com/"

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(CampaignResponse::class.java, CampaignResponseDeserializer())
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
