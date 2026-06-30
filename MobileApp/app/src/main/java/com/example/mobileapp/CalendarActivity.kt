package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        setupNavigation()
        setupTabs()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener { startActivity(Intent(this, QuestActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener { startActivity(Intent(this, TimerActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener { startActivity(Intent(this, NotesActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener { startActivity(Intent(this, StoryActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish() }
    }

    private fun setupTabs() {
        findViewById<TextView>(R.id.tabTimer).setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
}
