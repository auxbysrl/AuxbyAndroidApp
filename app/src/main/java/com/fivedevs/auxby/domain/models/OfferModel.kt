package com.fivedevs.auxby.domain.models

import androidx.room.TypeConverters
import com.fivedevs.auxby.data.database.converters.CategoryFieldsValueConverter
import com.fivedevs.auxby.data.database.converters.ListPhotosConverter
import com.fivedevs.auxby.data.database.converters.ListStringConverter
import com.fivedevs.auxby.data.database.converters.OfferBidValueConverter
import com.fivedevs.auxby.data.database.converters.OfferOwnerConverter
import com.fivedevs.auxby.data.database.entities.Offer

@TypeConverters(
    ListStringConverter::class,
    OfferOwnerConverter::class,
    ListPhotosConverter::class,
    OfferBidValueConverter::class,
    CategoryFieldsValueConverter::class
)
data class OfferModel(
    var id: Long = 0,
    var title: String? = "",
    var location: String? = "",
    var description: String? = "",
    var categoryId: Int? = 0,
    var publishDate: String? = "",
    var expirationDate: String? = "",
    var auctionStartDate: String? = "",
    var auctionEndDate: String? = "",
    var isOnAuction: Boolean = false,
    var bids: MutableList<OfferBid?>? = mutableListOf(),
    var price: Float? = 0f,
    var currencyType: String? = "",
    var currencySymbol: String? = "",
    var owner: OfferOwner? = OfferOwner(),
    var highestBid: Float? = 0f,
    var photos: List<OfferPhoto> = listOf(),
    var details: List<CategoryFieldsValue> = listOf(),
    var viewsNumber: Int = 0,
    var setAsFavoriteNumber: Int = 0,
    var isUserFavorite: Boolean = false,
    var status: String? = "",
    var isPromoted: Boolean = false,
    var autoExtend: Boolean = false,
    var condition: String = "",
    var phoneNumbers: String = "",
    var deepLink: String? = ""
) {
    fun toOffer() = Offer(
        id,
        title,
        location,
        description,
        categoryId,
        publishDate,
        expirationDate,
        auctionStartDate,
        auctionEndDate,
        isOnAuction,
        bids.orEmpty().toList(),
        price,
        currencyType.orEmpty(),
        currencySymbol,
        owner,
        highestBid,
        photos,
        details,
        viewsNumber,
        setAsFavoriteNumber,
        isUserFavorite,
        status,
        isPromoted,
        autoExtend,
        condition,
        phoneNumbers,
        deepLink.orEmpty()
    )
}

