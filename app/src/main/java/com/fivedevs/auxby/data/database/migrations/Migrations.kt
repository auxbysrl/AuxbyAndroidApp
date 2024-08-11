package com.fivedevs.auxby.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val migrationFrom1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE chatRoom ADD COLUMN lastSeenMessageTime TEXT NOT NULL DEFAULT ''")
        }
    }

    val migrationFrom2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new column with a default value
            db.execSQL("ALTER TABLE offer ADD COLUMN currencySymbol TEXT DEFAULT 'lei'")
        }
    }

    val migrationFrom3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new column with a default value
            db.execSQL("ALTER TABLE offer ADD COLUMN deepLink TEXT NOT NULL DEFAULT ''")
        }
    }

    val migrationFrom4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new columns with a default values
            db.execSQL("ALTER TABLE user ADD COLUMN isGoogleAccount INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE user ADD COLUMN isSubscribedToNewsletter INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE user ADD COLUMN rating REAL NOT NULL DEFAULT 0")
        }
    }
}