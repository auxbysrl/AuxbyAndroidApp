package com.fivedevs.auxby.domain.models.enums

import android.content.Context
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.utils.extensions.capitalizeFirst

enum class OfferStateEnum {
    ACTIVE {
        override fun getStatusName(): String {
           return ACTIVE.name.lowercase().capitalizeFirst()
        }

        override fun getStateTitle(context: Context): String {
            return context.getString(R.string.active)
        }

        override fun getStateBackground(): Int {
            return R.drawable.bg_active_state_header
        }

        override fun getStateColor(): Int {
            return R.color.light_blue_994B86B4
        }

        override fun getStateIcon(): Int {
            return R.drawable.ic_active
        }
    },
    INACTIVE {
        override fun getStatusName(): String {
            return INACTIVE.name.lowercase().capitalizeFirst()
        }

        override fun getStateTitle(context: Context): String {
            return context.getString(R.string.inactive)
        }

        override fun getStateBackground(): Int {
            return R.drawable.bg_inactive_state_header
        }

        override fun getStateColor(): Int {
            return R.color.yellow_inactive
        }

        override fun getStateIcon(): Int {
            return R.drawable.ic_inactive_offer
        }
    },
    FINISHED {
        override fun getStatusName(): String {
            return FINISHED.name.lowercase().capitalizeFirst()
        }

        override fun getStateTitle(context: Context): String {
            return context.getString(R.string.finished)
        }

        override fun getStateBackground(): Int {
            return R.drawable.bg_finished_state_header
        }

        override fun getStateColor(): Int {
            return R.color.green_finished
        }

        override fun getStateIcon(): Int {
            return R.drawable.ic_finish
        }
    },
    INTERRUPTED {
        override fun getStatusName(): String {
            return INTERRUPTED.name.lowercase().capitalizeFirst()
        }

        override fun getStateTitle(context: Context): String {
            return context.getString(R.string.interrupted)
        }

        override fun getStateBackground(): Int {
            return R.drawable.bg_interrupted_state_header
        }

        override fun getStateColor(): Int {
            return R.color.red_interrupted
        }

        override fun getStateIcon(): Int {
            return R.drawable.ic_interrupted
        }
    };

    abstract fun getStatusName(): String
    abstract fun getStateTitle(context: Context): String
    abstract fun getStateBackground(): Int
    abstract fun getStateColor(): Int
    abstract fun getStateIcon(): Int
}

fun getStateColorByStatus(status: String): Int {
    val states = OfferStateEnum.values()
    return states.first { it.getStatusName().equals(status, true) }.getStateColor()
}

fun getStateIconByStatus(status: String): Int {
    val states = OfferStateEnum.values()
    return states.first { it.getStatusName().equals(status, true) }.getStateIcon()
}

fun getStateTitleByStatus(status: String, context: Context): String {
    val states = OfferStateEnum.values()
    return states.first { it.getStatusName().equals(status, true) }.getStateTitle(context)
}

fun getStateBackgroundByStatus(status: String): Int {
    val states = OfferStateEnum.values()
    return states.first { it.getStatusName().equals(status, true) }.getStateBackground()
}
