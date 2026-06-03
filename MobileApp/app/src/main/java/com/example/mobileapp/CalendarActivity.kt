package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.EventRepositoryImpl
import com.example.mobileapp.domain.model.Event
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarActivity : AppCompatActivity() {
    private val eventRepository = EventRepositoryImpl()

    private lateinit var tabTimer: TextView
    private lateinit var etEventTitle: EditText
    private lateinit var etEventDescription: EditText
    private lateinit var etEventDate: EditText
    private lateinit var etEventTime: EditText
    private lateinit var etEventLocation: EditText
    private lateinit var btnSaveEvent: MaterialButton
    private lateinit var tvEventsStatus: TextView
    private lateinit var eventsListContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        setupUI()
        setupNavigation()
        loadEvents()
    }

    private fun setupUI() {
        tabTimer = findViewById(R.id.tabTimer)
        etEventTitle = findViewById(R.id.etEventTitle)
        etEventDescription = findViewById(R.id.etEventDescription)
        etEventDate = findViewById(R.id.etEventDate)
        etEventTime = findViewById(R.id.etEventTime)
        etEventLocation = findViewById(R.id.etEventLocation)
        btnSaveEvent = findViewById(R.id.btnSaveEvent)
        tvEventsStatus = findViewById(R.id.tvEventsStatus)
        eventsListContainer = findViewById(R.id.eventsListContainer)

        etEventDate.setText(System.currentTimeMillis().toString())
        etEventTime.setText("09:00")

        tabTimer.setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
            finish()
        }

        btnSaveEvent.setOnClickListener { saveEvent() }
    }

    private fun saveEvent() {
        val date = etEventDate.text.toString().toLongOrNull()
        if (date == null) {
            Toast.makeText(this, "Date must be epoch milliseconds", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            eventRepository.createEvent(
                title = etEventTitle.text.toString(),
                description = etEventDescription.text.toString(),
                date = date,
                time = etEventTime.text.toString(),
                location = etEventLocation.text.toString()
            ).onSuccess {
                Toast.makeText(this@CalendarActivity, "Event saved", Toast.LENGTH_SHORT).show()
                clearForm()
                loadEvents()
            }.onFailure {
                Toast.makeText(this@CalendarActivity, it.message ?: "Failed to save event", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearForm() {
        etEventTitle.text?.clear()
        etEventDescription.text?.clear()
        etEventLocation.text?.clear()
        etEventTime.setText("09:00")
        etEventDate.setText(System.currentTimeMillis().toString())
    }

    private fun loadEvents() {
        tvEventsStatus.text = "Loading events..."
        tvEventsStatus.visibility = android.view.View.VISIBLE
        eventsListContainer.removeAllViews()

        lifecycleScope.launch {
            eventRepository.getUpcomingEvents()
                .onSuccess { renderEvents(it) }
                .onFailure {
                    tvEventsStatus.text = it.message ?: "Failed to load events"
                    tvEventsStatus.visibility = android.view.View.VISIBLE
                }
        }
    }

    private fun renderEvents(events: List<Event>) {
        eventsListContainer.removeAllViews()
        if (events.isEmpty()) {
            tvEventsStatus.text = "No upcoming events yet."
            tvEventsStatus.visibility = android.view.View.VISIBLE
            return
        }

        tvEventsStatus.visibility = android.view.View.GONE
        val inflater = LayoutInflater.from(this)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        events.forEach { event ->
            val itemView = inflater.inflate(R.layout.item_event, eventsListContainer, false)
            itemView.findViewById<TextView>(R.id.tvEventIcon).text = "📌"
            itemView.findViewById<TextView>(R.id.tvEventTitle).text = event.title
            val meta = buildString {
                append(formatter.format(Date(event.date)))
                if (event.time.isNotBlank()) append(" • ").append(event.time)
                if (event.location.isNotBlank()) append(" • ").append(event.location)
            }
            itemView.findViewById<TextView>(R.id.tvEventMeta).text = meta
            itemView.findViewById<TextView>(R.id.btnDeleteEvent).setOnClickListener {
                deleteEvent(event.id)
            }
            eventsListContainer.addView(itemView)
        }
    }

    private fun deleteEvent(eventId: String) {
        lifecycleScope.launch {
            eventRepository.deleteEvent(eventId)
                .onSuccess {
                    Toast.makeText(this@CalendarActivity, "Event deleted", Toast.LENGTH_SHORT).show()
                    loadEvents()
                }
                .onFailure {
                    Toast.makeText(this@CalendarActivity, it.message ?: "Failed to delete event", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener {
            startActivity(Intent(this, QuestActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
            finish()
        }
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
