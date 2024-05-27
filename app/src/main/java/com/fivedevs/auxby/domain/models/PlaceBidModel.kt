package com.fivedevs.auxby.domain.models

data class PlaceBidModel(
    var offerId: Long,
    var amount: Int,
    var requiredCoins: Int = 0
)
