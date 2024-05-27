package com.fivedevs.auxby.domain.models

data class OfferOwner(
    val lastName: String = "",
    val firstName: String = "",
    val userName: String = "",
    val avatarUrl: String? = "",
    val lastSeen: String = ""
)