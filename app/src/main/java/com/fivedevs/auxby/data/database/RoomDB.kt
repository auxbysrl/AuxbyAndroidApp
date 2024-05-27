package com.fivedevs.auxby.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class RoomDB(context: Context) {

    val appDatabase: AppDatabase

    init {
        val migrationFrom1To2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE chatRoom ADD COLUMN lastSeenMessageTime TEXT NOT NULL DEFAULT ''")
            }
        }

        appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(object : RoomDatabase.Callback() {})
                .addMigrations(migrationFrom1To2)
                .allowMainThreadQueries()
                .build()
    }

    companion object {
        private const val DATABASE_NAME = "auxby-db"
    }
}
