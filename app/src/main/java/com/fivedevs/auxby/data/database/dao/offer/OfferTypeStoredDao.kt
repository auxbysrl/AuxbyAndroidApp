package com.fivedevs.auxby.data.database.dao.offer

import androidx.room.*
import com.fivedevs.auxby.data.database.entities.OfferTypeStored
import com.fivedevs.auxby.domain.models.enums.OfferTypeStoredEnum

@Dao
interface OfferTypeStoredDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOffersWithTypes(offers: List<OfferTypeStored>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOfferWithTypes(offer: OfferTypeStored)

    @Query("DELETE FROM offer_type_stored")
    fun clearOffersTypeStored()

    @Query("DELETE FROM offer_type_stored WHERE type = :type")
    fun deleteOffersType(type: OfferTypeStoredEnum)

    @Query("DELETE FROM offer_type_stored WHERE type = :type and offerId=:offerId")
    fun deleteOfferType(
        offerId: Long,
        type: OfferTypeStoredEnum
    )
    @Query("DELETE FROM offer_type_stored WHERE offerId=:offerId")
    fun deleteOfferById(
        offerId: Long
    )

}