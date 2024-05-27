package com.fivedevs.auxby.domain.utils.buttonAnimator

import android.graphics.Color

object ColorUtils {

    private fun correct(color: String): String {
        var mColor = color
        mColor = mColor.replace("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])".toRegex(), "#$1$1$2$2$3$3")
        return mColor
    }

    fun parse(color: String): Int {
        var mColor = color
        when (mColor.length) {
            4 -> {
                mColor = mColor.replace("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])".toRegex(), "#$1$1$2$2$3$3")
                mColor =
                    mColor.replace("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])".toRegex(), "#$2$2$3$3$4$4")
            }
            5 -> mColor =
                mColor.replace("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])".toRegex(), "#$2$2$3$3$4$4")
        }
        return Color.parseColor(mColor)
    }
}