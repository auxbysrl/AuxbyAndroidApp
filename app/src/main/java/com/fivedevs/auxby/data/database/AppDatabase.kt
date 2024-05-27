package com.fivedevs.auxby.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fivedevs.auxby.data.database.dao.CategoryDao
import com.fivedevs.auxby.data.database.dao.ChatDao
import com.fivedevs.auxby.data.database.dao.NotificationsDao
import com.fivedevs.auxby.data.database.dao.UserDao
import com.fivedevs.auxby.data.database.dao.offer.*
import com.fivedevs.auxby.data.database.entities.*

@Database(
    entities = [User::class, Offer::class, OfferTypeStored::class, Category::class, CategoryDetails::class, ChatRoom::class,
               NotificationItem::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
    abstract fun offersDao(): OffersDao
    abstract fun categoryDao(): CategoryDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun offerTypeStoredDao(): OfferTypeStoredDao
}