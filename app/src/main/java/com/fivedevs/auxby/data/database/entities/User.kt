package com.fivedevs.auxby.data.database.entities

import androidx.room.Entity
import androidx.room.TypeConverters
import com.fivedevs.auxby.data.database.converters.UserAddressConverter

@Entity(tableName = "user", primaryKeys = ["email"])
@TypeConverters(UserAddressConverter::class)
data class User(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var address: UserAddress? = UserAddress(),
    var phone: String = "",
    var avatar: String? = "",
    var availableCoins: Int = 0
) {
    data class UserAddress(
        var city: String = "",
        var country: String = ""
    )
}
