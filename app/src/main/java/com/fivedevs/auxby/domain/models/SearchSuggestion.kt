package com.fivedevs.auxby.domain.models

data class CategoryResult(
    val categoryResult: Map<String, Int>
)

data class SearchSuggestion(
    val searchText: String = "",
    val noOffers: Int = 0,
    val category: CategoryModel = CategoryModel()
)