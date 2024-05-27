package com.fivedevs.auxby.domain.models.enums

enum class PromoteOfferEnum {
    ONE_DAY {
        override fun getType(): Int = 0
    },
    ONE_WEEK {
        override fun getType(): Int = 1
    },
    ONE_MONTH {
        override fun getType(): Int = 2
    };

    abstract fun getType(): Int
}