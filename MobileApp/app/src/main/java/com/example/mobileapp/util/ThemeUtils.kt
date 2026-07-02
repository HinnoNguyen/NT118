package com.example.mobileapp.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import kotlin.math.hypot

object ThemeUtils {
    private var themeScreenshot: Bitmap? = null
    private var startX: Int = 0
    private var startY: Int = 0

    fun toggleTheme(activity: Activity, viewToCapture: View, triggerView: View, isDarkMode: Boolean) {
        if (viewToCapture.width <= 0 || viewToCapture.height <= 0) {
            applyThemeWithoutAnimation(activity, isDarkMode)
            return
        }

        try {
            val bitmap = Bitmap.createBitmap(viewToCapture.width, viewToCapture.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            viewToCapture.draw(canvas)
            themeScreenshot = bitmap
        } catch (t: Throwable) {
            applyThemeWithoutAnimation(activity, isDarkMode)
            return
        }
        
        val location = IntArray(2)
        triggerView.getLocationInWindow(location)
        startX = location[0] + triggerView.width / 2
        startY = location[1] + triggerView.height / 2

        applyThemeWithoutAnimation(activity, isDarkMode)
    }

    private fun applyThemeWithoutAnimation(activity: Activity, isDarkMode: Boolean) {
        if (activity.isFinishing) return

        val sharedPreferences = activity.getSharedPreferences("theme_prefs", Activity.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("is_dark_mode", isDarkMode).apply()
        
        val currentMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(currentMode)

        val intent = Intent(activity, activity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        activity.startActivity(intent)
        activity.finish()
        activity.overridePendingTransition(0, 0)
    }

    fun checkAndPerformRevealAnimation(activity: Activity, root: ViewGroup?) {
        val screenshot = themeScreenshot ?: return
        if (root == null) {
            themeScreenshot = null
            return
        }
        themeScreenshot = null

        val imageView = ImageView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setImageBitmap(screenshot)
            scaleType = ImageView.ScaleType.CENTER_CROP
            elevation = 999f // Ensure it's on top of everything
        }

        root.addView(imageView)

        // Use a slightly longer delay to ensure the new activity has rendered its first frame
        // or just wait for the layout pass to finish.
        root.post {
            val finalRadius = hypot(root.width.toDouble(), root.height.toDouble()).toFloat()
            
            // AccelerateDecelerate is usually smoother for large circular reveals
            val revealAnim = ViewAnimationUtils.createCircularReveal(imageView, startX, startY, finalRadius, 0f)
            revealAnim.duration = 650 // Slightly faster but with better interpolation
            revealAnim.interpolator = AccelerateDecelerateInterpolator()

            revealAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    root.removeView(imageView)
                }
            })
            revealAnim.start()
        }
    }
}
