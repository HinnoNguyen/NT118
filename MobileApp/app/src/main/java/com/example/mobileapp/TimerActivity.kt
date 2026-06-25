package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.timer.TimerViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TimerActivity : AppCompatActivity() {

    private val viewModel: TimerViewModel by viewModels { TimerViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        setupNavigation()
        setupUI()
        observeViewModel()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navQuest)?.setOnClickListener {
            startActivity(Intent(this, QuestActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navNotes)?.setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navStory)?.setOnClickListener {
            startActivity(Intent(this, StoryActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navSettings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
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
            }
        }
    }
}
