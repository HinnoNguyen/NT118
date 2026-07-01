package com.example.mobileapp.util

import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator

object AnimationUtils {

    fun View.setBounceClick() {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    v.performClick()
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            true
        }
    }

    fun View.slideUp(duration: Long = 400, delay: Long = 0) {
        this.visibility = View.VISIBLE
        this.alpha = 0f
        this.translationY = 50f
        this.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun View.popIn(duration: Long = 300) {
        this.alpha = 0f
        this.scaleX = 0.8f
        this.scaleY = 0.8f
        this.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun View.pulse(duration: Long = 1000) {
        this.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(duration / 2)
            .withEndAction {
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration / 2)
                    .withEndAction { pulse(duration) }
                    .start()
            }
            .start()
    }
}
