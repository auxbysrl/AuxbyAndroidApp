package com.fivedevs.auxby.domain.models

data class OfferBid(
    var userName: String,
    var email: String,
    var userAvatar: String?,
    val bidValue: Float,
    var bidDate: String,
    val winner: Boolean,
    val lastSeen: String,
    val phone: String
)