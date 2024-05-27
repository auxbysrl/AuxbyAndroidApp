package com.fivedevs.auxby.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fivedevs.auxby.data.database.converters.LabelConverter
import com.fivedevs.auxby.domain.models.LabelModel

@Entity(tableName = "category")
@TypeConverters(LabelConverter::class)
data class Category(
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0,
    var icon: String = "",
    var label: MutableList<LabelModel> = mutableListOf(),
    var noOffers: Int = 0
)