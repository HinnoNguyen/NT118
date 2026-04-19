package com.example.mobileapp

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class NotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val btnNewNote = findViewById<MaterialButton>(R.id.btnNewNote)
        val newNoteSection = findViewById<LinearLayout>(R.id.newNoteSection)
        
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
    }
}
