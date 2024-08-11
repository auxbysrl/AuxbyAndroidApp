package com.fivedevs.auxby.domain.models.enums

enum class CurrencyEnum(val currencyType:String) {

    RON("Ron") {
        override fun symbol(): String {
            return "lei"
        }
    },
    EURO("Euro") {
        override fun symbol(): String {
            return "€"
        }
    },
    USD("USD") {
        override fun symbol(): String {
            return "$"
        }
    };

    abstract fun symbol(): String
}