package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.domain.models.OfferBid
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OfferBidValueConverter {
    @TypeConverter
    fun fromArrayList(model: List<OfferBid>): String {
        return Gson().toJson(model)
    }

    @TypeConverter
    fun fromString(stringModel: String): List<OfferBid> {
        val type = object : TypeToken<List<OfferBid>>() {}.type
        return Gson().fromJson(stringModel, type)
    }
}