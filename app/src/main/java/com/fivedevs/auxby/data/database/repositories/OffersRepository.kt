package com.fivedevs.auxby.data.database.repositories

import androidx.room.Transaction
import com.fivedevs.auxby.data.database.AppDatabase
import com.fivedevs.auxby.data.database.entities.OfferTypeStored
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.enums.OfferTypeStoredEnum
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class OffersRepository @Inject constructor(private val appDatabase: AppDatabase) {

    // OFFERS
    @Transaction
    fun insertOffers(offers: List<OfferModel>) {
        insertOffersWithType(offers, OfferTypeStoredEnum.OFFERS)
    }

    fun insertOffer(offer: OfferModel) {
        insertOfferWithType(offer, OfferTypeStoredEnum.OFFERS)
    }

    @Transaction
    fun clearOffers() {
        appDatabase.offersDao().clearOffers()
        appDatabase.offerTypeStoredDao().clearOffersTypeStored()
    }

    fun updateOffer(offer: OfferModel) {
        appDatabase.offersDao().insertOffer(offer.toOffer())
    }

    fun getActiveOffers(): Flowable<List<OfferModel>> {
        return appDatabase.offersDao().getActiveOffers()
    }

    fun getSingleActiveOffers(): Single<List<OfferModel>> {
        return appDatabase.offersDao().getSingleActiveOffers()
    }

    fun getOffersByCategoryId(categoryId: Int): Flowable<List<OfferModel>> {
        return appDatabase.offersDao().getOffersByCategoryId(categoryId)
    }

    fun getOfferByCategoryId(categoryId: Int): Single<OfferModel> {
        return appDatabase.offersDao().getOfferByCategoryId(categoryId)
    }

    fun getOfferById(offerId: Long): Flowable<OfferModel> {
        return appDatabase.offersDao().getOfferById(offerId)
    }

    fun getAuctionOffers(): Single<List<OfferModel>> {
        return appDatabase.offersDao().getAuctionOffers()
    }

    fun deleteOfferById(offerId: Long) {
        appDatabase.offersDao().deleteOffer(offerId)
        appDatabase.offerTypeStoredDao().deleteOfferById(offerId)
    }

    // FAVORITE
    @Transaction
    fun insertSavedOffers(offers: List<OfferModel>) {
        insertOffersWithType(offers, OfferTypeStoredEnum.FAVORITE)
    }

    fun handlingSavedOffer(offer: OfferModel) {
        if (offer.isUserFavorite) {
            offer.apply { setAsFavoriteNumber = setAsFavoriteNumber.inc() }
            insertSavedOffer(offer.id)
        } else {
            offer.apply { setAsFavoriteNumber = setAsFavoriteNumber.dec() }
            removeSavedOffer(offer.id)
        }
        updateOffer(offer)
    }

    private fun insertSavedOffer(offerId: Long) {
        appDatabase.offerTypeStoredDao().insertOfferWithTypes(OfferTypeStored(offerId, OfferTypeStoredEnum.FAVORITE))
    }

    private fun removeSavedOffer(offerId: Long) {
        appDatabase.offerTypeStoredDao().deleteOfferType(offerId, OfferTypeStoredEnum.FAVORITE)
        appDatabase.offersDao().removeOfferByType(offerId, OfferTypeStoredEnum.FAVORITE)
    }

    fun getSavedOffers(): Flowable<List<OfferModel>> {
        return appDatabase.offersDao().getOffersByType(OfferTypeStoredEnum.FAVORITE)
    }

    // MY_OFFERS
    @Transaction
    fun insertMyOffers(offers: List<OfferModel>) {
        insertOffersWithType(offers, OfferTypeStoredEnum.MY_OFFERS)
    }

    @Transaction
    fun insertMyOffer(offer: OfferModel) {
        appDatabase.offerTypeStoredDao().insertOfferWithTypes(OfferTypeStored(offer.id, OfferTypeStoredEnum.OFFERS))
    }

    fun getMyOffers(): Flowable<List<OfferModel>> {
        return appDatabase.offersDao().getOffersByType(OfferTypeStoredEnum.MY_OFFERS)
    }

    // MY_BIDS
    @Transaction
    fun insertBids(offers: List<OfferModel>) {
        insertOffersWithType(offers, OfferTypeStoredEnum.MY_BIDS)
    }

    fun getBids(): Flowable<List<OfferModel>> {
        return appDatabase.offersDao().getOffersByType(OfferTypeStoredEnum.MY_BIDS)
    }

    // BY TYPE
    @Transaction
    fun insertOffersWithType(offers: List<OfferModel>, type: OfferTypeStoredEnum) {
        appDatabase.offersDao().insertOffers(offers.map { it.toOffer() })
        val offerTypes = offers.map { OfferTypeStored(it.id, type) }
        appDatabase.offerTypeStoredDao().insertOffersWithTypes(offerTypes)
    }

    @Transaction
    fun insertOfferWithType(offer: OfferModel, type: OfferTypeStoredEnum) {
        appDatabase.offersDao().insertOffer(offer.toOffer())
        appDatabase.offerTypeStoredDao().insertOfferWithTypes(OfferTypeStored(offer.id, type))
    }

    // PROMOTED OFFERS
    @Transaction
    fun insertPromotedOffers(offers: List<OfferModel>) {
        insertOffersWithType(offers, OfferTypeStoredEnum.PROMOTED)
    }

    fun getPromotedOffers(): Flowable<List<OfferModel>> {
        return appDatabase.offersDao().getPromotedOffers()
    }
}