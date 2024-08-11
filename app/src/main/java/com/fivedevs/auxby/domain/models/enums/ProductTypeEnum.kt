package com.fivedevs.auxby.domain.models.enums

enum class ProductTypeEnum(val value: String) {
    SILVER("auxby.silver.pack"),
    GOLD("auxby.gold.pack"),
    DIAMOND("auxby.diamond.pack");

    companion object {
        fun fromValue(value: String): ProductTypeEnum? {
            return entries.find { it.value == value }
        }
    }
}