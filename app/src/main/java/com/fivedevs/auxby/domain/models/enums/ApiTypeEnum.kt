package com.fivedevs.auxby.domain.models.enums

import com.fivedevs.auxby.BuildConfig

enum class ApiTypeEnum(val url: String) {
    AUXBY_PLATFORM(BuildConfig.AUXBY_PLATFORM),
    AUXBY_OFFER_MANAGEMENT(BuildConfig.AUXBY_OFFER_MANAGEMENT)
}