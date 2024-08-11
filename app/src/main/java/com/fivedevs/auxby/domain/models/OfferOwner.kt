package com.fivedevs.auxby.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OfferOwner(
    val lastName: String = "",
    val firstName: String = "",
    val userName: String = "",
    val avatarUrl: String? = "",
    val lastSeen: String = "",
    val rating: Float = 0f
) : Parcelable