package com.fivedevs.auxby.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fivedevs.auxby.data.database.entities.NotificationItem
import io.reactivex.rxjava3.core.Flowable

@Dao
interface NotificationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotification(notification: List<NotificationItem>)

    @Query("SELECT * FROM notifications")
    fun getUserNotifications(): Flowable<List<NotificationItem>>

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    fun removeNotificationById(notificationId: Long)

    @Query("DELETE FROM notifications")
    fun clearNotifications()
}