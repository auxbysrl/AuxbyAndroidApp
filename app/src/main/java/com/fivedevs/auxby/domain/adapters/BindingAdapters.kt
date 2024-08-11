package com.fivedevs.auxby.domain.adapters

import android.text.Html
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import javax.annotation.Nullable


@BindingAdapter("android:onDelayClick")
fun onDelayClick(view: View, onClickMethod: () -> Unit) {
    view.setOnClickListenerWithDelay { onClickMethod() }
}

@BindingAdapter("android:onDelayClick", "android:delayDuration")
fun onDelayClick(view: View, onClickMethod: () -> Unit, delay: Long) {
    view.setOnClickListenerWithDelay(delay) { onClickMethod() }
}

@BindingAdapter("android:scaleAnimationVisibility")
fun setScaleAnimationVisibility(target: View, isVisible: Boolean) {
    TransitionManager.beginDelayedTransition(target.rootView as ViewGroup)
    target.visibility = if (isVisible) View.GONE else View.VISIBLE
}

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, @Nullable url: String?) {
    Glide.with(view.context)
        .load(url.orEmpty())
        .placeholder(R.drawable.ic_placeholder)
        .centerCrop()
        .into(view)
}

@BindingAdapter("htmlText")
fun setHtmlText(view: TextView, htmlText: String?) {
    if (htmlText != null) {
        view.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    }
}