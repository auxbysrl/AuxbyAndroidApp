package com.fivedevs.auxby.domain.models

data class OffersResponse(
    var content: MutableList<OfferModel> = mutableListOf(),
    var totalElements: Int = 0,
    var totalPages: Int = 0,
    var page: Int = 0,
    var size: Int = 0,
    var sort: MutableList<String> = mutableListOf()
)
