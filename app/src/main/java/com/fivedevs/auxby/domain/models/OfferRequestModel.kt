package com.fivedevs.auxby.domain.models

import com.fivedevs.auxby.domain.models.enums.ConditionTypeEnum
import com.fivedevs.auxby.domain.models.enums.CurrencyEnum
import com.fivedevs.auxby.domain.models.enums.OfferTypeEnum
import com.fivedevs.auxby.domain.utils.Currencies
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.extensions.capitalizeFirst
import com.fivedevs.auxby.domain.utils.extensions.orElse

data class OfferRequestModel(
    var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var categoryId: Int = 0,
    var auctionEndDate: String = "",
    var publishDate: String = "",
    var offerType: String = OfferTypeEnum.FIXE_PRICE.offerType,
    var conditionType: String = ConditionTypeEnum.USED.getConditionName(),
    var requiredCoins: Int = 0,
    var price: Float = 0f,
    var currencyType: String = CurrencyEnum.RON.currencyType,
    var contactInfo: ContactInfo = ContactInfo(),
    var categoryDetails: List<CategoryFieldsValue> = listOf(),
    var photos: ArrayList<OfferPhoto> = arrayListOf(),
    var viewsNumber: Int = 0
) {

    fun setOfferTypeValue(offerTypeTag: String) {
        this.offerType = offerTypeTag
    }

    fun setConditionTypeValue(conditionTypeTag: String) {
        this.conditionType = conditionTypeTag
    }

    fun setCurrencyTypeValue(text: String) {
        val conditionTypeEnum = Currencies.currenciesList.firstOrNull { it.symbol.equals(text, true) }
        if (conditionTypeEnum != null) {
            this.currencyType = conditionTypeEnum.name
        }
    }

    fun setPriceValue(text: String) {
        this.price = text.toFloatOrNull().orElse(0f)
    }

    fun setEndDate(text: String) {
        if (text.isNotEmpty()) {
            this.auctionEndDate = DateUtils().getFormatOfferEndDate(text)
        }
    }
}

fun OfferModel.toOfferRequestModel(): OfferRequestModel {
    val offerRequestModel = OfferRequestModel(
        id = id,
        title = title.orEmpty(),
        description = description.orEmpty(),
        categoryId = categoryId ?: 0,
        auctionEndDate = auctionEndDate.orEmpty(),
        publishDate = publishDate.orEmpty(),
        price = price ?: 0f,
        currencyType = currencyType.orEmpty(),
        viewsNumber = viewsNumber,
        offerType = if (isOnAuction) OfferTypeEnum.AUCTION.offerType else OfferTypeEnum.FIXE_PRICE.offerType,
        contactInfo = ContactInfo("", location.orEmpty()),
        conditionType = condition.lowercase().capitalizeFirst()
    )
    offerRequestModel.photos.addAll(photos)
    offerRequestModel.categoryDetails = ArrayList(details)

    return offerRequestModel
}