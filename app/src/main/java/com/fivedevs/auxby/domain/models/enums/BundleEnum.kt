package com.fivedevs.auxby.domain.models.enums

import com.fivedevs.auxby.R

enum class BundleEnum {
    SILVER {
        override fun getBackground(): Int = R.drawable.ic_silver_coin
    },
    GOLD {
        override fun getBackground(): Int = R.drawable.ic_gold_coin
    },
    DIAMOND {
        override fun getBackground(): Int = R.drawable.ic_diamond_coin
    };

    abstract fun getBackground(): Int
}