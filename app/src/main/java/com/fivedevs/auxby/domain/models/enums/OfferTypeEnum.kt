package com.fivedevs.auxby.domain.models.enums

enum class OfferTypeEnum(val offerType: String) {
    AUCTION("Auction"),
    FIXE_PRICE("Fix price");

    fun getValue(offerType: String): String {
        return values().firstOrNull { it.offerType == offerType }?.offerType.orEmpty().ifEmpty { FIXE_PRICE.offerType }
    }
}