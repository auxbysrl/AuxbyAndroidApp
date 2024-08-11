package com.fivedevs.auxby.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0,
    val date: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val offerId: Long = 0,
    val userId: Long = 0,
)
