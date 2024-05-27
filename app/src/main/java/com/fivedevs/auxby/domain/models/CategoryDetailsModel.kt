package com.fivedevs.auxby.domain.models

import android.content.Context
import androidx.room.TypeConverters
import com.fivedevs.auxby.data.database.converters.CategoryDetailsModelConverter
import com.fivedevs.auxby.data.database.converters.CategoryFieldConverter
import com.fivedevs.auxby.data.database.converters.LabelConverter
import com.fivedevs.auxby.domain.utils.extensions.getLanguageCode

@TypeConverters(LabelConverter::class, CategoryFieldConverter::class, CategoryDetailsModelConverter::class)
data class CategoryDetailsModel(
    var id: Int = -1,
    var icon: String = "",
    var label: List<LabelModel> = listOf(),
    var parentName: String = "",
    var categoryDetails: List<CategoryField> = listOf(),
    var subcategories: List<CategoryDetailsModel> = listOf(),
    var addOfferCost: Int = 0,
    var placeBidCost: Int = 0
) {

    fun getName(context: Context): String {
        return label.first { it.language.equals(context.getLanguageCode(), true) }.translation
    }

    fun getSortedCategoryDetails() = categoryDetails.toMutableList().sortedBy { it.guiOrder }

    fun subcategoriesToCategoryModel() = subcategories.map {
        CategoryModel(
            it.id, it.icon, it.label, null
        )
    }
}
