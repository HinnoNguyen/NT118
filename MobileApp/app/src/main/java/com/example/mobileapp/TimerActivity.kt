package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class TimerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener { startActivity(Intent(this, QuestActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener { startActivity(Intent(this, NotesActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener { startActivity(Intent(this, StoryActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish() }
    }
}
