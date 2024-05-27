package com.fivedevs.auxby.domain.models

import com.fivedevs.auxby.data.database.entities.User

data class UserRegisterRequest(
    var firstName: String = "",
    var lastName: String = "",
    var password: String = "",
    var email: String = "",
    var address: User.UserAddress = User.UserAddress(),
    var phone: String = ""
)
