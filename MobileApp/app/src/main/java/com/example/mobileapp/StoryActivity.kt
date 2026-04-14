package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class StoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        setupStoryForge()
        setupNavigation()
    }

    private fun setupStoryForge() {
        val btnCreateStory = findViewById<MaterialButton>(R.id.btnCreateStory)
        val newStorySection = findViewById<LinearLayout>(R.id.newStorySection)
        
        val genreEpic = findViewById<LinearLayout>(R.id.genreEpic)
        val genreMystery = findViewById<LinearLayout>(R.id.genreMystery)
        val genreComedy = findViewById<LinearLayout>(R.id.genreComedy)
        val genreHorror = findViewById<LinearLayout>(R.id.genreHorror)

        val genres = listOf(genreEpic, genreMystery, genreComedy, genreHorror)

        // Toggle Forge Section
        btnCreateStory.setOnClickListener {
            if (newStorySection.visibility == View.GONE) {
                newStorySection.visibility = View.VISIBLE
                btnCreateStory.text = "✕"
            } else {
                newStorySection.visibility = View.GONE
                btnCreateStory.text = "+ CREATE"
            }
        }

        // Genre Selection Logic
        fun selectGenre(selected: LinearLayout) {
            genres.forEach { layout ->
                val icon = layout.getChildAt(0) as TextView
                val text = layout.getChildAt(1) as TextView
                
                if (layout == selected) {
                    layout.setBackgroundColor(0xFFFFD700.toInt()) // Gold
                    text.setTextColor(ContextCompat.getColor(this, R.color.black))
                } else {
                    layout.setBackgroundResource(R.drawable.bg_button_unselected)
                    text.setTextColor(0xFFAAAAAA.toInt())
                }
            }
        }

        genreEpic.setOnClickListener { selectGenre(genreEpic) }
        genreMystery.setOnClickListener { selectGenre(genreMystery) }
        genreComedy.setOnClickListener { selectGenre(genreComedy) }
        genreHorror.setOnClickListener { selectGenre(genreHorror) }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener {
            // Intent to QuestActivity
        }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
