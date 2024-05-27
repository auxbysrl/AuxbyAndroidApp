package com.fivedevs.auxby.domain.models.enums

import android.content.Context
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.utils.extensions.capitalizeFirst

enum class ConditionTypeEnum() {
    NEW {
        override fun getConditionName(): String {
            return name.lowercase().capitalizeFirst()
        }

        override fun getConditionTranslatedName(context: Context): String {
            return context.resources.getString(R.string.condition_new)
        }
    },
    USED {
        override fun getConditionName(): String {
            return name.lowercase().capitalizeFirst()
        }

        override fun getConditionTranslatedName(context: Context): String {
            return context.resources.getString(R.string.condition_used)
        }
    };

    abstract fun getConditionName(): String
    abstract fun getConditionTranslatedName(context: Context): String
}