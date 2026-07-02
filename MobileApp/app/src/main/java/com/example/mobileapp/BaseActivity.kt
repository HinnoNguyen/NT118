package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.presentation.NotificationsAdapter
import com.example.mobileapp.presentation.NotificationsViewModel
import com.example.mobileapp.presentation.TimerViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.example.mobileapp.util.AnimationUtils.popIn
import com.example.mobileapp.util.AnimationUtils.setBounceClick
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {

    companion object {
        private var lastActivityIndex = -1

        fun resetNavigationState() {
            lastActivityIndex = -1
        }
    }

    private val notificationsViewModel: NotificationsViewModel by viewModels { ViewModelFactory() }
    private val timerViewModel: TimerViewModel by viewModels { ViewModelFactory() }

    private val activityOrder = listOf(
        MainActivity::class.java,
        ProfileActivity::class.java,
        EditProfileActivity::class.java,
        SnakeGameActivity::class.java,
        QuestActivity::class.java,
        TimerActivity::class.java,
        CalendarActivity::class.java,
        NotesActivity::class.java,
        StoryActivity::class.java,
        SettingsActivity::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                if (overridePauseTransition) {
                    overridePendingTransition(0, 0)
                }
            }
        })

        observeTimer()
    }

    private fun observeTimer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                timerViewModel.timerFinished.collect { finished ->
                    if (finished) {
                        addNotification(
                            "Focus Complete!",
                            "Your focus session has ended. Great work, hero!",
                            "timer"
                        )
                        timerViewModel.clearFinishedFlag()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val currentIndex = activityOrder.indexOf(this::class.java)
        if (currentIndex != -1 && lastActivityIndex != -1 && currentIndex != lastActivityIndex) {
            val isForward = currentIndex > lastActivityIndex
            animateContent(isForward)
        }
        if (currentIndex != -1) {
            lastActivityIndex = currentIndex
        }
    }

    protected var overridePauseTransition = true

    override fun onPause() {
        super.onPause()
        if (overridePauseTransition) {
            overridePendingTransition(0, 0)
        }
    }

    private fun animateContent(isForward: Boolean) {
        val root = findViewById<ViewGroup>(R.id.main) ?: return
        val bottomNav = findViewById<View>(R.id.bottomNav)
        val topBar = findViewById<View>(R.id.topBar)
        val topDivider = findViewById<View>(R.id.topDivider)

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val startX = if (isForward) screenWidth else -screenWidth

        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child != bottomNav && child != topBar && child != topDivider) {
                child.animate().cancel()
                child.translationX = startX
                child.alpha = 0.0f
                child.scaleX = 0.95f
                child.scaleY = 0.95f

                child.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(350)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        applyEdgeToEdgeInsets()
        setupNavigationListeners()
        updateBottomNavSelection()
        setupNotificationListener()
    }

    private fun setupNotificationListener() {
        val bellContainer = findViewById<View>(R.id.topBar)?.findViewById<View>(R.id.bell_container) ?: 
                           findViewById<View>(R.id.main)?.findViewById<View>(R.id.bell_container)
        
        val redDot = bellContainer?.findViewById<View>(R.id.viewUnreadDot)

        bellContainer?.setOnClickListener {
            showNotificationsDialog()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationsViewModel.unreadCount.collect { count ->
                    if (count > 0 && redDot?.visibility != View.VISIBLE) {
                        redDot?.visibility = View.VISIBLE
                        redDot?.popIn()
                    } else if (count == 0) {
                        redDot?.visibility = View.GONE
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationsViewModel.error.collect { error ->
                    error?.let {
                        showAppNotification("Error", it)
                        notificationsViewModel.clearError()
                    }
                }
            }
        }
    }

    private fun showNotificationsDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_notifications, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val rv = dialogView.findViewById<RecyclerView>(R.id.rvNotifications)
        val btnClear = dialogView.findViewById<TextView>(R.id.btnClearAll)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btnClose)

        val adapter = NotificationsAdapter { notification ->
            notificationsViewModel.markAsRead(notification.id)
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        lifecycleScope.launch {
            notificationsViewModel.notifications.collect {
                adapter.submitList(it)
            }
        }

        btnClear.setOnClickListener {
            notificationsViewModel.clearAll()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun applyEdgeToEdgeInsets() {
        val root = findViewById<View>(R.id.main)
        val topBar = findViewById<View>(R.id.topBar)
        val bottomNav = findViewById<View>(R.id.bottomNav)

        ViewCompat.setOnApplyWindowInsetsListener(root ?: return) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            topBar?.setPadding(
                topBar.paddingLeft,
                systemBars.top,
                topBar.paddingRight,
                topBar.paddingBottom
            )

            bottomNav?.setPadding(
                bottomNav.paddingLeft,
                bottomNav.paddingTop,
                bottomNav.paddingRight,
                systemBars.bottom
            )
            
            insets
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateBottomNavSelection()
    }

    private fun setupNavigationListeners() {
        findViewById<LinearLayout>(R.id.navHome)?.apply { setBounceClick(); setOnClickListener { navigateTo(MainActivity::class.java) } }
        findViewById<LinearLayout>(R.id.navQuest)?.apply { setBounceClick(); setOnClickListener { navigateTo(QuestActivity::class.java) } }
        findViewById<LinearLayout>(R.id.navTime)?.apply { setBounceClick(); setOnClickListener { navigateTo(TimerActivity::class.java) } }
        findViewById<LinearLayout>(R.id.navNotes)?.apply { setBounceClick(); setOnClickListener { navigateTo(NotesActivity::class.java) } }
        findViewById<LinearLayout>(R.id.navStory)?.apply { setBounceClick(); setOnClickListener { navigateTo(StoryActivity::class.java) } }
        findViewById<LinearLayout>(R.id.navSettings)?.apply { setBounceClick(); setOnClickListener { navigateTo(SettingsActivity::class.java) } }
    }

    private fun updateBottomNavSelection() {
        val currentCls = this::class.java
        val navItems = mapOf(
            MainActivity::class.java to Pair(R.id.icHome, R.id.tvHome),
            QuestActivity::class.java to Pair(R.id.icQuest, R.id.tvQuest),
            TimerActivity::class.java to Pair(R.id.icTime, R.id.tvTime),
            CalendarActivity::class.java to Pair(R.id.icTime, R.id.tvTime),
            NotesActivity::class.java to Pair(R.id.icNotes, R.id.tvNotes),
            StoryActivity::class.java to Pair(R.id.icStory, R.id.tvStory),
            SettingsActivity::class.java to Pair(R.id.icSettings, R.id.tvSettings)
        )

        navItems.forEach { (cls, views) ->
            val icon = findViewById<TextView>(views.first)
            val text = findViewById<TextView>(views.second)
            if (cls == currentCls) {
                icon?.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
                text?.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
                text?.alpha = 1.0f
            } else {
                icon?.setTextColor(ContextCompat.getColor(this, R.color.white))
                text?.setTextColor(ContextCompat.getColor(this, R.color.white))
                text?.alpha = 0.5f
            }
        }
    }

    protected fun navigateTo(cls: Class<*>) {
        if (this::class.java == cls) return

        val intent = Intent(this, cls)
        
        startActivity(intent)
        if (overridePauseTransition) {
            overridePendingTransition(0, 0)
        }
    }

    protected fun addNotification(title: String, message: String, type: String) {
        notificationsViewModel.addNotification(title, message, type)
        showAppNotification(title, message)
    }

    protected fun showAppNotification(title: String, message: String) {
        val root = findViewById<ViewGroup>(android.R.id.content) ?: return
        val view = layoutInflater.inflate(R.layout.layout_custom_notification, root, false)
        
        view.findViewById<TextView>(R.id.tvNotificationTitle).text = title.uppercase()
        view.findViewById<TextView>(R.id.tvNotificationMessage).text = message
        
        root.addView(view)
        
        val inAnim = AnimationUtils.loadAnimation(this, R.anim.notification_in)
        val outAnim = AnimationUtils.loadAnimation(this, R.anim.notification_out)
        
        view.startAnimation(inAnim)
        
        view.postDelayed({
            outAnim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(p0: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(p0: android.view.animation.Animation?) {}
                override fun onAnimationEnd(p0: android.view.animation.Animation?) {
                    root.removeView(view)
                }
            })
            view.startAnimation(outAnim)
        }, 3000)
    }
}
