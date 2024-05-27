package com.fivedevs.auxby.domain.models.enums

enum class CurrencyEnum(val currencyType:String) {

    RON("Ron") {
        override fun short(): String {
            return "lei"
        }
    },
    EURO("Euro") {
        override fun short(): String {
            return "€"
        }
    };

    abstract fun short(): String
}