package com.fivedevs.auxby.domain.models

data class UserLoginRequest(
    var email: String = "",
    var password: String = "",
)
