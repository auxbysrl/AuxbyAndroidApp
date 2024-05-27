package com.fivedevs.auxby.domain.models.enums

import android.content.Context
import com.fivedevs.auxby.R

enum class LanguagesCodeEnum {
    EN {
        override fun getLanguageName(context: Context): String {
            return context.getString(R.string.text_english)
        }

        override fun getLowerCode(): String {
            return EN.name.lowercase()
        }
    },
    RO {
        override fun getLanguageName(context: Context): String {
            return context.getString(R.string.text_romanian)
        }

        override fun getLowerCode(): String {
            return RO.name.lowercase()
        }
    };

    abstract fun getLanguageName(context: Context): String
    abstract fun getLowerCode(): String
}