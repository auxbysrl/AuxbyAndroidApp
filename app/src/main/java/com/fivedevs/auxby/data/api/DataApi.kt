package com.fivedevs.auxby.data.api

import com.fivedevs.auxby.data.api.response.BidActionResponse
import com.fivedevs.auxby.data.api.response.DeepLink
import com.fivedevs.auxby.data.database.entities.Category
import com.fivedevs.auxby.data.database.entities.CategoryDetails
import com.fivedevs.auxby.domain.models.*
import com.fivedevs.auxby.domain.models.enums.ApiTypeEnum
import com.fivedevs.auxby.domain.utils.Api
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import retrofit2.http.*

interface DataApi {

    // GET routes
    @GET("/api/v1/product/category")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getAllCategories(): Observable<List<Category>>

    @GET("/api/v1/product")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getOffers(
        @QueryMap filters: Map<String, String>
    ): Observable<OffersResponse>

    @GET("/api/v1/product/promoted")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getPromotedOffers(): Observable<List<OfferModel>>

    @GET("/api/v1/category/details")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getCategoriesDetails(): Observable<List<CategoryDetails>>

    @GET("/api/v1/product/{id}")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getOfferById(@Path("id") offerId: Long, @Query("increaseView") increaseView: Boolean = false): Observable<OfferModel>

    @GET("/api/v1/product/getByIds")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getOfferByIds(@Query("id") taskIds: List<Long>): Single<List<OfferModel>>

    @GET("/api/v1/product/favorites")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getSavedOffers(): Observable<List<OfferModel>>

    @GET("/api/v1/product/user/my-offers")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getMyOffers(): Observable<List<OfferModel>>

    @GET("/api/v1/product/search-summary")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getSearchSuggestions(@Query("offerTitle") searchText: String): Observable<CategoryResult>

    @GET("/api/v1/application/currencies")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getAllCurrencies(): Observable<List<CurrencyModel>>

    // POST routes
    @POST("/api/v1/product")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun addOffer(@Body offerRequestModel: OfferRequestModel): Observable<OfferModel>

    @Multipart
    @POST("/api/v1/product/upload/{id}")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun uploadOfferPhotos(
        @Path("id") id: String, @Part files: Array<MultipartBody.Part>
    ): Observable<Any>

    @POST("/api/v1/product/{offerId}/favorite")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun saveOfferToFavorites(
        @Path("offerId") id: Long, @Body offerModel: OfferModel
    ): Observable<Any>

    @POST("/api/v1/product/search")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getSearchOffers(@Body searchOffersModel: AdvancedFiltersModel): Observable<List<OfferModel>>

    @POST("/api/v1/product/{offerId}/promote")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun promoteCurrentOffer(@Path("offerId") offerId: Long, @Body offerPromoteOfferRequest: PromoteOfferRequest): Observable<Any>

    // PUT routes
    @PUT("/api/v1/product/{id}")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun updateOfferById(
        @Path("id") offerId: Long, @Body offerRequestModel: OfferRequestModel
    ): Observable<Any>

    @PUT("/api/v1/product/{offerId}/changeStatus")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun changeOfferStatus(
        @Path("offerId") offerId: Long,
        @Body requiredCoinsModel: RequiredCoinsModel
    ): Observable<Any>

    // DELETE routes
    @DELETE("/api/v1/product/{id}")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun deleteOfferById(@Path("id") offerId: Long): Observable<Any>

    // bid-controller
    @GET("/api/v1/product/my-bids/offers")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getMyBids(): Observable<List<OfferModel>>

    @POST("/api/v1/product/bid")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun placeBid(@Body placeBidModel: PlaceBidModel): Observable<BidActionResponse>

    @GET("/api/v1/product/generate-deep-link/{id}")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getOfferDeepLink(@Path("id") offerId: Long): Observable<DeepLink>

    @GET("/api/v1/ads")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getAppAds(): Observable<List<AdsModel>>

}