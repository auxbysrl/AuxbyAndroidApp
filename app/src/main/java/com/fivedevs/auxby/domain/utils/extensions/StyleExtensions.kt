package com.fivedevs.auxby.domain.utils.extensions

import android.app.Activity
import android.graphics.Color
import androidx.annotation.ColorInt
import qiu.niorgai.StatusBarCompat

fun Activity.setDarkStatusBar() {
    StatusBarCompat.setStatusBarColor(this, Color.BLACK)
}

fun Activity.setTransparentStatusBar() {
    StatusBarCompat.setStatusBarColor(this, Color.TRANSPARENT)
}

fun Activity.setWhiteStatusBar() {
    StatusBarCompat.setStatusBarColor(this, Color.WHITE)
    StatusBarCompat.changeToLightStatusBar(this)
}

fun Activity.setStatusBarColor(@ColorInt color: Int, switchToLightStatusBar: Boolean = true) {
    StatusBarCompat.setStatusBarColor(this, color)
    if (switchToLightStatusBar)
        StatusBarCompat.changeToLightStatusBar(this)
}
