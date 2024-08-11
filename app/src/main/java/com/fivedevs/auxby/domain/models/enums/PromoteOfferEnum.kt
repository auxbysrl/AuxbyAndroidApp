package com.fivedevs.auxby.domain.models.enums

enum class PromoteOfferEnum {
    ONE_DAY {
        override fun getType(): Int = 0
        override fun getRequiredCoins(): Int = 5
    },
    ONE_WEEK {
        override fun getType(): Int = 1
        override fun getRequiredCoins(): Int = 30
    },
    ONE_MONTH {
        override fun getType(): Int = 2
        override fun getRequiredCoins(): Int = 70
    };

    abstract fun getType(): Int
    abstract fun getRequiredCoins(): Int
}