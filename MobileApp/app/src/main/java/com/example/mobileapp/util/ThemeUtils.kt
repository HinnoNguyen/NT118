package com.example.mobileapp.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import kotlin.math.hypot

object ThemeUtils {
    private var themeScreenshot: Bitmap? = null
    private var startX: Int = 0
    private var startY: Int = 0
    private var lastToggleTime: Long = 0

    fun toggleTheme(activity: Activity, viewToCapture: View?, triggerView: View, isDarkMode: Boolean, animate: Boolean = true, onThemeApplied: (() -> Unit)? = null): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToggleTime < 5000) {
            // Rate limit: 5 seconds between toggles
            return false
        }
        lastToggleTime = currentTime

        if (!animate || viewToCapture == null || viewToCapture.width <= 0 || viewToCapture.height <= 0) {
            applyThemeWithoutAnimation(activity, isDarkMode, onThemeApplied)
            return true
        }

        try {
            // Capture the current screen state
            val width = viewToCapture.width
            val height = viewToCapture.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            viewToCapture.draw(canvas)
            themeScreenshot = bitmap
        } catch (t: Throwable) {
            themeScreenshot = null
            applyThemeWithoutAnimation(activity, isDarkMode, onThemeApplied)
            return true
        }
        
        val location = IntArray(2)
        triggerView.getLocationInWindow(location)
        startX = location[0] + triggerView.width / 2
        startY = location[1] + triggerView.height / 2

        // Transform animation: Deep scale and fade to hide the "gap"
        viewToCapture.animate()
            .scaleX(0.90f)
            .scaleY(0.90f)
            .alpha(0f)
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                if (!activity.isFinishing) {
                    applyThemeWithoutAnimation(activity, isDarkMode, onThemeApplied)
                }
            }
            .start()
        
        return true
    }

    private fun applyThemeWithoutAnimation(activity: Activity, isDarkMode: Boolean, onThemeApplied: (() -> Unit)? = null) {
        if (activity.isFinishing) return

        val sharedPreferences = activity.getSharedPreferences("theme_prefs", Activity.MODE_PRIVATE)
        val currentMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        
        if (AppCompatDelegate.getDefaultNightMode() == currentMode && 
            sharedPreferences.getBoolean("is_dark_mode", !isDarkMode) == isDarkMode) {
            return
        }

        sharedPreferences.edit().putBoolean("is_dark_mode", isDarkMode).apply()
        AppCompatDelegate.setDefaultNightMode(currentMode)
        onThemeApplied?.invoke()
        
        // Use recreate() for a cleaner lifecycle handling
        activity.recreate()
    }

    fun checkAndPerformRevealAnimation(activity: Activity, root: ViewGroup?) {
        val screenshot = themeScreenshot ?: return
        if (root == null || activity.isFinishing) {
            themeScreenshot = null
            try { screenshot.recycle() } catch (e: Exception) {}
            return
        }
        themeScreenshot = null

        val imageView = ImageView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setImageBitmap(screenshot)
            scaleType = ImageView.ScaleType.CENTER_CROP
            elevation = 1000f
            // Match the end state of previous activity
            scaleX = 0.90f
            scaleY = 0.90f
            alpha = 0f
        }

        try {
            root.addView(imageView)
            
            // Phase 1: Transform back to full screen
            imageView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    // Phase 2: Circular reveal once full sized
                    root.post {
                        if (imageView.parent == null) return@post
                        val finalRadius = hypot(root.width.toDouble(), root.height.toDouble()).toFloat()
                        
                        try {
                            val revealAnim = ViewAnimationUtils.createCircularReveal(imageView, startX, startY, finalRadius, 0f)
                            revealAnim.duration = 600
                            revealAnim.interpolator = AccelerateDecelerateInterpolator()

                            // While revealing the top, zoom the new content for depth
                            root.scaleX = 0.95f
                            root.scaleY = 0.95f
                            root.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(600)
                                .setInterpolator(DecelerateInterpolator())
                                .start()

                            revealAnim.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    root.removeView(imageView)
                                    try { screenshot.recycle() } catch (e: Exception) {}
                                }
                            })
                            revealAnim.start()
                        } catch (e: Exception) {
                            root.removeView(imageView)
                            try { screenshot.recycle() } catch (e: Exception) {}
                        }
                    }
                }
                .start()
        } catch (e: Exception) {
            try { screenshot.recycle() } catch (ex: Exception) {}
        }
    }
}
