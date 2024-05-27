package com.fivedevs.auxby.domain.models.enums

enum class FiltersEnum(val type: String) {
    PAGE_KEY("page"),
    SIZE_KEY("size"),
    SORT_KEY("sort"),
    SORT_ORDER_KEY("DESC"),
    TITLE_KEY("title"),
    CATEGORIES_KEY("categories"),
    IS_ACTION_KEY("isOnAuction"),
    IS_PROMOTED_KEY("isPromotedOnly"),
    SORT_BY_PUBLISH_DATE_KEY("publishDate"),
}