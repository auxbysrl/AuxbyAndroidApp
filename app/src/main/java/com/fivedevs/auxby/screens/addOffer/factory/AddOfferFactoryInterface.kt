package com.fivedevs.auxby.screens.addOffer.factory

import android.view.View
import com.fivedevs.auxby.domain.models.CategoryField
import com.fivedevs.auxby.domain.models.CategoryFieldsValue

interface AddOfferFactoryInterface {

    fun createDynamicView(categoryField: CategoryField, valueByName: String?): View
    fun validateDynamicFields(): Boolean
    fun getDynamicFieldsValue(): List<CategoryFieldsValue>
}