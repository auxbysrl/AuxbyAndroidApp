package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.domain.models.CategoryDetailsModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryDetailsModelConverter {
    @TypeConverter
    fun fromArrayList(model: List<CategoryDetailsModel>): String {
        return Gson().toJson(model)
    }

    @TypeConverter
    fun fromString(stringModel: String): List<CategoryDetailsModel> {
        val type = object : TypeToken<List<CategoryDetailsModel>>() {}.type
        return Gson().fromJson(stringModel, type)
    }
}