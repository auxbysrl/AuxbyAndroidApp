package com.fivedevs.auxby.domain.utils

import android.content.Context
import android.net.Uri
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.OfferOwner
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.fivedevs.auxby.domain.models.OfferRequestModel
import com.fivedevs.auxby.domain.models.enums.CurrencyEnum
import com.fivedevs.auxby.domain.models.enums.OfferStateEnum
import com.fivedevs.auxby.domain.utils.FileUtils.compressImageFile
import com.fivedevs.auxby.domain.utils.FileUtils.getImageUrlAndConvertToFile
import com.fivedevs.auxby.domain.utils.Formatters.priceDecimalFormat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object OfferUtils {

    fun getOfferPreviewPrice(context: Context, offerRequestModel: OfferRequestModel): String {
        val currencySymbol = Currencies.currenciesList.firstOrNull { it.name.equals(offerRequestModel.currencyType, true) }?.symbol ?: CurrencyEnum.RON.symbol()
        return context.getString(
            R.string.offer_price_value, priceDecimalFormat.format(offerRequestModel.price),
            currencySymbol
        )
    }

    fun getOfferDetailsPrice(context: Context, offerModel: OfferModel): String {
        return context.getString(R.string.offer_price_value,
            priceDecimalFormat.format(offerModel.price).toString(),
            offerModel.currencyType?.ifEmpty { CurrencyEnum.RON.currencyType }?.uppercase()
                ?.let {
                    Currencies.currenciesList.firstOrNull { currencyModel -> currencyModel.name.equals(it, true) }?.symbol ?: CurrencyEnum.RON.symbol()
                }
        )
    }

    fun getFormattedAmountWithCurrency(context: Context, bidValue: Float, currency: String?): String {
        return context.getString(R.string.offer_price_value,
            priceDecimalFormat.format(bidValue).toString(),
            currency?.ifEmpty { CurrencyEnum.RON.currencyType }?.uppercase()
                ?.let {
                    Currencies.currenciesList.firstOrNull { currencyModel -> currencyModel.name.equals(it, true) }?.symbol ?: CurrencyEnum.RON.symbol()
                }
        )
    }

    fun getCurrency(currency: String?): String {
        return Currencies.currenciesList.firstOrNull { it.name.equals(currency, true) }?.symbol ?: CurrencyEnum.RON.symbol()
    }

    fun getHighestBidWithCurrency(offerModel: OfferModel): String {
        return "${getHighestBid(offerModel)}${getCurrency(offerModel.currencyType)}"
    }

    fun getHighestBid(offerModel: OfferModel): String {
        return if (offerModel.highestBid?.equals(0f) == true) {
            priceDecimalFormat.format(offerModel.price)
        } else {
            priceDecimalFormat.format(offerModel.highestBid)
        }
    }

    fun getYourBidWithCurrency(offerModel: OfferModel, user: User?): String {
        val bid = offerModel.bids
            ?.filter { it?.email.orEmpty() == user?.email.orEmpty() }
            ?.maxByOrNull { it?.bidValue ?: 0f }
            ?.bidValue ?: return NO_BID_LINE

        val formattedBid = priceDecimalFormat.format(bid.toDouble())
        return "$formattedBid${getCurrency(offerModel.currencyType)}"
    }

    fun getYourBid(offerModel: OfferModel, user: User?): Float {
        var yourBid = 0f
        user?.let {
            yourBid = offerModel.bids?.filter { it?.email.orEmpty() == user.email }
                ?.sortedByDescending { it?.bidValue }?.firstOrNull()?.bidValue ?: 0f
        }
        return yourBid
    }

    fun getFullUsername(user: User): String {
        return "${user.firstName} ${user.lastName}"
    }

    fun getFullOwnerName(owner: OfferOwner): String {
        return "${owner.firstName} ${owner.lastName}"
    }

    fun getOfferDate(offer: OfferModel, context: Context): String {
        return if (offer.isOnAuction) {
            if (offer.status.equals(OfferStateEnum.FINISHED.getStatusName()) || offer.status.equals(OfferStateEnum.INTERRUPTED.getStatusName())) {
                context.resources.getString(R.string.auction_ended)
            } else {
                DateUtils().getRemainingTimeForAuction(offer.auctionEndDate ?: Constants.EMPTY)
            }
        } else {
            DateUtils().getFormattedDateForOffer(offer.publishDate ?: Constants.EMPTY)
        }
    }

    fun getFavoriteIconByState(isSaved: Boolean): Int {
        return if (isSaved) R.drawable.ic_favorite_saved else R.drawable.ic_favorite_unsaved
    }

    fun getOfferDetailsFavoriteIconByState(isSaved: Boolean): Int {
        return if (isSaved) R.drawable.ic_favorite_selected_orange else R.drawable.ic_favorite_unselected_dark
    }

    fun getMultipartOfferPhotos(
        photos: List<OfferPhoto>,
        context: Context
    ): Array<MultipartBody.Part>? {
        if (photos.isEmpty()) return arrayOf()
        return photos.map {
            if (it.url.contains("https://s3.")) {
                getImageUrlAndConvertToFile(context, it.url)
            } else {
                val file = File(FileUtils.getRealPathFromURI(context, Uri.parse(it.url)))
                compressImageFile(file, context)
            }?.let { file ->
                val requestBody: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                MultipartBody.Part.createFormData("files", file.name, requestBody)
            } ?: kotlin.run { return null }
        }.toTypedArray()
    }

    private const val NO_BID_LINE = "-"

    fun getUserLastSeenTime(context: Context, lastSeen: String): String {
        if (lastSeen.isEmpty()) return Constants.EMPTY
        return if (DateUtils().isToday(DateUtils().getFormattedDateYearMonthDayFromServer(lastSeen))) {
            context.getString(
                R.string.active_today_at,
                DateUtils().getFormattedDateForUserActive(lastSeen)
            )
        } else if (DateUtils().isYesterday(DateUtils().getFormattedDateYearMonthDayFromServer(lastSeen))) {
            context.getString(
                R.string.active_yesterday_at,
                DateUtils().getFormattedDateForUserActive(lastSeen)
            )
        } else {
            context.getString(
                R.string.active_at,
                DateUtils().getFormattedDateForUserActive(lastSeen, DateUtils.FORMAT_DD_MMM_YYYY)
            )
        }
    }

}