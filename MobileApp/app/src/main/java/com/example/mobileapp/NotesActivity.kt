package com.example.mobileapp

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
                    // Selected state
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.note_purple))
                    btn.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    // Unselected state
                    btn.setBackgroundResource(R.drawable.bg_button_unselected)
                    btn.setTextColor(0xFFAAAAAA.toInt())
                }
            }

            // Show/Hide Time Layout
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
}
