package com.fivedevs.auxby.domain.models

data class ErrorResponse(
    val message: String,
    val code: Int
)