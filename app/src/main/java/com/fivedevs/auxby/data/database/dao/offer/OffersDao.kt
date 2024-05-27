package com.fivedevs.auxby.data.database.dao.offer

import androidx.room.*
import com.fivedevs.auxby.data.database.entities.Offer
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.enums.OfferStateEnum
import com.fivedevs.auxby.domain.models.enums.OfferTypeStoredEnum
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants.API_PAGE_SIZE
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface OffersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOffers(offers: List<Offer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOffer(offers: Offer)

    @Update
    fun updateOffer(offerModel: Offer)

    @Query("SELECT offer.* FROM offer INNER JOIN offer_type_stored ON offer.id = offer_type_stored.offerId WHERE offer_type_stored.type = :type AND offer.status = :status ORDER BY publishDate DESC")
    fun getActiveOffers(
        status: String = OfferStateEnum.ACTIVE.getStatusName(),
        type: OfferTypeStoredEnum = OfferTypeStoredEnum.OFFERS
    ): Flowable<List<OfferModel>>

    @Query("SELECT offer.* FROM offer INNER JOIN offer_type_stored ON offer.id = offer_type_stored.offerId WHERE offer_type_stored.type = :type ORDER BY publishDate DESC LIMIT 10")
    fun getPromotedOffers(
        type: OfferTypeStoredEnum = OfferTypeStoredEnum.OFFERS
    ): Single<List<OfferModel>>

    @Query("SELECT offer.* FROM offer INNER JOIN offer_type_stored ON offer.id = offer_type_stored.offerId WHERE offer_type_stored.type = :type AND offer.categoryId = :categoryId ORDER BY publishDate DESC LIMIT 10")
    fun getOffersByCategoryId(
        categoryId: Int,
        type: OfferTypeStoredEnum = OfferTypeStoredEnum.OFFERS
    ): Flowable<List<OfferModel>>

    @Query("SELECT * FROM offer INNER JOIN offer_type_stored ON offer.id = offer_type_stored.offerId WHERE offer_type_stored.type = :type AND offer.categoryId = :categoryId")
    fun getOfferByCategoryId(
        categoryId: Int,
        type: OfferTypeStoredEnum = OfferTypeStoredEnum.OFFERS
    ): Single<OfferModel>

    @Query("SELECT * FROM offer INNER JOIN offer_type_stored ON offer.id = offer_type_stored.offerId WHERE offer_type_stored.type = :type AND isOnAuction = :isAuction")
    fun getAuctionOffers(
        isAuction: Boolean = true,
        type: OfferTypeStoredEnum = OfferTypeStoredEnum.OFFERS
    ): Single<List<OfferModel>>

    @Query("SELECT * FROM offer WHERE id == :offerId")
    fun getOfferById(offerId: Long): Flowable<OfferModel>

    @Query("DELETE FROM offer WHERE id = :offerId")
    fun deleteOffer(offerId: Long)

    @Query("DELETE FROM offer")
    fun clearOffers()

    // BY TYPE
    @Query("SELECT * FROM offer INNER JOIN offer_type_stored ON offer.id = offer_type_stored.offerId WHERE offer_type_stored.type = :type AND id == :offerId ORDER BY publishDate DESC")
    fun getOfferByType(
        offerId: Long,
        type: OfferTypeStoredEnum
    ): Flowable<OfferModel>

    @Query("SELECT * FROM offer INNER JOIN offer_type_stored ON offer.id = offer_type_stored.offerId WHERE offer_type_stored.type = :type ORDER BY publishDate DESC")
    fun getOffersByType(
        type: OfferTypeStoredEnum
    ): Flowable<List<OfferModel>>

    @Query("DELETE FROM offer WHERE id = :offerId AND id IN (SELECT offerId FROM offer_type_stored WHERE type = :type)")
    fun removeOfferByType(
        offerId: Long,
        type: OfferTypeStoredEnum
    )

}