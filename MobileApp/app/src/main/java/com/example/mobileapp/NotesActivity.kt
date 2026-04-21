package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class NotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        setupUI()
        setupNavigation()
    }

    private fun setupUI() {
        val btnNewNote = findViewById<MaterialButton>(R.id.btnNewNote)
        val newNoteSection = findViewById<LinearLayout>(R.id.newNoteSection)
        
        val btnTypeNote = findViewById<TextView>(R.id.btnTypeNote)
        val btnTypeReminder = findViewById<TextView>(R.id.btnTypeReminder)
        val btnTypeFlashcard = findViewById<TextView>(R.id.btnTypeFlashcard)
        val reminderTimeLayout = findViewById<LinearLayout>(R.id.reminderTimeLayout)

        // Toggle New Note Section
        btnNewNote.setOnClickListener {
            if (newNoteSection.visibility == View.GONE) {
                newNoteSection.visibility = View.VISIBLE
                btnNewNote.text = "✕"
            } else {
                newNoteSection.visibility = View.GONE
                btnNewNote.text = "+ NEW"
            }
        }

        // Type Selection Logic
        val typeButtons = listOf(btnTypeNote, btnTypeReminder, btnTypeFlashcard)
        
        fun selectType(selectedView: TextView) {
            typeButtons.forEach { btn ->
                if (btn == selectedView) {
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.note_purple))
                    btn.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    btn.setBackgroundResource(R.drawable.bg_button_unselected)
                    btn.setTextColor(0xFFAAAAAA.toInt())
                }
            }

            if (selectedView == btnTypeReminder) {
                reminderTimeLayout.visibility = View.VISIBLE
            } else {
                reminderTimeLayout.visibility = View.GONE
            }
        }

        btnTypeNote.setOnClickListener { selectType(btnTypeNote) }
        btnTypeReminder.setOnClickListener { selectType(btnTypeReminder) }
        btnTypeFlashcard.setOnClickListener { selectType(btnTypeFlashcard) }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener {
            // Intent to QuestActivity
        }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener {
            // Intent to TimerActivity
        }
        // Current is Notes
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener {
            startActivity(Intent(this, StoryActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
