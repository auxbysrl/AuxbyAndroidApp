package com.fivedevs.auxby.data.database.repositories

import com.fivedevs.auxby.data.database.AppDatabase
import com.fivedevs.auxby.data.database.entities.User
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class UserRepository @Inject constructor(private val appDatabase: AppDatabase) {

    fun insertUser(user: User) {
        appDatabase.userDao().insertUser(user)
    }

    fun getCurrentUser(): Flowable<User> {
        return appDatabase.userDao().getCurrentUser()
    }

    fun updateUserAvatar(avatar: String) {
        appDatabase.userDao().updateUserAvatar(avatar)
    }

    fun clearUserData() {
        appDatabase.clearAllTables()
    }
}