package com.fivedevs.auxby.domain.models

import androidx.room.Embedded
import androidx.room.Relation
import com.fivedevs.auxby.data.database.entities.Offer
import com.fivedevs.auxby.data.database.entities.OfferTypeStored

data class OfferWithTypes(
    @Embedded
    var offer: Offer,

    @Relation(parentColumn = "id", entityColumn = "offerId")
    var types: List<OfferTypeStored>
)