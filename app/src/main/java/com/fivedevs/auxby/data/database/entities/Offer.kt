package com.fivedevs.auxby.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fivedevs.auxby.data.database.converters.*
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.models.OfferBid
import com.fivedevs.auxby.domain.models.OfferOwner
import com.fivedevs.auxby.domain.models.OfferPhoto

@Entity(tableName = "offer")
@TypeConverters(
    ListStringConverter::class,
    OfferOwnerConverter::class,
    ListPhotosConverter::class,
    OfferBidValueConverter::class,
    CategoryFieldsValueConverter::class
)
data class Offer(
    @PrimaryKey(autoGenerate = false)
    var id: Long,
    var title: String?,
    var location: String?,
    var description: String?,
    var categoryId: Int?,
    var publishDate: String?,
    var expirationDate: String?,
    var auctionStartDate: String?,
    var auctionEndDate: String?,
    var isOnAuction: Boolean,
    var bids: List<OfferBid?>,
    var price: Float?,
    var currencyType: String?,
    var owner: OfferOwner?,
    var highestBid: Float?,
    var photos: List<OfferPhoto>,
    var details: List<CategoryFieldsValue> = listOf(),
    var viewsNumber: Int = 0,
    var setAsFavoriteNumber: Int = 0,
    var isUserFavorite: Boolean = false,
    var status: String?,
    var isPromoted: Boolean = false,
    var autoExtend: Boolean = false,
    var condition: String?,
    var phoneNumbers: String
)
