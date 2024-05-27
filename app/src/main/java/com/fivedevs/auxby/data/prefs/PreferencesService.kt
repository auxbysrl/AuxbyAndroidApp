package com.fivedevs.auxby.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.fivedevs.auxby.domain.utils.Constants.EMPTY
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesService @Inject constructor(@ApplicationContext context: Context) {

    private var masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private var encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        ENCRYPTED_SHARED_PREFS,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun <T> setValue(key: String, value: T) {
        val editor = encryptedSharedPreferences.edit()

        when (value) {
            is Boolean -> {
                editor.putBoolean(key, value as Boolean)
            }
            is String -> {
                editor.putString(key, value as String)
            }
            is Long -> {
                editor.putLong(key, value as Long)
            }
            is Int -> {
                editor.putInt(key, value as Int)
            }
        }

        editor.apply()
    }

    fun getUserToken(): String? {
        val userToken = encryptedSharedPreferences.getString(USER_TOKEN, EMPTY)
        if (!userToken.isNullOrEmpty()) {
            return userToken
        }
        return null
    }

    fun getTermsAndCond(): Boolean {
        return encryptedSharedPreferences.getBoolean(TERMS_AND_CONDITIONS, false)
    }

    fun getSelectedLanguageApp() = getString(LANGUAGE_APP_SELECTED).lowercase().ifEmpty { "ro" }

    fun getUserId(): String? {
        val userId = encryptedSharedPreferences.getString(USER_ID, "")
        if (!userId.isNullOrEmpty()) {
            return userId
        }
        return null
    }

    fun getOnBoardingStatus() = getBoolean(ON_BOARDING_KEY)

    fun clearUserDetails() {
        encryptedSharedPreferences.edit().remove(USER_ID).apply()
        encryptedSharedPreferences.edit().remove(USER_TOKEN).apply()
        encryptedSharedPreferences.edit().remove(IS_GOOGLE_ACCOUNT).apply()
    }

    fun isUserLoggedIn() = !getUserToken().isNullOrEmpty()

    fun isGoogleAccount() = getBoolean(IS_GOOGLE_ACCOUNT)

    private fun getBoolean(key: String): Boolean {
        return encryptedSharedPreferences.getBoolean(key, false)
    }

    private fun getInt(key: String): Int {
        return encryptedSharedPreferences.getInt(key, 0)
    }

    private fun getString(key: String): String {
        return encryptedSharedPreferences.getString(key, EMPTY).orEmpty()
    }

    fun canReceiveNotifications(): Boolean {
        return encryptedSharedPreferences.getBoolean(PUSH_NOTIFICATIONS, false)
    }

    companion object {
        const val USER_ID = "USER_ID"
        const val USER_TOKEN = "USER_TOKEN"
        const val IS_GOOGLE_ACCOUNT = "IS_GOOGLE_ACCOUNT"
        const val ON_BOARDING_KEY = "ON_BOARDING_KEY"
        const val PUSH_NOTIFICATIONS = "PUSH_NOTIFICATIONS"
        const val TERMS_AND_CONDITIONS = "TERMS_AND_CONDITIONS"
        const val LANGUAGE_APP_SELECTED = "LANGUAGE_APP_SELECTED"
        const val ENCRYPTED_SHARED_PREFS = "encrypted_shared_prefs"
    }
}