package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class QuestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quest)
        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener { startActivity(Intent(this, TimerActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener { startActivity(Intent(this, NotesActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener { startActivity(Intent(this, StoryActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish() }
    }
}
