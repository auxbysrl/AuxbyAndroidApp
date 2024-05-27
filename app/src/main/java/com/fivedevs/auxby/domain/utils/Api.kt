package com.fivedevs.auxby.domain.utils

import com.fivedevs.auxby.domain.models.enums.ApiTypeEnum

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Api(
    val value: ApiTypeEnum
)
