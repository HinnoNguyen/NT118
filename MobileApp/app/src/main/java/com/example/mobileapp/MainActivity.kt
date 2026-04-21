package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        setupEdgeToEdge()
        setupUI()
        setupNavigation()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUI() {
        val userInfoCard = findViewById<LinearLayout>(R.id.userInfoCard)
        userInfoCard.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Đã ở Home rồi
        }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener {
            startActivity(Intent(this, QuestActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener {
            startActivity(Intent(this, StoryActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
