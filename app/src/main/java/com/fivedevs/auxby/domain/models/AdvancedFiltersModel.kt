package com.fivedevs.auxby.domain.models

import com.fivedevs.auxby.domain.models.enums.CurrencyEnum

data class AdvancedFiltersModel(
    var categories: List<Int> = listOf(),
    var offerType: String? = null,
    var title: String? = null,
    var conditionType: String? = null,
    var priceFilter: PriceFilter = PriceFilter(),
    var sortDetails: SortDetails? = null,
    var location: String? = null
) {
    fun clear() {
        categories = listOf()
        offerType = null
        title = null
        conditionType = null
        priceFilter = PriceFilter()
        sortDetails = null
        location = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdvancedFiltersModel

        if (categories != other.categories) return false
        if (offerType != other.offerType) return false
        if (title != other.title) return false
        if (conditionType != other.conditionType) return false
        if (priceFilter != other.priceFilter) return false
        if (sortDetails != other.sortDetails) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

data class PriceFilter(
    var highestPrice: Int = Int.MAX_VALUE,
    var lowestPrice: Int = 0,
    var currencyType: String? = CurrencyEnum.RON.currencyType
)

data class SortDetails(
    var key: String = "",
    var orderType: String = "desc"
)