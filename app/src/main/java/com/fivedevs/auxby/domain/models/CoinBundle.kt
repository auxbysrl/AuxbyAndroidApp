package com.fivedevs.auxby.domain.models

data class CoinBundle(
    var id: Int = 0,
    var name: String = "",
    var description: String = "",
    var coins: Long = 0,
    var isChecked: Boolean = false
)
