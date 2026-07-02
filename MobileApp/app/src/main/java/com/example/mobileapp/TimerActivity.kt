package com.example.mobileapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.TimerViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.example.mobileapp.util.AnimationUtils.pulse
import com.example.mobileapp.util.AnimationUtils.setBounceClick
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TimerActivity : BaseActivity() {

    private val viewModel: TimerViewModel by viewModels { ViewModelFactory() }

    private lateinit var tvTimer: TextView
    private lateinit var btnStartTimer: MaterialButton

    private val notificationPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        
        com.example.mobileapp.utils.NotificationHelper.createChannel(this)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setupTabs()
        setupTimer()
        observeViewModel()
    }

    private fun setupTabs() {
        findViewById<TextView>(R.id.tabCalendar).setOnClickListener {
            navigateTo(CalendarActivity::class.java)
        }
    }

    private fun setupTimer() {
        tvTimer = findViewById(R.id.tvTimer)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnStartTimer.setBounceClick()
        btnStartTimer.pulse()

        findViewById<TextView>(R.id.btnTime15).setOnClickListener { viewModel.setTimer(15) }
        findViewById<TextView>(R.id.btnTime25).setOnClickListener { viewModel.setTimer(25) }
        findViewById<TextView>(R.id.btnTime45).setOnClickListener { viewModel.setTimer(45) }
        findViewById<TextView>(R.id.btnTime60).setOnClickListener { viewModel.setTimer(60) }

        btnStartTimer.setOnClickListener {
            if (viewModel.isTimerRunning.value) {
                viewModel.pauseTimer()
            } else {
                viewModel.startTimer()
            }
        }

        findViewById<MaterialButton>(R.id.btnResetTimer).apply {
            setBounceClick()
            setOnClickListener {
                viewModel.resetTimer()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.timeLeftMs.collect { timeLeft ->
                        updateTimerDisplay(timeLeft)
                        updateButtonStates(viewModel.getTimerLengthMinutes())
                    }
                }
                launch {
                    viewModel.isTimerRunning.collect { isRunning ->
                        btnStartTimer.text = if (isRunning) "❚❚ PAUSE" else "▶ START"
                    }
                }
                launch {
                    viewModel.timerFinished.collect { finished ->
                        if (finished) {
                            Toast.makeText(this@TimerActivity, "Focus session complete! Great job!", Toast.LENGTH_LONG).show()
                            com.example.mobileapp.utils.NotificationHelper.showSessionComplete(
                                this@TimerActivity, 
                                "Focus session complete!", 
                                "Great job! Time for a well-deserved break."
                            )
                            viewModel.clearFinishedFlag()
                        }
                    }
                }
            }
        }
    }

    private fun updateButtonStates(selectedMinutes: Int) {
        val buttonsMap = mapOf(
            15 to findViewById<TextView>(R.id.btnTime15),
            25 to findViewById<TextView>(R.id.btnTime25),
            45 to findViewById<TextView>(R.id.btnTime45),
            60 to findViewById<TextView>(R.id.btnTime60)
        )

        for ((mins, button) in buttonsMap) {
            if (button == null) continue
            if (mins == selectedMinutes) {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_yellow))
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
                button.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                button.setBackgroundResource(R.drawable.bg_button_unselected)
                button.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
                button.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun updateTimerDisplay(timeLeftMs: Long) {
        val minutes = (timeLeftMs / 1000) / 60
        val seconds = (timeLeftMs / 1000) % 60
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }
}
