package com.fivedevs.auxby.data.api.response

import com.fivedevs.auxby.domain.models.OfferBid

data class BidActionResponse(
    var wasBidAccepted: Boolean = true,
    var offerBids: ArrayList<OfferBid> = arrayListOf()
)
