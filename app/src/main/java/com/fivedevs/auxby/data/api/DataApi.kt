package com.fivedevs.auxby.data.api

import com.fivedevs.auxby.data.api.response.BidActionResponse
import com.fivedevs.auxby.data.database.entities.Category
import com.fivedevs.auxby.data.database.entities.CategoryDetails
import com.fivedevs.auxby.domain.models.*
import com.fivedevs.auxby.domain.models.enums.ApiTypeEnum
import com.fivedevs.auxby.domain.utils.Api
import com.fivedevs.auxby.domain.utils.NoTokenNeed
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

    @GET("/api/v1/category/details")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getCategoriesDetails(): Observable<List<CategoryDetails>>

    @NoTokenNeed
    @GET("/api/v1/bundle")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getBundles(): Observable<MutableList<CoinBundle>>

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

    @POST("/api/v1/product/{offerId}/report")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun reportOffer(
        @Path("offerId") offerId: Long, @Body reportOfferModel: ReportOfferModel
    ): Observable<Any>

    @POST("/api/v1/product/search")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun getSearchOffers(@Body searchOffersModel: AdvancedFiltersModel): Observable<List<OfferModel>>

    // PUT routes
    @PUT("/api/v1/product/{id}")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun updateOfferById(
        @Path("id") offerId: Long, @Body offerRequestModel: OfferRequestModel
    ): Observable<Any>

    @PUT("/api/v1/product/{offerId}/changeStatus")
    @Api(ApiTypeEnum.AUXBY_OFFER_MANAGEMENT)
    fun changeOfferStatus(@Path("offerId") offerId: Long,
                          @Body requiredCoinsModel: RequiredCoinsModel): Observable<Any>

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
}