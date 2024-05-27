package com.fivedevs.auxby.domain.models

data class PromoteOfferModel(
    val icon: Int,
    val title: String,
    var isChecked: Boolean = false
)
