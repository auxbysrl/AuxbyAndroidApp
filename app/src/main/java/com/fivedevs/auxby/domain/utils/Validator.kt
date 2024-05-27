package com.fivedevs.auxby.domain.utils

import android.content.Context
import android.util.Patterns
import com.fivedevs.auxby.R

object Validator {

    fun validatePassword(text: String): Boolean {
        if (text.length < 8) {
            return false
        }
        if (!text.matches(".*[A-Z].*".toRegex())) {
            return false
        }
        if (!text.matches(".*[a-z].*".toRegex())) {
            return false
        }
        if (!text.matches(".*[0-9].*".toRegex())) {
            return false
        }
        return true
    }

    fun validateOptionalTextField(text: String, context: Context): String? {
       return if (text.isNotEmpty() && !text.contains(Regex("[A-Za-z]{2,30}+\$"))) {
            context.getString(R.string.invalid_field)
        } else {
            null
        }
    }

    fun validateTextField(text: String, context: Context): String? {
        return if (text.isEmpty()) {
            context.getString(R.string.cannot_be_empty)
        } else if (!text.contains(Regex("[A-Za-z]{2,30}+\$"))) {
            context.getString(R.string.invalid_field)
        } else {
            null
        }
    }

    fun validateEmailField(email: String, context: Context): String? {
        return if (email.isEmpty()) {
            context.getString(R.string.cannot_be_empty)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            context.getString(R.string.invalid_email_address)
        } else {
            null
        }
    }

    fun validatePhoneField(phoneNumber: String, context: Context): String? {
        return if (phoneNumber.isEmpty()) {
            context.getString(R.string.cannot_be_empty)
        } else if (!phoneNumber.contains(Regex("^\\d{10}\$"))) {
            context.getString(R.string.invalid_phone_number)
        } else {
            null
        }
    }
}