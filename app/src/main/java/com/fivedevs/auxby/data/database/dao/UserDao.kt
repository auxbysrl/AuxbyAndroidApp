package com.fivedevs.auxby.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fivedevs.auxby.data.database.entities.User
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("SELECT * FROM user")
    fun getCurrentUser(): Flowable<User>

    @Query("SELECT * FROM user WHERE email=:email")
    fun getUserByEmail(email: String): Observable<User>

    @Query("UPDATE user SET avatar=:avatar")
    fun updateUserAvatar(avatar: String)

    @Query("DELETE FROM user")
    fun clearUserData()
}