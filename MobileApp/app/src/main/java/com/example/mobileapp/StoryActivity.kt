package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.story.StoryViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Story (StoryForge) screen.
 * Communicates only with [StoryViewModel]; UI state is driven from the ViewModel.
 */
class StoryActivity : AppCompatActivity() {

    private val viewModel: StoryViewModel by viewModels { StoryViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)
        setupStoryForge()
        setupNavigation()
        observeViewModel()
    }

    private fun setupStoryForge() {
        val btnCreateStory = findViewById<MaterialButton>(R.id.btnCreateStory)
        val genreEpic = findViewById<LinearLayout>(R.id.genreEpic)
        val genreMystery = findViewById<LinearLayout>(R.id.genreMystery)
        val genreComedy = findViewById<LinearLayout>(R.id.genreComedy)
        val genreHorror = findViewById<LinearLayout>(R.id.genreHorror)

        btnCreateStory.setOnClickListener { viewModel.toggleForgeSection() }

        genreEpic.setOnClickListener    { viewModel.selectGenre(StoryViewModel.Genre.EPIC) }
        genreMystery.setOnClickListener { viewModel.selectGenre(StoryViewModel.Genre.MYSTERY) }
        genreComedy.setOnClickListener  { viewModel.selectGenre(StoryViewModel.Genre.COMEDY) }
        genreHorror.setOnClickListener  { viewModel.selectGenre(StoryViewModel.Genre.HORROR) }
    }

    private fun observeViewModel() {
        val btnCreateStory = findViewById<MaterialButton>(R.id.btnCreateStory)
        val newStorySection = findViewById<LinearLayout>(R.id.newStorySection)
        val genreEpic = findViewById<LinearLayout>(R.id.genreEpic)
        val genreMystery = findViewById<LinearLayout>(R.id.genreMystery)
        val genreComedy = findViewById<LinearLayout>(R.id.genreComedy)
        val genreHorror = findViewById<LinearLayout>(R.id.genreHorror)
        val genres = listOf(genreEpic, genreMystery, genreComedy, genreHorror)
        val genreEnum = listOf(
            StoryViewModel.Genre.EPIC, StoryViewModel.Genre.MYSTERY,
            StoryViewModel.Genre.COMEDY, StoryViewModel.Genre.HORROR
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isForgeVisible.collect { visible ->
                        newStorySection.visibility = if (visible) View.VISIBLE else View.GONE
                        btnCreateStory.text = if (visible) "✕" else "+ CREATE"
                    }
                }
                launch {
                    viewModel.selectedGenre.collect { selected ->
                        genres.forEachIndexed { i, layout ->
                            val text = layout.getChildAt(1) as TextView
                            if (genreEnum[i] == selected) {
                                layout.setBackgroundColor(0xFFFFD700.toInt())
                                text.setTextColor(ContextCompat.getColor(this@StoryActivity, R.color.black))
                            } else {
                                layout.setBackgroundResource(R.drawable.bg_button_unselected)
                                text.setTextColor(0xFFAAAAAA.toInt())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
