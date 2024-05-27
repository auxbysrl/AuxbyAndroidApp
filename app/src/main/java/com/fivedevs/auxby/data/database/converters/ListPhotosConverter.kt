package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListPhotosConverter {
    @TypeConverter
    fun fromArrayList(model: List<OfferPhoto>): String {
        return Gson().toJson(model)
    }

    @TypeConverter
    fun fromString(stringModel: String): List<OfferPhoto> {
        val type = object : TypeToken<List<OfferPhoto>>() {}.type
        return Gson().fromJson(stringModel, type)
    }
}