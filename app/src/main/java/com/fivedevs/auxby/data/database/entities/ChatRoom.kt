package com.fivedevs.auxby.data.database.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "chatRoom")
data class ChatRoom(
    @PrimaryKey(autoGenerate = false)
    val roomId: Long = 0,
    val lastMessageTime: String = "",
    val guest: String = "",
    val host: String = "",
    val roomName: String = "",
    val offerId: Long = 0,
    val chatImage: String = "",
    val isRoomHost: Boolean = false,
    var lastSeenMessageTime: String = ""
): Parcelable
