package com.fivedevs.auxby.domain.models

data class ChangePasswordRequest(
    var oldPassword: String = "",
    var newPassword: String = ""
)
