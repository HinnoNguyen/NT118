package com.example.mobileapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.presentation.NotesAdapter
import com.example.mobileapp.presentation.NotesViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class NotesActivity : BaseActivity() {

    private val viewModel: NotesViewModel by viewModels { ViewModelFactory() }
    private lateinit var rvNotes: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var etNoteTitle: EditText
    private lateinit var etNoteContent: EditText
    private var selectedType = "note"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        rvNotes = findViewById(R.id.rvNotes)
        etNoteTitle = findViewById(R.id.etNoteTitle)
        etNoteContent = findViewById(R.id.etNoteContent)

        notesAdapter = NotesAdapter(
            onDeleteClick = { noteId ->
                viewModel.deleteNote(noteId)
            },
            onShareClick = { note ->
                com.example.mobileapp.utils.ShareHelper.showShareDialog(this, note.title, note.content)
            }
        )
        rvNotes.apply {
            layoutManager = LinearLayoutManager(this@NotesActivity)
            adapter = notesAdapter
        }

        setupFilterButtons()
        setupNewNoteSection()
    }

    private fun setupFilterButtons() {
        val btnFilterAll = findViewById<TextView>(R.id.btnFilterAll)
        val btnFilterNotes = findViewById<TextView>(R.id.btnFilterNotes)
        val btnFilterReminders = findViewById<TextView>(R.id.btnFilterReminders)
        val btnFilterFlashcards = findViewById<TextView>(R.id.btnFilterFlashcards)

        btnFilterAll.setOnClickListener { viewModel.setFilter("all") }
        btnFilterNotes.setOnClickListener { viewModel.setFilter("note") }
        btnFilterReminders.setOnClickListener { viewModel.setFilter("reminder") }
        btnFilterFlashcards.setOnClickListener { viewModel.setFilter("flashcard") }
    }

    private fun setupNewNoteSection() {
        val btnNewNote = findViewById<MaterialButton>(R.id.btnNewNote)
        val newNoteSection = findViewById<LinearLayout>(R.id.newNoteSection)
        val btnSaveScroll = findViewById<MaterialButton>(R.id.btnSaveScroll)

        val btnTypeNote = findViewById<TextView>(R.id.btnTypeNote)
        val btnTypeReminder = findViewById<TextView>(R.id.btnTypeReminder)
        val btnTypeFlashcard = findViewById<TextView>(R.id.btnTypeFlashcard)

        btnNewNote.setOnClickListener {
            if (newNoteSection.visibility == View.GONE) {
                newNoteSection.visibility = View.VISIBLE
                btnNewNote.text = "✕"
            } else {
                newNoteSection.visibility = View.GONE
                btnNewNote.text = "+ NEW"
            }
        }

        btnTypeNote.setOnClickListener { selectedType = "note"; updateTypeUI(btnTypeNote) }
        btnTypeReminder.setOnClickListener { selectedType = "reminder"; updateTypeUI(btnTypeReminder) }
        btnTypeFlashcard.setOnClickListener { selectedType = "flashcard"; updateTypeUI(btnTypeFlashcard) }

        btnSaveScroll.setOnClickListener {
            val title = etNoteTitle.text.toString()
            val content = etNoteContent.text.toString()
            if (title.isNotBlank()) {
                viewModel.addNote(title, content, selectedType)
                etNoteTitle.text.clear()
                etNoteContent.text.clear()
                newNoteSection.visibility = View.GONE
                btnNewNote.text = "+ NEW"
            } else {
                showAppNotification("Attention", "Title is required")
            }
        }
    }

    private fun updateFilterUI(selectedFilter: String) {
        val filters = mapOf(
            "all" to R.id.btnFilterAll,
            "note" to R.id.btnFilterNotes,
            "reminder" to R.id.btnFilterReminders,
            "flashcard" to R.id.btnFilterFlashcards
        )

        filters.forEach { (type, viewId) ->
            val btn = findViewById<TextView>(viewId)
            btn.setBackgroundResource(R.drawable.bg_filter_button)
            if (type == selectedFilter) {
                btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.accent_yellow)
                btn.setTextColor(ContextCompat.getColor(this, R.color.black))
            } else {
                btn.backgroundTintList = null
                btn.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
            }
        }
    }

    private fun updateTypeUI(selected: TextView) {
        val buttons = listOf(R.id.btnTypeNote, R.id.btnTypeReminder, R.id.btnTypeFlashcard)
        buttons.forEach { id ->
            val btn = findViewById<TextView>(id)
            if (btn == selected) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.note_purple))
                btn.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                btn.setBackgroundResource(R.drawable.bg_button_unselected)
                btn.setTextColor(0xFFAAAAAA.toInt())
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notes.collect { notes ->
                        notesAdapter.submitList(notes)
                    }
                }
                launch {
                    viewModel.filterType.collect { filter ->
                        updateFilterUI(filter)
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            showAppNotification("System Error", it)
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
}
