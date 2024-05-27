package com.fivedevs.auxby.domain.models

import com.google.gson.annotations.SerializedName

data class OfferPhoto(
    var url: String = "",
    @SerializedName("isPrimary")
    var isCover: Boolean = false
)