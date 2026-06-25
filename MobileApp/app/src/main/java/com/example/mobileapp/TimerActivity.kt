package com.example.mobileapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.timer.TimerViewModel
import com.example.mobileapp.utils.NavHelper
import com.example.mobileapp.utils.NotificationHelper
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TimerActivity : AppCompatActivity() {

    private val viewModel: TimerViewModel by viewModels { TimerViewModel.factory() }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        NotificationHelper.createChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        setupNavigation()
        setupUI()
        observeViewModel()
    }

    private fun setupNavigation() {
        NavHelper.setup(this, NavHelper.Screen.TIME)
        // Tab navigation to Calendar
        findViewById<TextView>(R.id.tabCalendar)?.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }
    }

    private fun setupUI() {
        findViewById<MaterialButton>(R.id.btnStartPause)?.setOnClickListener {
            viewModel.startPause()
        }
        findViewById<MaterialButton>(R.id.btnReset)?.setOnClickListener {
            viewModel.reset()
        }
        // Preset duration buttons
        findViewById<TextView>(R.id.btnPreset15)?.setOnClickListener {
            viewModel.setWorkMinutes(15)
        }
        findViewById<TextView>(R.id.btnPreset25)?.setOnClickListener {
            viewModel.setWorkMinutes(25)
        }
        findViewById<TextView>(R.id.btnPreset45)?.setOnClickListener {
            viewModel.setWorkMinutes(45)
        }
        findViewById<TextView>(R.id.btnPreset60)?.setOnClickListener {
            viewModel.setWorkMinutes(60)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.millisLeft.collect { ms ->
                        val minutes = (ms / 1000) / 60
                        val seconds = (ms / 1000) % 60
                        findViewById<TextView>(R.id.tvTimeDisplay)?.text =
                            "%02d:%02d".format(minutes, seconds)
                        val total = viewModel.totalMillis.value
                        if (total > 0) {
                            val progress = ((ms.toFloat() / total) * 100).toInt()
                            findViewById<ProgressBar>(R.id.progressTimer)?.progress = progress
                        }
                    }
                }
                launch {
                    viewModel.isRunning.collect { running ->
                        findViewById<MaterialButton>(R.id.btnStartPause)?.text =
                            if (running) "⏸ PAUSE" else "▶ START"
                    }
                }
                launch {
                    viewModel.sessionComplete.collect { completedMode ->
                        val (title, message) = when (completedMode) {
                            TimerViewModel.Mode.WORK -> "Work session complete!" to "Time for a break."
                            TimerViewModel.Mode.BREAK -> "Break's over!" to "Back to work."
                        }
                        NotificationHelper.showSessionComplete(this@TimerActivity, title, message)
                        Toast.makeText(this@TimerActivity, title, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
