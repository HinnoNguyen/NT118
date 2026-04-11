package com.example.mobileapp

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
import com.example.mobileapp.presentation.notes.NotesViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Notes screen.
 * Communicates only with [NotesViewModel]; UI state is driven from the ViewModel.
 */
class NotesActivity : AppCompatActivity() {

    private val viewModel: NotesViewModel by viewModels { NotesViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        val btnNewNote = findViewById<MaterialButton>(R.id.btnNewNote)
        val btnTypeNote = findViewById<TextView>(R.id.btnTypeNote)
        val btnTypeReminder = findViewById<TextView>(R.id.btnTypeReminder)
        val btnTypeFlashcard = findViewById<TextView>(R.id.btnTypeFlashcard)

        btnNewNote.setOnClickListener { viewModel.toggleNewNoteSection() }
        btnTypeNote.setOnClickListener     { viewModel.selectType(NotesViewModel.NoteType.NOTE) }
        btnTypeReminder.setOnClickListener { viewModel.selectType(NotesViewModel.NoteType.REMINDER) }
        btnTypeFlashcard.setOnClickListener { viewModel.selectType(NotesViewModel.NoteType.FLASHCARD) }
    }

    private fun observeViewModel() {
        val btnNewNote = findViewById<MaterialButton>(R.id.btnNewNote)
        val newNoteSection = findViewById<LinearLayout>(R.id.newNoteSection)
        val reminderTimeLayout = findViewById<LinearLayout>(R.id.reminderTimeLayout)
        val btnTypeNote = findViewById<TextView>(R.id.btnTypeNote)
        val btnTypeReminder = findViewById<TextView>(R.id.btnTypeReminder)
        val btnTypeFlashcard = findViewById<TextView>(R.id.btnTypeFlashcard)
        val typeButtons = listOf(btnTypeNote, btnTypeReminder, btnTypeFlashcard)
        val typeEnum = listOf(
            NotesViewModel.NoteType.NOTE,
            NotesViewModel.NoteType.REMINDER,
            NotesViewModel.NoteType.FLASHCARD
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isNewNoteVisible.collect { visible ->
                        newNoteSection.visibility = if (visible) View.VISIBLE else View.GONE
                        btnNewNote.text = if (visible) "✕" else "+ NEW"
                    }
                }
                launch {
                    viewModel.isReminderTimeVisible.collect { visible ->
                        reminderTimeLayout.visibility = if (visible) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.selectedType.collect { selected ->
                        typeButtons.forEachIndexed { i, btn ->
                            if (typeEnum[i] == selected) {
                                btn.setBackgroundColor(ContextCompat.getColor(this@NotesActivity, R.color.note_purple))
                                btn.setTextColor(ContextCompat.getColor(this@NotesActivity, R.color.white))
                            } else {
                                btn.setBackgroundResource(R.drawable.bg_button_unselected)
                                btn.setTextColor(0xFFAAAAAA.toInt())
                            }
                        }
                    }
                }
            }
        }
    }
}
