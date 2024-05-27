package com.fivedevs.auxby.domain.utils.extensions

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.DateUtils.Companion.FORMAT_DD_MMM_YYYY
import com.fivedevs.auxby.domain.utils.DateUtils.Companion.currentLocale
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import com.tapadoo.alerter.Alerter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachedToRoot: Boolean = false): View = LayoutInflater.from(context).inflate(layoutRes, this, attachedToRoot)

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.enableClick() {
    this.isEnabled = true
}

fun View.disableClick() {
    this.isEnabled = false
}

fun View.alphaShow() {
    this.alpha = 1f
}

fun View.alphaHide() {
    this.alpha = 0f
}

fun View.showWithAnimation(duration: Long = 150) {
    val autoTransition = AutoTransition()
    autoTransition.duration = duration
    TransitionManager.beginDelayedTransition(this.rootView as ViewGroup, autoTransition)
    this.visibility = View.VISIBLE
}

fun View.hideWithAnimation(duration: Long = 150) {
    val autoTransition = AutoTransition()
    autoTransition.duration = duration
    TransitionManager.beginDelayedTransition(this.rootView as ViewGroup, autoTransition)
    this.visibility = View.GONE
}

fun View.invisibleWithAnimation(duration: Long = 150) {
    val autoTransition = AutoTransition()
    autoTransition.duration = duration
    TransitionManager.beginDelayedTransition(this.rootView as ViewGroup, autoTransition)
    this.visibility = View.INVISIBLE
}

fun View.setOnClickListenerWithDelay(duration: Long = 2000L, function: () -> Unit) {
    this.setOnClickListener {
        this.isClickable = false
        function()
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            this.isClickable = true
        }, duration)
    }
}

fun View.hideKeyboard() {
    val inputMethod = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    val inputMethod = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

inline fun FragmentManager.transaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commitAllowingStateLoss()
}

inline fun FragmentManager.transactionWithBackStack(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().addToBackStack("").commitAllowingStateLoss()
}

fun AppCompatActivity.add(fragment: Fragment, container: Int) {
    supportFragmentManager.transaction { add(container, fragment) }
}

fun AppCompatActivity.replace(fragment: Fragment, container: Int) {
    supportFragmentManager.transaction { replace(container, fragment) }
}

fun AppCompatActivity.addBackStack(fragment: Fragment, container: Int) {
    supportFragmentManager.transactionWithBackStack { add(container, fragment) }
}

fun AppCompatActivity.replaceBackStack(fragment: Fragment, container: Int) {
    supportFragmentManager.transactionWithBackStack { replace(container, fragment) }
}

fun AppCompatActivity.show(activeFragment: Fragment, fragment: Fragment) {
    supportFragmentManager.transaction { hide(activeFragment).show(fragment) }
}

fun AppCompatActivity.hide(fragment: Fragment) {
    supportFragmentManager.transaction { hide(fragment) }
}

fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
    val scrollDuration = 1000f
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return scrollDuration / computeHorizontalScrollRange()
        }
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

fun View.expand(duration: Long = 500L) {
    val rotateAnimation = RotateAnimation(
        179.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )
    rotateAnimation.interpolator = DecelerateInterpolator()
    rotateAnimation.repeatCount = 0
    rotateAnimation.duration = duration
    rotateAnimation.fillAfter = true
    this.startAnimation(rotateAnimation)
}

fun View.collapse(duration: Long = 500L) {
    val rotateAnimation = RotateAnimation(
        0.0f, 179.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )
    rotateAnimation.interpolator = DecelerateInterpolator()
    rotateAnimation.repeatCount = 0
    rotateAnimation.duration = duration
    rotateAnimation.fillAfter = true
    this.startAnimation(rotateAnimation)
}

fun ImageView.setTint(@ColorRes colorRes: Int) {
    ImageViewCompat.setImageTintList(
        this, ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
    )
}

fun View.makeMeShake(duration: Long = 30, offset: Float = 6f) {
    val anim = TranslateAnimation(-offset, offset, 0f, 0f)
    anim.duration = duration
    anim.repeatMode = Animation.REVERSE
    anim.repeatCount = 5
    this.startAnimation(anim)
}

fun MaterialShapeDrawable.setCornerRadius(radius: Float) {
    shapeAppearanceModel =
        this.shapeAppearanceModel.toBuilder().setTopRightCorner(CornerFamily.ROUNDED, radius).setTopLeftCorner(CornerFamily.ROUNDED, radius).build()
}

fun TextInputEditText.transformIntoDatePicker(
    context: Context, format: String = FORMAT_DD_MMM_YYYY, maxDate: Date? = null
) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false
    Locale.setDefault(currentLocale)

    val myCalendar = Calendar.getInstance()
    myCalendar.add(Calendar.DAY_OF_MONTH, 1) // Set the minimum date to tomorrow

    // Calculate the maximum date (3 months from now)
    val maxCalendar = Calendar.getInstance()
    maxCalendar.add(Calendar.DAY_OF_MONTH, 1)
    maxCalendar.add(Calendar.MONTH, 3)

    // Calculate the minimum date (starting from tomorrow)
    val minCalendarDate = Calendar.getInstance()
    minCalendarDate.add(Calendar.DAY_OF_MONTH, 1)

    val datePickerOnDataSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, monthOfYear)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        setText(sdf.format(myCalendar.time))
    }

    setOnClickListenerWithDelay {
        DatePickerDialog(
            context,
            R.style.DatePickerDialogTheme,
            datePickerOnDataSetListener,
            myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        ).run {

            // Set the minimum date to tomorrow
            datePicker.minDate = minCalendarDate.timeInMillis
            datePicker.maxDate = maxCalendar.timeInMillis
            show()
        }
    }
}

fun Activity.showInternetConnectionDialog(isConnected: Boolean) {
    Alerter.create(this, R.layout.item_alert_internet_connection).setDuration(Constants.DELAY_3_SECONDS).enableSwipeToDismiss()
        .setBackgroundColorRes(android.R.color.transparent).also { alerter ->
            alerter.getLayoutContainer()?.let {
                it.findViewById<ImageView>(R.id.messageIcon).setImageResource(if (isConnected) R.drawable.ic_check_active else R.drawable.ic_info_red)
                it.findViewById<TextView>(R.id.messageInfo).setText(if (isConnected) R.string.internet_connected else R.string.no_internet_connection)
                it.findViewById<TextView>(R.id.messageSubtitleInfo)
                    .setText(if (isConnected) R.string.internet_connected_subtitle else R.string.check_your_connection_and_try_again)
            }
        }.show()
}

fun ViewPager2.autoScroll(carouselHandler: Handler) {
    val delayMillis = 3000L

    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            carouselHandler.removeCallbacksAndMessages(null)
            carouselHandler.postDelayed({
                val currentItem = this@autoScroll.currentItem
                val itemCount = this@autoScroll.adapter?.itemCount ?: 0
                if (itemCount > 0) {
                    val nextItem = (currentItem + 1) % itemCount
                    this@autoScroll.setCurrentItem(nextItem, true)
                }
            }, delayMillis)
        }
    })
}