package com.fivedevs.auxby.data.database.repositories

import androidx.room.Transaction
import com.fivedevs.auxby.data.database.AppDatabase
import com.fivedevs.auxby.data.database.entities.NotificationItem
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class NotificationsRepository @Inject constructor(private val appDatabase: AppDatabase) {

    @Transaction
    fun insertNotifications(notifications: List<NotificationItem>) {
        appDatabase.notificationsDao().clearNotifications()
        appDatabase.notificationsDao().insertNotification(notifications)
    }

    fun getNotifications(): Flowable<List<NotificationItem>> {
        return appDatabase.notificationsDao().getUserNotifications()
    }

    fun removeNotificationById(id: Long) {
        return appDatabase.notificationsDao().removeNotificationById(id)
    }
}