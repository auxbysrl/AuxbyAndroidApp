package com.fivedevs.auxby.domain.models

import androidx.room.TypeConverters
import com.fivedevs.auxby.data.database.converters.LabelConverter
import com.fivedevs.auxby.domain.utils.Utils

@TypeConverters(LabelConverter::class)
data class CategoryModel(
    var id: Int = 0,
    var icon: String = "",
    var label: List<LabelModel> = mutableListOf(),
    var noOffers: Int? = 0
) {
    fun getIconUrl() = Utils.getFullImageUrl(icon)
}

