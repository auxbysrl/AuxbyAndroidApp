package com.fivedevs.auxby.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fivedevs.auxby.data.database.converters.CategoryDetailsConverter
import com.fivedevs.auxby.data.database.converters.CategoryFieldConverter
import com.fivedevs.auxby.data.database.converters.LabelConverter
import com.fivedevs.auxby.domain.models.CategoryField
import com.fivedevs.auxby.domain.models.LabelModel

@Entity(tableName = "category_details")
@TypeConverters(LabelConverter::class, CategoryFieldConverter::class, CategoryDetailsConverter::class)
data class CategoryDetails(
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0,
    var icon: String = "",
    var label: List<LabelModel> = listOf(),
    var parentName: String = "",
    var categoryDetails: List<CategoryField> = listOf(),
    var subcategories: List<CategoryDetails> = listOf(),
    var addOfferCost: Int,
    var placeBidCost: Int
)