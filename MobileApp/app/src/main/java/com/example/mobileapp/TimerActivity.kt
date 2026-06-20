package com.example.mobileapp

import android.content.res.ColorStateList
import android.content.Intent
import android.os.CountDownTimer
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.TimerSessionRepositoryImpl
import com.example.mobileapp.domain.model.TimerSession
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimerActivity : AppCompatActivity() {
    private val timerSessionRepository = TimerSessionRepositoryImpl()

    private lateinit var tvTimer: TextView
    private lateinit var btnDuration15: TextView
    private lateinit var btnDuration25: TextView
    private lateinit var btnDuration45: TextView
    private lateinit var btnDuration60: TextView
    private lateinit var btnStartTimer: MaterialButton
    private lateinit var btnResetTimer: MaterialButton
    private lateinit var tvTimerStatus: TextView
    private lateinit var timerSessionsContainer: LinearLayout

    private var selectedDurationMinutes = 25
    private var remainingMillis = minutesToMillis(selectedDurationMinutes)
    private var countdownTimer: CountDownTimer? = null
    private var activeSession: TimerSession? = null
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        setupUi()
        setupTabs()
        setupNavigation()
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        countdownTimer = null
        super.onDestroy()
    }

    private fun setupUi() {
        tvTimer = findViewById(R.id.tvTimer)
        btnDuration15 = findViewById(R.id.btnDuration15)
        btnDuration25 = findViewById(R.id.btnDuration25)
        btnDuration45 = findViewById(R.id.btnDuration45)
        btnDuration60 = findViewById(R.id.btnDuration60)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnResetTimer = findViewById(R.id.btnResetTimer)
        tvTimerStatus = findViewById(R.id.tvTimerStatus)
        timerSessionsContainer = findViewById(R.id.timerSessionsContainer)

        val durationButtons = mapOf(
            btnDuration15 to 15,
            btnDuration25 to 25,
            btnDuration45 to 45,
            btnDuration60 to 60
        )

        durationButtons.forEach { (button, minutes) ->
            button.setOnClickListener {
                if (isTimerRunning) {
                    Toast.makeText(this, "Reset the active timer before changing duration", Toast.LENGTH_SHORT).show()
                } else {
                    selectDuration(minutes)
                }
            }
        }

        btnStartTimer.setOnClickListener {
            if (isTimerRunning) stopActiveTimer() else startTimer()
        }
        btnResetTimer.setOnClickListener { resetTimer() }

        selectDuration(selectedDurationMinutes)
        loadSessions()
    }

    private fun setupTabs() {
        findViewById<TextView>(R.id.tabCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
            finish()
        }
    }

    private fun selectDuration(minutes: Int) {
        selectedDurationMinutes = minutes
        remainingMillis = minutesToMillis(minutes)
        renderRemainingTime()

        val selectedBackground = 0xFFFFD700.toInt()
        val selectedTextColor = ContextCompat.getColor(this, R.color.black)
        val unselectedTextColor = 0xFFAAAAAA.toInt()
        val buttons = listOf(btnDuration15, btnDuration25, btnDuration45, btnDuration60)

        buttons.forEach { button ->
            val buttonMinutes = button.text.toString().removeSuffix("m").toIntOrNull()
            if (buttonMinutes == minutes) {
                button.setBackgroundColor(selectedBackground)
                button.setTextColor(selectedTextColor)
            } else {
                button.setBackgroundResource(R.drawable.bg_button_unselected)
                button.setTextColor(unselectedTextColor)
            }
        }
    }

    private fun startTimer() {
        lifecycleScope.launch {
            timerSessionRepository.createSession(selectedDurationMinutes)
                .onSuccess { session ->
                    activeSession = session
                    isTimerRunning = true
                    btnStartTimer.text = "■ STOP"
                    btnStartTimer.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(this@TimerActivity, R.color.rank_s))
                    startCountdown()
                }
                .onFailure {
                    Toast.makeText(this@TimerActivity, it.message ?: "Failed to start timer", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun startCountdown() {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(remainingMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                renderRemainingTime()
            }

            override fun onFinish() {
                remainingMillis = 0L
                renderRemainingTime()
                completeActiveTimer()
            }
        }.start()
    }

    private fun completeActiveTimer() {
        val session = activeSession ?: return resetTimerState(loadAfterReset = true)
        lifecycleScope.launch {
            timerSessionRepository.completeSession(session)
                .onSuccess {
                    Toast.makeText(this@TimerActivity, "Focus session completed", Toast.LENGTH_SHORT).show()
                    resetTimerState(loadAfterReset = true)
                }
                .onFailure {
                    Toast.makeText(this@TimerActivity, it.message ?: "Failed to complete timer session", Toast.LENGTH_SHORT).show()
                    resetTimerState(loadAfterReset = true)
                }
        }
    }

    private fun stopActiveTimer() {
        val session = activeSession ?: return resetTimer()
        countdownTimer?.cancel()
        lifecycleScope.launch {
            timerSessionRepository.interruptSession(session)
                .onSuccess {
                    Toast.makeText(this@TimerActivity, "Timer stopped", Toast.LENGTH_SHORT).show()
                    resetTimerState(loadAfterReset = true)
                }
                .onFailure {
                    Toast.makeText(this@TimerActivity, it.message ?: "Failed to stop timer", Toast.LENGTH_SHORT).show()
                    resetTimerState(loadAfterReset = true)
                }
        }
    }

    private fun resetTimer() {
        if (isTimerRunning) {
            stopActiveTimer()
            return
        }
        resetTimerState(loadAfterReset = false)
    }

    private fun resetTimerState(loadAfterReset: Boolean) {
        countdownTimer?.cancel()
        countdownTimer = null
        activeSession = null
        isTimerRunning = false
        btnStartTimer.text = "▶ START"
        btnStartTimer.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent_green))
        remainingMillis = minutesToMillis(selectedDurationMinutes)
        renderRemainingTime()
        if (loadAfterReset) {
            loadSessions()
        }
    }

    private fun renderRemainingTime() {
        val totalSeconds = remainingMillis / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun loadSessions() {
        tvTimerStatus.visibility = android.view.View.VISIBLE
        tvTimerStatus.text = "Loading sessions..."
        timerSessionsContainer.removeAllViews()

        lifecycleScope.launch {
            timerSessionRepository.getSessions()
                .onSuccess { renderSessions(it) }
                .onFailure {
                    tvTimerStatus.text = it.message ?: "Failed to load timer sessions"
                    tvTimerStatus.visibility = android.view.View.VISIBLE
                }
        }
    }

    private fun renderSessions(sessions: List<TimerSession>) {
        timerSessionsContainer.removeAllViews()
        if (sessions.isEmpty()) {
            tvTimerStatus.text = "No sessions yet."
            tvTimerStatus.visibility = android.view.View.VISIBLE
            return
        }

        tvTimerStatus.visibility = android.view.View.GONE
        val inflater = LayoutInflater.from(this)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sessions.forEach { session ->
            val itemView = inflater.inflate(R.layout.item_timer_session, timerSessionsContainer, false)
            itemView.findViewById<TextView>(R.id.tvSessionIcon).text = if (session.completed) "🔥" else "⏱"
            itemView.findViewById<TextView>(R.id.tvSessionTitle).text = "${session.durationMinutes}m focus run"
            itemView.findViewById<TextView>(R.id.tvSessionMeta).text =
                "${formatter.format(Date(session.startedAt))} • ${if (session.completed) "completed" else "stopped"}"

            val badge = itemView.findViewById<TextView>(R.id.tvSessionBadge)
            if (session.completed) {
                badge.text = "DONE"
                badge.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_green))
            } else {
                badge.text = "STOPPED"
                badge.setBackgroundColor(ContextCompat.getColor(this, R.color.rank_s))
            }

            timerSessionsContainer.addView(itemView)
        }
    }

    private fun minutesToMillis(minutes: Int): Long = minutes * 60_000L

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener { startActivity(Intent(this, QuestActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener { startActivity(Intent(this, NotesActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener { startActivity(Intent(this, StoryActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish() }
    }
}
