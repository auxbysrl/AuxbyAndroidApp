package com.fivedevs.auxby.data.database.entities

import androidx.room.Entity
import com.fivedevs.auxby.domain.models.enums.OfferTypeStoredEnum

@Entity(tableName = "offer_type_stored", primaryKeys = ["offerId", "type"])
data class OfferTypeStored(
    var offerId: Long,
    var type: OfferTypeStoredEnum
)