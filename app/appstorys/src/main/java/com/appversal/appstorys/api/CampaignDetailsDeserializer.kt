package com.appversal.appstorys.api

import com.appversal.appstorys.utils.removeDoubleQuotes
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

internal class CampaignResponseDeserializer : JsonDeserializer<CampaignResponse> {
    override fun deserialize(
        json: JsonElement, typeOfT: Type, context: JsonDeserializationContext
    ): CampaignResponse {
        val jsonObject = json.asJsonObject

        val userId = jsonObject.get("user_id")?.takeIf { !it.isJsonNull }?.asString?.removeDoubleQuotes() ?: ""
        val campaignsJsonArray = jsonObject.getAsJsonArray("campaigns")?.takeIf { !it.isJsonNull }

        val campaigns = campaignsJsonArray?.map { campaignElement ->
            val campaignObject = campaignElement.asJsonObject
            val campaignType = campaignObject.get("campaign_type")?.asString ?: ""

            val detailsJson = campaignObject.get("details")
            val details: Any? = when (campaignType) {
                "FLT" -> context.deserialize(detailsJson, FloaterDetails::class.java)
                "CSAT" -> context.deserialize(detailsJson, CSATDetails::class.java)
                "WID" -> context.deserialize(detailsJson, WidgetDetails::class.java)
                "BAN" -> context.deserialize(detailsJson, BannerDetails::class.java)
                "REL" -> context.deserialize(detailsJson, ReelsDetails::class.java)
                "TTP" -> context.deserialize(detailsJson, TooltipsDetails::class.java)
                "PIP" -> context.deserialize(detailsJson, PipDetails::class.java)
                "BTS" -> context.deserialize(detailsJson, BottomSheetDetails::class.java)
                "STR" -> context.deserialize(detailsJson, object : TypeToken<List<StoryGroup>>() {}.type)
                else -> null
            }

            val position = campaignObject.get("position")?.takeIf { !it.isJsonNull }?.asString?.removeDoubleQuotes()

            Campaign(
                id = campaignObject.get("id")?.takeIf { !it.isJsonNull }?.asString?.removeDoubleQuotes() ?: "",
                campaignType = campaignType,
                details = details,
                position = position
            )
        }
        return CampaignResponse(userId, campaigns ?: emptyList())
    }
}