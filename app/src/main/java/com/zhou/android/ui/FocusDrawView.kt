package com.zhou.android.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import com.zhou.android.R
import kotlinx.android.synthetic.main.layout_focus_draw.view.*

/**
 * Created by mxz on 2019/9/3.
 */
class FocusDrawView : RelativeLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val animator: AnimatorSet
    private val rotation: ObjectAnimator

    init {
        inflate(context, R.layout.layout_focus_draw, this)

        rotation = ObjectAnimator.ofFloat(focus, "rotation", 0f, 360f).apply {
            duration = 2200
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        animator = AnimatorSet().apply {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    circleView.innerRadius = 500f
                    rotation.start()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    focus.visibility = View.VISIBLE
                }
            })
            duration = 1200
            interpolator = LinearInterpolator()
        }
        animator.play(ObjectAnimator.ofFloat(focus, "scaleX", 0.2f, 1f))
                .with(ObjectAnimator.ofFloat(focus, "scaleY", 0.2f, 1f))
                .with(ObjectAnimator.ofFloat(focus, "rotation", 0f, 360f))

        animator.start()
    }

    override fun detachAllViewsFromParent() {
        if (animator.isRunning) {
            animator.cancel()
        }
        if (rotation.isRunning) {
            rotation.cancel()
        }
        super.detachAllViewsFromParent()
    }

    fun setBitmap(bitmap: Bitmap) {
        circleView.drawBitmap(bitmap)
    }
}