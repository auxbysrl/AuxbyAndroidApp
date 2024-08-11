package com.fivedevs.auxby.domain.models

data class CoinBundle(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var coins: String = "",
    var price: String = "",
    var priceInMicros: Long = 0L,
    var isChecked: Boolean = false
)
