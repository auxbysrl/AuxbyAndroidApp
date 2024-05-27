package com.fivedevs.auxby.screens.addOffer.factory

import android.content.Context
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.utils.extensions.concatenate
import com.fivedevs.auxby.screens.addOffer.factory.addOfferFactory.DynamicDropDownField
import com.fivedevs.auxby.screens.addOffer.factory.addOfferFactory.DynamicEditTextField
import com.fivedevs.auxby.screens.addOffer.factory.addOfferFactory.DynamicRadioBoxField
import com.fivedevs.auxby.screens.addOffer.factory.utils.FieldTypeEnum

class DynamicViewsManager(
    private val context: Context
) {

    private val dynamicEditTextField: DynamicEditTextField by lazy { DynamicEditTextField(context) }
    private val dynamicDropDownField: DynamicDropDownField by lazy { DynamicDropDownField(context) }
    private val dynamicRadioBoxField: DynamicRadioBoxField by lazy { DynamicRadioBoxField(context) }


    fun getValueByName(detailsValue: List<CategoryFieldsValue>, categoryName: String): String? {
        return detailsValue.firstOrNull { it.key.equals(categoryName, true) }?.value
    }

    fun areDynamicFieldsValid(): Boolean {
        val editTextField = dynamicEditTextField.validateDynamicFields()
        val radioBoxField = dynamicRadioBoxField.validateDynamicFields()
        val dropDownField = dynamicDropDownField.validateDynamicFields()

        return (editTextField && radioBoxField && dropDownField)
    }

    fun getCategoryDetailsValues(): List<CategoryFieldsValue> {
        return concatenate(
            dynamicEditTextField.getDynamicFieldsValue(),
            dynamicRadioBoxField.getDynamicFieldsValue(),
            dynamicDropDownField.getDynamicFieldsValue()
        )
    }

    fun getFieldByType(fieldType: String): AddOfferFactoryInterface? {
        return when (fieldType) {
            FieldTypeEnum.TEXT_INPUT_FIELD.fieldType -> dynamicEditTextField
            FieldTypeEnum.DROP_DOWN_FIELD.fieldType -> dynamicDropDownField
            FieldTypeEnum.RADIO_BOX_FIELD.fieldType -> dynamicRadioBoxField
            else -> null
        }
    }
}