package com.example.carousal

import android.app.Application
import android.content.Context
import com.appversal.appstorys.AppStorysAPI
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class App : Application() {


    val screenNameNavigation = MutableStateFlow("")
    private val appScope = MainScope()

    override fun onCreate() {
        super.onCreate()
        val attributes: Map<String, Any> = mapOf("name" to "Alice", "age" to 25)

        //1163a1a2-61a8-486c-b263-7252f9a502c2
        //5bb1378d-9f32-4da8-aed1-1ee44d086db7

        //afadf960-3975-4ba2-933b-fac71ccc2002
        //13555479-077f-445e-87f0-e6eae2e215c5

        val userId = getOrCreateUserId()

        val appStorysApi = AppStorysAPI.getInstance()

        // Initialize CampaignManager with userId and appId
        appStorysApi.initialize(
            context = this,
//            appId = "5a2ecc86-346a-4895-b14c-f662cc12071b",
//            accountId = "70ddda52-2106-453b-919a-d63d1287bdf7",
            appId = "9e1b21a2-350a-4592-918c-2a19a73f249a",
            accountId = "4350bf8e-0c9a-46bd-b953-abb65ab21d11",
//            userId = userId,
            userId = "27c39982-cbec-4fa5-ba1d-4944a03b6d42",
//            userId = "nameisprem",
            attributes = attributes,
            navigateToScreen = { screen ->
                println("Navigating to $screen")
                navigateToScreen(screen)
            }
        )

        appStorys = appStorysApi
    }

    private fun getOrCreateUserId(): String {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val existingUserId = prefs.getString("appstorys_user_id", null)
        return if (existingUserId != null) {
            existingUserId
        } else {
            val newUserId = UUID.randomUUID().toString()
            prefs.edit().putString("appstorys_user_id", newUserId).apply()
            newUserId
        }
    }


    fun navigateToScreen(name: String) {
        appScope.launch {
            screenNameNavigation.emit(name)
        }
    }

    fun resetNavigation() {
        appScope.launch {
            screenNameNavigation.emit("")
        }
    }

    companion object {
        lateinit var appStorys: AppStorysAPI
            private set
    }
}
