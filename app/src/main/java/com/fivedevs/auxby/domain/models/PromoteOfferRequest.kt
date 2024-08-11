package com.fivedevs.auxby.domain.models

data class PromoteOfferRequest(
    var requiredCoins: Int,
    var expirationDate: String
)