package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class TimerActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnStartTimer: MaterialButton
    private var countDownTimer: CountDownTimer? = null
    
    private var timerLengthMs = 25 * 60 * 1000L
    private var timeLeftMs = 25 * 60 * 1000L
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        setupNavigation()
        setupTabs()
        setupTimer()
    }

    private fun setupTabs() {
        findViewById<TextView>(R.id.tabCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun setupTimer() {
        tvTimer = findViewById(R.id.tvTimer)
        btnStartTimer = findViewById(R.id.btnStartTimer)

        findViewById<TextView>(R.id.btnTime15).setOnClickListener { selectTime(15) }
        findViewById<TextView>(R.id.btnTime25).setOnClickListener { selectTime(25) }
        findViewById<TextView>(R.id.btnTime45).setOnClickListener { selectTime(45) }
        findViewById<TextView>(R.id.btnTime60).setOnClickListener { selectTime(60) }

        btnStartTimer.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        findViewById<MaterialButton>(R.id.btnResetTimer).setOnClickListener {
            resetTimer()
        }
    }

    private fun selectTime(minutes: Int) {
        stopTimer()
        timerLengthMs = minutes * 60 * 1000L
        timeLeftMs = timerLengthMs
        updateTimerDisplay()

        val buttonsMap = mapOf(
            15 to findViewById<TextView>(R.id.btnTime15),
            25 to findViewById<TextView>(R.id.btnTime25),
            45 to findViewById<TextView>(R.id.btnTime45),
            60 to findViewById<TextView>(R.id.btnTime60)
        )

        for ((mins, button) in buttonsMap) {
            if (mins == minutes) {
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

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMs = millisUntilFinished
                updateTimerDisplay()
            }

            override fun onFinish() {
                timeLeftMs = 0
                updateTimerDisplay()
                isTimerRunning = false
                btnStartTimer.text = "▶ START"
                Toast.makeText(this@TimerActivity, "Focus session complete! Great job!", Toast.LENGTH_LONG).show()
            }
        }.start()

        isTimerRunning = true
        btnStartTimer.text = "❚❚ PAUSE"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStartTimer.text = "▶ START"
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStartTimer.text = "▶ START"
    }

    private fun resetTimer() {
        stopTimer()
        timeLeftMs = timerLengthMs
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        val minutes = (timeLeftMs / 1000) / 60
        val seconds = (timeLeftMs / 1000) % 60
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener { startActivity(Intent(this, QuestActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener { startActivity(Intent(this, NotesActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener { startActivity(Intent(this, StoryActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish() }
    }
}
