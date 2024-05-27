package com.fivedevs.auxby.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fivedevs.auxby.data.database.entities.ChatRoom
import io.reactivex.rxjava3.core.Flowable

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatRooms(chatRooms: List<ChatRoom>)

    @Query("SELECT * FROM chatRoom")
    fun getUserChatRooms(): Flowable<List<ChatRoom>>

    @Query("SELECT * FROM chatRoom WHERE roomId=:roomId")
    fun getChatRoomById(roomId: Long): Flowable<ChatRoom>

    @Query("DELETE FROM chatRoom")
    fun clearChatRooms()

    @Query("UPDATE chatRoom SET lastSeenMessageTime=:lastMessageTime WHERE roomId=:roomId")
    fun updateChatRoomSeenStatus(roomId: Long, lastMessageTime: String)
}