package com.fivedevs.auxby.domain.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.LocaleList
import java.util.Locale

class LocalizationHandler(base: Context?) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            var localContext = context

            // Get the current configuration
            val config = localContext.resources.configuration

            // Apply language change
            val locale = Locale(language)
            Locale.setDefault(locale)

            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            localContext = localContext.createConfigurationContext(config)

            return LocalizationHandler(localContext)
        }
    }
}
