package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListStringConverter {
    @TypeConverter
    fun fromArrayList(list: List<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromString(string: String): List<String> {
        val gson = Gson()
        val listType = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(string, listType)
    }
}