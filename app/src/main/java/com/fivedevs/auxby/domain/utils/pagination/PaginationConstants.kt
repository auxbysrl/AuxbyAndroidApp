package com.fivedevs.auxby.domain.utils.pagination

import com.fivedevs.auxby.domain.models.enums.FiltersEnum

object PaginationConstants {
    const val API_PAGE_SIZE = 10
    const val PAGINATION_PAGE_START = 0

    const val ITEM = 1
    const val LOADING = 0

    val paginationFilters: Map<String, String> = mapOf(
        FiltersEnum.PAGE_KEY.type to PAGINATION_PAGE_START.toString(),
        FiltersEnum.SIZE_KEY.type to API_PAGE_SIZE.toString(),
        FiltersEnum.SORT_KEY.type to "${FiltersEnum.SORT_BY_PUBLISH_DATE_KEY.type},${FiltersEnum.SORT_ORDER_KEY.type}"
    )
}