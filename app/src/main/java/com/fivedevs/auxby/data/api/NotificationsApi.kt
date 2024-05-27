package com.fivedevs.auxby.data.api

import com.fivedevs.auxby.data.database.entities.NotificationItem
import com.fivedevs.auxby.domain.models.enums.ApiTypeEnum
import com.fivedevs.auxby.domain.utils.Api
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface NotificationsApi {

    @GET("/api/v1/notifications")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getNotifications(): Observable<List<NotificationItem>>

    @DELETE("/api/v1/notifications/{notificationId}")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun deleteNotification(@Path("notificationId") notificationId: Long): Observable<Any>
}