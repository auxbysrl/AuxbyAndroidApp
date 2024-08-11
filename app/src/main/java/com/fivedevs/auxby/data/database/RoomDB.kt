package com.fivedevs.auxby.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fivedevs.auxby.data.database.migrations.Migrations.migrationFrom1To2
import com.fivedevs.auxby.data.database.migrations.Migrations.migrationFrom2To3
import com.fivedevs.auxby.data.database.migrations.Migrations.migrationFrom3To4
import com.fivedevs.auxby.data.database.migrations.Migrations.migrationFrom4To5

class RoomDB(context: Context) {

    val appDatabase: AppDatabase

    init {
        appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .addCallback(object : RoomDatabase.Callback() {})
            .addMigrations(migrationFrom1To2, migrationFrom2To3, migrationFrom3To4, migrationFrom4To5)
            .allowMainThreadQueries()
            .build()
    }

    companion object {
        private const val DATABASE_NAME = "auxby-db"
    }
}
