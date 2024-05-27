package com.fivedevs.auxby.data.database.converters

import androidx.room.TypeConverter
import com.fivedevs.auxby.domain.models.OfferOwner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OfferOwnerConverter {
    @TypeConverter
    fun fromArrayList(model: OfferOwner?): String {
        var offerOwner = model
        if (offerOwner == null) {
            offerOwner = OfferOwner()
        }
        return Gson().toJson(offerOwner)
    }

    @TypeConverter
    fun fromString(stringModel: String): OfferOwner {
        val type = object : TypeToken<OfferOwner>() {}.type
        return Gson().fromJson(stringModel, type)
    }
}