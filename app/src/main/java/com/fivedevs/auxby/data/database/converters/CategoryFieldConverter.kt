package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.domain.models.CategoryField
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryFieldConverter {
    @TypeConverter
    fun fromArrayList(model: List<CategoryField>): String {
        return Gson().toJson(model)
    }

    @TypeConverter
    fun fromString(stringModel: String): List<CategoryField> {
        val type = object : TypeToken<List<CategoryField>>() {}.type
        return Gson().fromJson(stringModel, type)
    }
}