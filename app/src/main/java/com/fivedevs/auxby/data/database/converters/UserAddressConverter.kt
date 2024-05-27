package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.data.database.entities.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserAddressConverter {
    @TypeConverter
    fun fromArrayList(userAddress: User.UserAddress): String {
        return Gson().toJson(userAddress)
    }

    @TypeConverter
    fun fromString(earningsBreakdown: String): User.UserAddress {
        val type = object : TypeToken<User.UserAddress>() {}.type
        return Gson().fromJson(earningsBreakdown, type)
    }
}