package com.fivedevs.auxby.domain.models.enums

enum class DialogTypes {
    DEFAULT {
        override fun getType(): Int = 0
    },
    LOG_OUT {
        override fun getType(): Int = 1
    },
    DELETE_ACCOUNT {
        override fun getType(): Int = 2
    },
    PUBLISH_OFFER {
        override fun getType(): Int = 3
    },
    PUBLISH_OFFER_NO_COINS {
        override fun getType(): Int = 4
    },
    CLOSE_AUCTION {
        override fun getType(): Int = 5
    },
    DELETE_OFFER {
        override fun getType(): Int = 6
    },
    DISABLE_OFFER {
        override fun getType(): Int = 7
    },
    ENABLE_OFFER {
        override fun getType(): Int = 8
    },
    CONTACT_US {
        override fun getType(): Int = 9
    };

    abstract fun getType(): Int
}