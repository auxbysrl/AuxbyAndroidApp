package com.fivedevs.auxby.domain.utils

import com.fivedevs.auxby.domain.models.enums.ApiTypeEnum

object EnvironmentManager {

    private val environments = mutableListOf(
        ApiTypeEnum.AUXBY_PLATFORM,
        ApiTypeEnum.AUXBY_OFFER_MANAGEMENT,
    )

    fun getBaseUrl(apiType: ApiTypeEnum): String {
        return environments.find { it == apiType }?.url.toString()
    }
}