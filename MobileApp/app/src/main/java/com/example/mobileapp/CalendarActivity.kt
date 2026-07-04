package com.example.mobileapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.presentation.CalendarViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : BaseActivity() {

    private val viewModel: CalendarViewModel by viewModels { ViewModelFactory() }

    private lateinit var calendarGrid: GridLayout
    private lateinit var tvMonthYear: TextView
    private lateinit var eventListContainer: LinearLayout
    private lateinit var btnPrevMonth: TextView
    private lateinit var btnNextMonth: TextView
    private lateinit var tvSelectedDateLabel: TextView
    private lateinit var btnAddQuest: MaterialButton

    private var currentCalendar = Calendar.getInstance()
    private var selectedDate = Calendar.getInstance()
    
    private var allTasks: List<Task> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        
        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        calendarGrid = findViewById(R.id.calendarGrid)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        eventListContainer = findViewById(R.id.eventListContainer)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        tvSelectedDateLabel = findViewById(R.id.tvSelectedDateLabel)
        btnAddQuest = findViewById(R.id.btnAddQuest)
    }

    private fun setupListeners() {
        findViewById<TextView>(R.id.tabTimer).setOnClickListener {
            navigateTo(TimerActivity::class.java)
        }

        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        btnAddQuest.setOnClickListener {
            showAddQuestDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { showLoading(it) }
                }
                launch {
                    viewModel.tasks.collect { tasks ->
                        allTasks = tasks
                        updateCalendar()
                        showEventsForDate(selectedDate)
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@CalendarActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun updateCalendar() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        tvMonthYear.text = sdf.format(currentCalendar.time).uppercase()

        val childCount = calendarGrid.childCount
        if (childCount > 7) {
            calendarGrid.removeViews(7, childCount - 7)
        }

        val cal = currentCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until firstDayOfWeek) {
            val emptyView = View(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = (48 * resources.displayMetrics.density).toInt()
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
            }
            calendarGrid.addView(emptyView)
        }

        val today = Calendar.getInstance()

        for (day in 1..daysInMonth) {
            val dateCal = currentCalendar.clone() as Calendar
            dateCal.set(Calendar.DAY_OF_MONTH, day)
            
            val tasksForThisDay = allTasks.filter { 
                isSameDay(it.dueAt, dateCal.timeInMillis)
            }
            
            val hasEvents = tasksForThisDay.isNotEmpty()
            val isSelected = isSameDay(dateCal.timeInMillis, selectedDate.timeInMillis)
            val isToday = isSameDay(dateCal.timeInMillis, today.timeInMillis)

            val dayView = createDayView(day, isToday, isSelected, hasEvents, tasksForThisDay.firstOrNull()) {
                selectedDate = dateCal.clone() as Calendar
                updateCalendar()
                showEventsForDate(selectedDate)
            }
            calendarGrid.addView(dayView)
        }
    }

    private fun createDayView(day: Int, isToday: Boolean, isSelected: Boolean, hasEvents: Boolean, firstTask: Task?, onClick: () -> Unit): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = (48 * resources.displayMetrics.density).toInt()
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            setPadding(2, 2, 2, 2)
            
            when {
                isSelected -> setBackgroundColor(ContextCompat.getColor(context, R.color.accent_green))
                isToday -> setBackgroundColor(Color.parseColor("#333333"))
                else -> setBackgroundColor(Color.TRANSPARENT)
            }
            
            setOnClickListener { onClick() }
        }

        val tvDay = TextView(this).apply {
            text = day.toString()
            typeface = Typeface.MONOSPACE
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
        }
        container.addView(tvDay)

        if (hasEvents) {
            val tvIndicator = TextView(this).apply {
                text = when(firstTask?.description?.uppercase()) {
                    "QUEST" -> "⚔️"
                    "EVENT" -> "📌"
                    "BOSS" -> "👹"
                    else -> "•"
                }
                textSize = 8f
                gravity = Gravity.CENTER
                setTextColor(if (isSelected) Color.BLACK else ContextCompat.getColor(context, R.color.accent_yellow))
            }
            container.addView(tvIndicator)
        }

        return container
    }

    private fun showEventsForDate(date: Calendar) {
        val sdfLabel = SimpleDateFormat("MMMM dd", Locale.US)
        tvSelectedDateLabel.text = "QUESTS FOR ${sdfLabel.format(date.time).uppercase()}"

        eventListContainer.removeAllViews()
        val tasksForDate = allTasks.filter { isSameDay(it.dueAt, date.timeInMillis) }

        if (tasksForDate.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No quests for this day."
                typeface = Typeface.MONOSPACE
                setTextColor(Color.GRAY)
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, 40, 0, 0)
            }
            eventListContainer.addView(emptyTv)
            return
        }

        for (task in tasksForDate) {
            val eventView = layoutInflater.inflate(R.layout.item_calendar_event, eventListContainer, false)
            val icon = when(task.description.uppercase()) {
                "QUEST" -> "⚔️"
                "EVENT" -> "📌"
                "BOSS" -> "👹"
                else -> "⚔️"
            }
            eventView.findViewById<TextView>(R.id.tvEventIcon).text = icon
            eventView.findViewById<TextView>(R.id.tvEventTitle).text = task.title
            eventView.findViewById<TextView>(R.id.tvEventDay).text = "Day ${date.get(Calendar.DAY_OF_MONTH)}"
            eventView.findViewById<TextView>(R.id.tvEventType).text = task.description.uppercase()
            eventListContainer.addView(eventView)
        }
    }

    private fun showAddQuestDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_calendar_quest, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val tvDate = dialogView.findViewById<TextView>(R.id.tvDialogDate)
        val etTitle = dialogView.findViewById<EditText>(R.id.etQuestTitle)
        val btnTypeQuest = dialogView.findViewById<TextView>(R.id.btnTypeQuest)
        val btnTypeEvent = dialogView.findViewById<TextView>(R.id.btnTypeEvent)
        val btnTypeBoss = dialogView.findViewById<TextView>(R.id.btnTypeBoss)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSaveQuest)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
        tvDate.text = "Date: $dateStr"

        var selectedType = "QUEST"

        fun updateTypeSelection(type: String) {
            selectedType = type
            btnTypeQuest.setBackgroundResource(if (type == "QUEST") R.color.accent_green else R.drawable.bg_button_unselected)
            btnTypeQuest.setTextColor(if (type == "QUEST") Color.BLACK else Color.parseColor("#AAAAAA"))
            
            btnTypeEvent.setBackgroundResource(if (type == "EVENT") R.color.accent_green else R.drawable.bg_button_unselected)
            btnTypeEvent.setTextColor(if (type == "EVENT") Color.BLACK else Color.parseColor("#AAAAAA"))
            
            btnTypeBoss.setBackgroundResource(if (type == "BOSS") R.color.accent_green else R.drawable.bg_button_unselected)
            btnTypeBoss.setTextColor(if (type == "BOSS") Color.BLACK else Color.parseColor("#AAAAAA"))
        }

        btnTypeQuest.setOnClickListener { updateTypeSelection("QUEST") }
        btnTypeEvent.setOnClickListener { updateTypeSelection("EVENT") }
        btnTypeBoss.setOnClickListener { updateTypeSelection("BOSS") }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Enter a title hero!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addQuest(title, selectedType, selectedDate.timeInMillis)
            dialog.dismiss()
            Toast.makeText(this, "Quest recorded in the stars!", Toast.LENGTH_SHORT).show()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
