package com.fivedevs.auxby.data.api

import com.fivedevs.auxby.domain.models.ChatMessage
import com.fivedevs.auxby.domain.models.ChatMessageRequest
import com.fivedevs.auxby.data.database.entities.ChatRoom
import com.fivedevs.auxby.domain.models.ChatRoomIdRequest
import com.fivedevs.auxby.domain.models.enums.ApiTypeEnum
import com.fivedevs.auxby.domain.utils.Api
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {

    @GET("/api/v1/chat")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getChatRooms(): Observable<List<ChatRoom>>

    @GET("/api/v1/chat/{roomId}")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun getRoomMessages(@Path("roomId") roomId: Long): Observable<List<ChatMessage>>

    @POST("/api/v1/chat")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun createChatRoom(@Body chatRoomIdRequest: ChatRoomIdRequest): Observable<ChatRoom>

    @POST("/api/v1/chat/{roomId}")
    @Api(ApiTypeEnum.AUXBY_PLATFORM)
    fun sendChatMessage(
        @Path("roomId") roomId: Long,
        @Body chatMessageRequest: ChatMessageRequest
    ): Observable<List<ChatMessage>>
}