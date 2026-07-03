package com.example.mobileapp

import android.app.TimePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotesActivity : BaseActivity() {

    private val viewModel: NotesViewModel by viewModels { ViewModelFactory() }
    private lateinit var rvNotes: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var etNoteTitle: EditText
    private lateinit var etNoteContent: EditText
    private var selectedType = "note"
    private var selectedReminderTime: Calendar? = null

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
                deleteNote(noteId)
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

    private fun deleteNote(noteId: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_delete_note))
            .setMessage(getString(R.string.dialog_delete_confirm))
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                viewModel.deleteNote(noteId)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
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
        val tvReminderTime = findViewById<TextView>(R.id.tvReminderTime)

        val btnTypeNote = findViewById<TextView>(R.id.btnTypeNote)
        val btnTypeReminder = findViewById<TextView>(R.id.btnTypeReminder)
        val btnTypeFlashcard = findViewById<TextView>(R.id.btnTypeFlashcard)

        btnNewNote.setOnClickListener {
            if (newNoteSection.visibility == View.GONE) {
                newNoteSection.visibility = View.VISIBLE
                btnNewNote.text = getString(R.string.btn_close_x)
            } else {
                newNoteSection.visibility = View.GONE
                btnNewNote.text = getString(R.string.btn_new)
            }
        }

        btnTypeNote.setOnClickListener { selectedType = "note"; updateTypeUI(btnTypeNote) }
        btnTypeReminder.setOnClickListener { selectedType = "reminder"; updateTypeUI(btnTypeReminder) }
        btnTypeFlashcard.setOnClickListener { selectedType = "flashcard"; updateTypeUI(btnTypeFlashcard) }

        tvReminderTime.setOnClickListener {
            val calendar = selectedReminderTime ?: Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val newTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                selectedReminderTime = newTime
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvReminderTime.text = sdf.format(newTime.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        btnSaveScroll.setOnClickListener {
            val title = etNoteTitle.text.toString()
            val content = etNoteContent.text.toString()
            if (title.isNotBlank()) {
                val reminderTime = if (selectedType == "reminder") selectedReminderTime?.timeInMillis else null
                viewModel.addNote(title, content, selectedType, reminderTime)
                etNoteTitle.text.clear()
                etNoteContent.text.clear()
                selectedReminderTime = null
                tvReminderTime.text = getString(R.string.default_time)
                newNoteSection.visibility = View.GONE
                btnNewNote.text = getString(R.string.btn_new)
            } else {
                showAppNotification(getString(R.string.notification_attention), getString(R.string.error_title_required))
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
        val reminderTimeLayout = findViewById<LinearLayout>(R.id.reminderTimeLayout)
        
        reminderTimeLayout.visibility = if (selected.id == R.id.btnTypeReminder) View.VISIBLE else View.GONE
        
        buttons.forEach { id ->
            val btn = findViewById<TextView>(id)
            if (btn == selected) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.note_purple))
                btn.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                btn.setBackgroundResource(R.drawable.bg_button_unselected)
                btn.setTextColor(ContextCompat.getColor(this, R.color.text_gray))
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
                            showAppNotification(getString(R.string.notification_system_error), it)
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
}
