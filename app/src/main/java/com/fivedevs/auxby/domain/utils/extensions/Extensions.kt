package com.fivedevs.auxby.domain.utils.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.text.Html
import android.text.Layout
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.models.LabelModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

fun Context.getLanguageCode(): String = this.resources.configuration.locales[0].language.ifEmpty { "ro" }

fun Context.toast(text: CharSequence) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
fun Activity.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Fragment.toast(message: String) = Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()

fun Context.longToast(text: CharSequence) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
fun Activity.longToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
fun Fragment.longToast(message: String) = Toast.makeText(this.context, message, Toast.LENGTH_LONG).show()

inline fun <reified T> Context.jsonToClass(@RawRes resourceId: Int): T =
    Gson().fromJson(resources.openRawResource(resourceId).bufferedReader().use { it.readText() }, T::class.java)

fun String.capitalizeFirst(): String = this.substring(0, 1).uppercase() + this.substring(1)

inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)

inline fun <reified T : Any> Activity.launchActivityWithFinish(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivityForResult(intent, requestCode, options)
    finish()
}

inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivityForResult(intent, requestCode, options)
}

inline fun <reified T : Any> Context.launchActivity(
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivity(intent, options)
}

inline fun <reified T : Any> Context.launchActivityNewTask(
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent, options)
}

fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

fun Context.getDrawableCompat(@DrawableRes drawable: Int) = ContextCompat.getDrawable(this, drawable)

fun TextView.htmlText(text: String) {
    setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
}

internal fun Snackbar.action(message: String, action: (View) -> Unit) {
    this.setAction(message, action)
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

fun List<LabelModel>.getName(context: Context): String {
    return this.firstOrNull { it.language.equals(context.getLanguageCode(), true) }?.translation.orEmpty()
}

fun <T> concatenate(vararg lists: List<T>): List<T> {
    val result: MutableList<T> = ArrayList()
    for (list in lists) {
        result.addAll(list)
    }
    return result
}

fun Activity.makeStatusBarTransparent() {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        statusBarColor = Color.TRANSPARENT
    }
}

fun View.setMarginTop(marginTop: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(0, marginTop, 0, 0)
    this.layoutParams = menuLayoutParams
}

fun Layout?.isTextEllipsized(): Boolean {
    if (this != null) {
        val lines: Int = this.lineCount
        if (lines > 0) {
            return this.getEllipsisCount(lines - 1) > 0
        }
    }
    return false
}

@SuppressLint("CheckResult")
fun ImageView.loadImage(url: String, placeholder: Int = R.drawable.ic_placeholder, circleCrop: Boolean = false) =
    Glide.with(this).load(url).let { request ->
        request.diskCacheStrategy(DiskCacheStrategy.ALL)
        if (circleCrop) {
            request.circleCrop()
        }
        request.placeholder(placeholder)
        request.error(placeholder)
        request.encodeQuality(70)
        request.into(this)
    }

@SuppressLint("CheckResult")
fun ImageView.loadImageResource(imageResource: Int, placeholder: Int = R.drawable.ic_placeholder, circleCrop: Boolean = false) =
    Glide.with(this).load(imageResource).let { request ->
        request.diskCacheStrategy(DiskCacheStrategy.ALL)
        if (circleCrop) {
            request.circleCrop()
        }
        request.placeholder(placeholder)
        request.into(this)
    }

@SuppressLint("CheckResult")
fun ImageView.loadImageResource(imageResource: Int) = Glide.with(this).load(imageResource).into(this)

fun String.toDecimalsOnly(): String = this.replace(Regex("\\D"), "")

fun ViewPager2.findFragmentAtPosition(
    fragmentManager: FragmentManager,
    position: Int
): Fragment? {
    return fragmentManager.findFragmentByTag("f$position")
}

@SuppressLint("HardwareIds")
fun Context.deviceId(): String = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)


// Used to prevent accidental emitting of a liveData value when the fragment is resumed
fun LifecycleOwner.isResumedOrLater(action: () -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        action.invoke()
    }
}