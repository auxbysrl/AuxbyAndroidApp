package com.fivedevs.auxby.data.api

import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.domain.models.*
import com.fivedevs.auxby.domain.models.enums.ApiTypeEnum
import com.fivedevs.auxby.domain.utils.Api
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import retrofit2.http.*
import java.util.*

interface UserApi {

    // GET routes
    @GET("/api/v1/user/email/check")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun checkEmailExists(@Query("email") email: String): Observable<Boolean>

    @GET("/api/v1/user")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getUser(): Observable<User>

    // POST routes
    @POST("/api/v1/auth/login")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun loginUser(@Body userLoginRequest: UserLoginRequest): Observable<LoginResponse>

    @POST("/api/v1/auth/googleAuth")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun googleAuth(@Body googleAuthRequest: LoginResponse): Observable<LoginResponse>

    @POST("/api/v1/auth/register")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun registerUser(@Body userRegisterRequest: UserRegisterRequest): Observable<Any>

    @POST("/api/v1/auth/send-confirmation-email")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun resendEmailVerification(@Query("email") email: String): Completable

    @POST("/api/v1/auth/send-reset-password")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun sendResetLink(@Query("email") email: String): Observable<Boolean>

    @Multipart
    @POST("/api/v1/user/avatar")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun uploadUserAvatar(@Part file: MultipartBody.Part): Observable<UserAvatarResponse>

    @POST("/api/v1/user/device-token")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun sendDeviceToken(@Body pushNotificationsSubscribe: PushNotificationsSubscribe): Observable<Any>

    // PUT routes
    @PUT("/api/v1/user")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun updateUser(@Body user: User): Observable<User>

    @PUT("/api/v1/user/password")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): Observable<Any>

    // Delete routes
    @DELETE("/api/v1/user")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun deleteUser(): Completable
}