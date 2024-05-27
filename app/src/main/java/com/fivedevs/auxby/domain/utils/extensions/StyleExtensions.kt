package com.fivedevs.auxby.domain.utils.extensions

import android.app.Activity
import android.graphics.Color
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
