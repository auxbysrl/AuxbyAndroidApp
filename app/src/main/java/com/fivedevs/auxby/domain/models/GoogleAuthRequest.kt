package com.fivedevs.auxby.domain.models

data class GoogleAuthRequest(
    val token: String,
    val userReferralId: Int?
)