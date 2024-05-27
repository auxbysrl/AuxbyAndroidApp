package com.fivedevs.auxby.domain.models

import android.text.InputType
import com.fivedevs.auxby.screens.addOffer.factory.utils.InputTypeEnum

data class CategoryField(
    var guiOrder: Int,
    val parent: String,
    var name: String = "",
    var type: String = "",
    var placeholder: String? = "",
    var required: Boolean = false,
    var label: List<LabelModel> = listOf(),
    var options: List<FieldOption> = listOf(),
    var constraints: FieldConstraints? = FieldConstraints(),
)

data class FieldOption(
    var name: String = "",
    var parentOption: String = "",
    var label: List<LabelModel> = listOf(),
    var childOptions: List<String> = listOf(),
)

data class FieldConstraints(
    val maxLength: Int? = 0,
    val maxLines: Int? = 0,
    val inputType: String? = ""
) {
    fun getFieldInputType(): Int {
        return when (inputType.orEmpty()) {
            InputTypeEnum.TEXT_INPUT_TYPE.inputType -> InputType.TYPE_CLASS_TEXT
            InputTypeEnum.NUMERIC_TYPE.inputType -> InputType.TYPE_CLASS_NUMBER
            else -> InputType.TYPE_CLASS_TEXT
        }
    }
}