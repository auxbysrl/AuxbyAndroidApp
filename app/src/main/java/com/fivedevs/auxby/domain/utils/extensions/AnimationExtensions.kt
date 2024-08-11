package com.fivedevs.auxby.domain.utils.extensions

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ActivityOptions
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import androidx.core.content.ContextCompat
import com.fivedevs.auxby.R
import com.github.leandroborgesferreira.loadingbutton.animatedDrawables.ProgressType
import com.github.leandroborgesferreira.loadingbutton.customViews.ProgressButton
import kotlin.math.hypot

fun ProgressButton.animDone(
    context: Context,
    fillColor: Int = ContextCompat.getColor(context, R.color.colorPrimary),
    bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, com.github.leandroborgesferreira.loadingbutton.R.drawable.ic_done_white_48dp)
) {
    progressType = ProgressType.INDETERMINATE
    doneLoadingAnimation(fillColor, bitmap)
}

fun View.circularReveal(animationDuration: Long, color: Int) {
    val width = Resources.getSystem().displayMetrics.widthPixels
    val height = Resources.getSystem().displayMetrics.heightPixels
    val cx = width / 2
    val cy = height - 100

    val radius = hypot(right.toDouble(), bottom.toDouble()).toInt()
    this.setBackgroundColor(ContextCompat.getColor(context, color))
    ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, radius.toFloat()).apply {
        interpolator = null
        duration = animationDuration
        start()
    }

    this.postDelayed({
        this.animateChangeBackgroundColor(ContextCompat.getColor(context, color), Color.WHITE)
    }, animationDuration - 100)
}

fun View.animateChangeBackgroundColor(from: Int, to: Int, animationDuration: Long = 300) {
    val anim = ValueAnimator()
    anim.setIntValues(from, to)
    anim.setEvaluator(ArgbEvaluator())
    anim.addUpdateListener { valueAnimator -> this.setBackgroundColor(valueAnimator.animatedValue as Int) }
    anim.duration = animationDuration
    anim.start()
}

//Start an activity from a specific point with reveal animation.
fun View.revealBundle(): Bundle? {
    var opts: ActivityOptions? = null
    val left = 0
    val top = 0
    val width = this.measuredWidth
    val height = this.measuredHeight
    opts = ActivityOptions.makeClipRevealAnimation(this, left, top, width, height)
    return opts?.toBundle()
}