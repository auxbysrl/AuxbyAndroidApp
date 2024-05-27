package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryFieldsValueConverter {
    @TypeConverter
    fun fromArrayList(model: List<CategoryFieldsValue>): String {
        return Gson().toJson(model)
    }

    @TypeConverter
    fun fromString(stringModel: String): List<CategoryFieldsValue> {
        val type = object : TypeToken<List<CategoryFieldsValue>>() {}.type
        return Gson().fromJson(stringModel, type)
    }
}