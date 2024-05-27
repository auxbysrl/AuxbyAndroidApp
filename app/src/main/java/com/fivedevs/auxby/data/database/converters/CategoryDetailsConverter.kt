package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.data.database.entities.CategoryDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryDetailsConverter {
    @TypeConverter
    fun fromArrayList(model: List<CategoryDetails>): String {
        return Gson().toJson(model)
    }

    @TypeConverter
    fun fromString(stringModel: String): List<CategoryDetails> {
        val type = object : TypeToken<List<CategoryDetails>>() {}.type
        return Gson().fromJson(stringModel, type)
    }
}