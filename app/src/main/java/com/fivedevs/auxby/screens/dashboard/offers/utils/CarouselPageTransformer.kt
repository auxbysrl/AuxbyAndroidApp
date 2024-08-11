package com.fivedevs.auxby.screens.dashboard.offers.utils


import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.fivedevs.auxby.R

class CarouselPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        val minScale = 0.8f
        val maxScale = 1.0f
        val scaleFactor = when {
            position < -1 -> minScale
            position <= 1 -> {
                minScale + (maxScale - minScale) * (1 - kotlin.math.abs(position))
            }

            else -> minScale
        }

        view.scaleX = scaleFactor
        view.scaleY = scaleFactor
        view.translationZ = if (position == 0f) 1f else 0f // Bring the middle item to the front


        // Add margin between items
        val pageMargin = view.resources.getDimensionPixelOffset(R.dimen.pageMargin).toFloat()
        val offsetPx = view.resources.getDimensionPixelOffset(R.dimen.offset).toFloat()
        val offset = position * -(2 * offsetPx + pageMargin)
        view.translationX = offset
    }
}