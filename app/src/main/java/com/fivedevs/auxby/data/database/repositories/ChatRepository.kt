package com.fivedevs.auxby.data.database.repositories

import androidx.room.Transaction
import com.fivedevs.auxby.data.database.AppDatabase
import com.fivedevs.auxby.data.database.entities.ChatRoom
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class ChatRepository @Inject constructor(private val appDatabase: AppDatabase) {

    @Transaction
    fun insertChatRooms(chatRooms: List<ChatRoom>) {
        appDatabase.chatDao().clearChatRooms()
        appDatabase.chatDao().insertChatRooms(chatRooms)
    }

    fun getUserChatRooms(): Flowable<List<ChatRoom>> {
        return appDatabase.chatDao().getUserChatRooms()
    }

    fun getChatRoomById(roomId: Long): Flowable<ChatRoom> {
        return appDatabase.chatDao().getChatRoomById(roomId)
    }

    fun updateChatRoomSeenStatus(roomId: Long, lastMessageTime: String) {
        appDatabase.chatDao().updateChatRoomSeenStatus(roomId, lastMessageTime)
    }
}