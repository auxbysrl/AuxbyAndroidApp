package com.fivedevs.auxby.domain.utils.buttonAnimator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.fivedevs.auxby.R

class TransitionButton : AppCompatButton {
    private val WIDTH_ANIMATION_DURATION = 200
    private val SCALE_ANIMATION_DURATION = 300
    private val SHAKE_ANIMATION_DURATION = 500
    private val COLOR_ANIMATION_DURATION = 350
    private var messageAnimationDuration = COLOR_ANIMATION_DURATION * 10
    private var currentState: State? = null
    private var isMorphingInProgress = false
    private var initialWidth = 0
    private var initialHeight = 0
    private var initialText: String? = null
    private var defaultColor = 0
    private var errorColor = 0
    private var loaderColor = 0
    private var progressCircularAnimatedDrawable: CircularAnimatedDrawable? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        currentState = State.IDLE
        errorColor = ContextCompat.getColor(getContext(), R.color.red)
        loaderColor = ContextCompat.getColor(getContext(), R.color.white)
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.color.colorAccent, typedValue, true)
        defaultColor = typedValue.data
        if (attrs != null) {
            val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.TransitionButton)
            val dc: CharSequence? = attrsArray.getString(R.styleable.TransitionButton_defaultColor)
            if (dc != null) defaultColor = ColorUtils.parse(dc.toString())
            val lc: CharSequence? = attrsArray.getString(R.styleable.TransitionButton_loaderColor)
            if (lc != null) loaderColor = ColorUtils.parse(lc.toString())
            attrsArray.recycle()
        }
        setBackground(false)
    }

    fun startAnimation() {
        currentState = State.PROGRESS
        isMorphingInProgress = true
        initialWidth = width
        initialHeight = height
        initialText = text.toString()
        text = null
        isClickable = false
        startWidthAnimation(initialHeight, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationCancel(animation)
                isMorphingInProgress = false
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentState == State.PROGRESS && !isMorphingInProgress) {
            drawIndeterminateProgress(canvas)
        }
    }

    private fun drawIndeterminateProgress(canvas: Canvas) {
        if (progressCircularAnimatedDrawable == null || !progressCircularAnimatedDrawable!!.isRunning) {
            val arcWidth = height / 18
            progressCircularAnimatedDrawable = CircularAnimatedDrawable(loaderColor, arcWidth.toFloat())
            val offset = (width - height) / 2
            val right = width - offset
            val bottom = height
            val top = 0
            progressCircularAnimatedDrawable?.setBounds(offset, top, right, bottom)
            progressCircularAnimatedDrawable?.callback = this
            setBackground(true)
            progressCircularAnimatedDrawable?.start()
        } else {
            setBackground(true)
            progressCircularAnimatedDrawable?.draw(canvas)
            invalidate()
        }
    }

    private fun setBackground(isRounded: Boolean) {
        var backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.transition_button_shape_idle)
        if (isRounded) {
            backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.transition_button_shape_circle)
        }
        background = backgroundDrawable
    }

    fun stopAnimation(
        stopAnimationStyle: StopAnimationStyle?,
        onAnimationStopEndListener: OnAnimationStopEndListener? = null
    ) {
        when (stopAnimationStyle) {
            StopAnimationStyle.SHAKE -> {
                currentState = State.ERROR
                isEnabled = false
                startWidthAnimation(initialWidth, object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        text = initialText
                        setBackground(false)
                        isEnabled = true
                        startShakeAnimation(object : AnimationListenerAdapter() {
                            override fun onAnimationEnd(animation: Animation) {
                                currentState = State.IDLE
                                isClickable = true
                                onAnimationStopEndListener?.onAnimationStopEnd()
                            }
                        })
                    }
                })
            }
            StopAnimationStyle.EXPAND -> {
                currentState = State.TRANSITION
                startScaleAnimation(object : AnimationListenerAdapter() {
                    override fun onAnimationEnd(animation: Animation) {
                        super.onAnimationEnd(animation)
                        onAnimationStopEndListener?.onAnimationStopEnd()
                    }
                })
            }
            StopAnimationStyle.SUCCESS -> {
                currentState = State.TRANSITION
                startWidthAnimation(initialWidth, object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        text = initialText
                        setBackground(false)
                    }
                })
            }
            else -> {}
        }
    }

    private fun startWidthAnimation(to: Int, onAnimationEnd: AnimatorListenerAdapter) {
        startWidthAnimation(width, to, onAnimationEnd)
    }

    private fun startWidthAnimation(from: Int, to: Int, onAnimationEnd: AnimatorListenerAdapter?) {
        val widthAnimation = ValueAnimator.ofInt(from, to)
        widthAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = layoutParams
            layoutParams.width = `val`
            setLayoutParams(layoutParams)
        }
        val animatorSet = AnimatorSet()
        animatorSet.duration = WIDTH_ANIMATION_DURATION.toLong()
        animatorSet.playTogether(widthAnimation)
        if (onAnimationEnd != null) animatorSet.addListener(onAnimationEnd)
        animatorSet.start()
    }

    private fun startShakeAnimation(animationListener: Animation.AnimationListener) {
        val shake = TranslateAnimation(0f, 15f, 0f, 0f)
        shake.duration = SHAKE_ANIMATION_DURATION.toLong()
        shake.interpolator = CycleInterpolator(4f)
        shake.setAnimationListener(animationListener)
        startAnimation(shake)
    }

    private fun startScaleAnimation(animationListener: Animation.AnimationListener) {
        val ts = (WindowUtils.getHeight(context) / height * 2.1).toFloat()
        val anim: Animation = ScaleAnimation(
            1f, ts,
            1f, ts,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.duration = SCALE_ANIMATION_DURATION.toLong()
        anim.fillAfter = true
        anim.setAnimationListener(animationListener)
        startAnimation(anim)
    }

    open inner class AnimationListenerAdapter : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}
        override fun onAnimationEnd(animation: Animation) {}
        override fun onAnimationRepeat(animation: Animation) {}
    }

    interface OnAnimationStopEndListener {
        fun onAnimationStopEnd()
    }

    private enum class State {
        PROGRESS, IDLE, ERROR, TRANSITION
    }

    enum class StopAnimationStyle {
        EXPAND, SHAKE, SUCCESS
    }
}