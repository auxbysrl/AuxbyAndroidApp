package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.domain.models.LabelModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LabelConverter {
    @TypeConverter
    fun fromArrayList(labelModels: List<LabelModel>): String {
        return Gson().toJson(labelModels)
    }

    @TypeConverter
    fun fromString(labelModels: String): List<LabelModel> {
        val type = object : TypeToken<List<LabelModel>>() {}.type
        return Gson().fromJson(labelModels, type)
    }
}