package com.fivedevs.auxby.domain.models.enums

import android.content.Context
import com.fivedevs.auxby.R

enum class NotificationsTypeEnum {
    NEW_CHAT_STARTED {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(R.string.new_chat)
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(R.string.new_chat_description)
        }
    },
    BID_EXCEEDED {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(R.string.bid_position_lost)
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(R.string.bid_position_lost_description)
        }
    },
    NEW_MESSAGE_RECEIVED {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(R.string.new_message)
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(R.string.new_message_description)
        }
    },
    CLOSE_AUCTION {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(R.string.auction_closed)
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(R.string.auction_closed_description)
        }
    },
    AUCTION_ENDED {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(R.string.auction_finished)
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(R.string.auction_finished_owner)
        }
    },
    AUCTION_WON {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(R.string.auction_finished)
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(R.string.auction_finished_winner)
        }
    },
    AUCTION_INTERRUPTED {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(R.string.auction_finished)
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(R.string.auction_finished_winner)
        }
    },
    ACTION_FAILED {
        override fun getNotificationTitle(context: Context): String {
            return context.getString(R.string.auction_finished)
        }

        override fun getNotificationDescription(context: Context): String {
            return context.getString(R.string.auction_finished_winner)
        }
    };

    abstract fun getNotificationTitle(context: Context): String
    abstract fun getNotificationDescription(context: Context): String
}

fun getNotificationTitle(type: String, context: Context): String {
    return try {
        val notificationTypes = NotificationsTypeEnum.valueOf(type)
        notificationTypes.getNotificationTitle(context)
    } catch (exception: java.lang.Exception) {
        ""
    }
}

fun getNotificationDescription(type: String, context: Context): String {
    return try {
        val notificationTypes =  NotificationsTypeEnum.valueOf(type)
        notificationTypes.getNotificationDescription(context)
    } catch (exception: java.lang.Exception) {
        ""
    }
}