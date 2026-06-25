package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.domain.model.Event
import com.example.mobileapp.presentation.calendar.CalendarViewModel
import com.example.mobileapp.utils.NavHelper
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {

    private val viewModel: CalendarViewModel by viewModels { CalendarViewModel.factory() }
    private val auth = FirebaseAuth.getInstance()

    // Keep track of selected day cell for highlight
    private var selectedDayCell: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        NavHelper.setup(this, NavHelper.Screen.TIME)

        setupTabNavigation()
        setupMonthNavigation()
        setupAddEventButton()
        observeViewModel()

        // Load events for today on start
        val uid = auth.currentUser?.uid ?: return
        viewModel.loadEventsForDate(uid, viewModel.selectedDate.value)
    }

    private fun setupTabNavigation() {
        findViewById<TextView>(R.id.tabTimer)?.setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
            finish()
        }
    }

    private fun setupMonthNavigation() {
        findViewById<TextView>(R.id.btnPrevMonth)?.setOnClickListener {
            viewModel.previousMonth()
            rebuildCalendarGrid()
        }
        findViewById<TextView>(R.id.btnNextMonth)?.setOnClickListener {
            viewModel.nextMonth()
            rebuildCalendarGrid()
        }
    }

    private fun setupAddEventButton() {
        findViewById<MaterialButton>(R.id.btnAddEvent)?.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.events.collect { events ->
                        renderEvents(events)
                    }
                }
                launch {
                    viewModel.error.collect { msg ->
                        msg?.let { Toast.makeText(this@CalendarActivity, it, Toast.LENGTH_SHORT).show() }
                    }
                }
                launch {
                    viewModel.currentMonth.collect {
                        rebuildCalendarGrid()
                    }
                }
            }
        }
    }

    private fun rebuildCalendarGrid() {
        val grid = findViewById<GridLayout>(R.id.calendarGrid) ?: return
        val tvMonth = findViewById<TextView>(R.id.tvCalendarMonth) ?: return

        val year = viewModel.currentYear.value
        val month = viewModel.currentMonth.value

        val monthNames = arrayOf(
            "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        )
        tvMonth.text = "${monthNames[month]} $year"

        // Remove all day cells (keep header row — first 7 children)
        while (grid.childCount > 7) {
            grid.removeViewAt(grid.childCount - 1)
        }

        // Compute first day of month (0=Sunday)
        val firstDayCal = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
        val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Today info
        val todayCal = Calendar.getInstance()
        val todayYear = todayCal.get(Calendar.YEAR)
        val todayMonth = todayCal.get(Calendar.MONTH)
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

        // Selected date info
        val selCal = Calendar.getInstance().apply { timeInMillis = viewModel.selectedDate.value }
        val selYear = selCal.get(Calendar.YEAR)
        val selMonth = selCal.get(Calendar.MONTH)
        val selDay = selCal.get(Calendar.DAY_OF_MONTH)

        val dp = resources.displayMetrics.density
        selectedDayCell = null

        // Total cells needed = 42 (6 rows × 7)
        for (i in 0 until 42) {
            val dayNumber = i - firstDayOfWeek + 1
            val isValidDay = dayNumber in 1..daysInMonth

            val cell = TextView(this).apply {
                val params = GridLayout.LayoutParams().apply {
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    width = 0
                    height = (48 * dp).toInt()
                    setMargins(1, 1, 1, 1)
                }
                layoutParams = params
                gravity = Gravity.CENTER
                typeface = android.graphics.Typeface.MONOSPACE
                textSize = 11f

                if (isValidDay) {
                    text = dayNumber.toString()
                    val isToday = (year == todayYear && month == todayMonth && dayNumber == todayDay)
                    val isSelected = (year == selYear && month == selMonth && dayNumber == selDay)

                    when {
                        isToday -> {
                            setBackgroundColor(0xFF57E389.toInt()) // accent_green
                            setTextColor(0xFF000000.toInt())
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                        }
                        isSelected -> {
                            setBackgroundColor(0xFF1A1A24.toInt())
                            setTextColor(0xFFFFFFFF.toInt())
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                        }
                        else -> {
                            setBackgroundColor(0xFF121217.toInt())
                            setTextColor(0xFFCCCCCC.toInt())
                        }
                    }

                    if (isSelected) selectedDayCell = this

                    val d = dayNumber
                    val uid = auth.currentUser?.uid
                    setOnClickListener {
                        // Remove highlight from previous selected
                        selectedDayCell?.let { prev ->
                            val prevIsToday = (year == todayYear && month == todayMonth
                                    && prev.text.toString().toIntOrNull() == todayDay)
                            if (prevIsToday) {
                                prev.setBackgroundColor(0xFF57E389.toInt())
                                prev.setTextColor(0xFF000000.toInt())
                            } else {
                                prev.setBackgroundColor(0xFF121217.toInt())
                                prev.setTextColor(0xFFCCCCCC.toInt())
                                prev.setTypeface(prev.typeface, android.graphics.Typeface.NORMAL)
                            }
                        }
                        // Highlight this cell
                        setBackgroundColor(0xFF1A1A24.toInt())
                        setTextColor(0xFFFFFFFF.toInt())
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        selectedDayCell = this

                        uid?.let { viewModel.selectDate(it, year, month, d) }
                    }
                } else {
                    text = ""
                    setBackgroundColor(0xFF121217.toInt())
                }
            }
            grid.addView(cell)
        }
    }

    private fun renderEvents(events: List<Event>) {
        val container = findViewById<LinearLayout>(R.id.eventListContainer) ?: return
        container.removeAllViews()
        val uid = auth.currentUser?.uid ?: return
        val dp = resources.displayMetrics.density

        if (events.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "[ NO EVENTS FOR THIS DAY ]"
                setTextColor(0xFF555555.toInt())
                typeface = android.graphics.Typeface.MONOSPACE
                textSize = 11f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (12 * dp).toInt()
                    bottomMargin = (12 * dp).toInt()
                }
            }
            container.addView(emptyView)
            return
        }

        events.forEach { event ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (12 * dp).toInt() }
                setBackgroundResource(R.drawable.bg_main_card)
                setPadding(
                    (12 * dp).toInt(), (12 * dp).toInt(),
                    (12 * dp).toInt(), (12 * dp).toInt()
                )
            }

            val icon = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (40 * dp).toInt(), (40 * dp).toInt()
                )
                setBackgroundColor(0xFF1A1A24.toInt())
                gravity = Gravity.CENTER
                text = "📅"
                textSize = 18f
            }

            val textBlock = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (12 * dp).toInt()
                }
            }

            val titleView = TextView(this).apply {
                text = event.title.ifBlank { "(no title)" }
                setTextColor(0xFF57E389.toInt())
                textSize = 13f
                typeface = android.graphics.Typeface.MONOSPACE
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val timeView = TextView(this).apply {
                text = if (event.time.isNotBlank()) event.time else "All day"
                setTextColor(0xFFAAAAAA.toInt())
                textSize = 10f
                typeface = android.graphics.Typeface.MONOSPACE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (2 * dp).toInt() }
            }

            val descView = if (event.description.isNotBlank()) {
                TextView(this).apply {
                    text = event.description
                    setTextColor(0xFF888888.toInt())
                    textSize = 10f
                    typeface = android.graphics.Typeface.MONOSPACE
                    maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = (2 * dp).toInt() }
                }
            } else null

            val deleteBtn = TextView(this).apply {
                text = "✕"
                setTextColor(0xFFFF4444.toInt())
                textSize = 14f
                setPadding((8 * dp).toInt(), 0, 0, 0)
                setOnClickListener { viewModel.deleteEvent(uid, event.id) }
            }

            textBlock.addView(titleView)
            textBlock.addView(timeView)
            descView?.let { textBlock.addView(it) }
            row.addView(icon)
            row.addView(textBlock)
            row.addView(deleteBtn)
            container.addView(row)
        }
    }

    private fun showAddEventDialog() {
        val dp = resources.displayMetrics.density
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * dp).toInt(), (16 * dp).toInt(), (20 * dp).toInt(), (8 * dp).toInt())
            setBackgroundColor(0xFF1A1A24.toInt())
        }

        val etTitle = EditText(this).apply {
            hint = "Event title *"
            setHintTextColor(0xFF555555.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.MONOSPACE
            setBackgroundColor(0xFF121217.toInt())
            setPadding((12 * dp).toInt(), (8 * dp).toInt(), (12 * dp).toInt(), (8 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (12 * dp).toInt() }
        }

        val etTime = EditText(this).apply {
            hint = "Time (HH:mm) — optional"
            setHintTextColor(0xFF555555.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.MONOSPACE
            setBackgroundColor(0xFF121217.toInt())
            setPadding((12 * dp).toInt(), (8 * dp).toInt(), (12 * dp).toInt(), (8 * dp).toInt())
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (12 * dp).toInt() }
        }

        val etDesc = EditText(this).apply {
            hint = "Description — optional"
            setHintTextColor(0xFF555555.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.MONOSPACE
            setBackgroundColor(0xFF121217.toInt())
            setPadding((12 * dp).toInt(), (8 * dp).toInt(), (12 * dp).toInt(), (8 * dp).toInt())
            minLines = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        dialogLayout.addView(etTitle)
        dialogLayout.addView(etTime)
        dialogLayout.addView(etDesc)

        AlertDialog.Builder(this)
            .setTitle("+ ADD EVENT")
            .setView(dialogLayout)
            .setPositiveButton("SAVE") { _, _ ->
                val uid = auth.currentUser?.uid ?: return@setPositiveButton
                val title = etTitle.text.toString().trim()
                if (title.isBlank()) {
                    Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.addEvent(uid, title, etTime.text.toString().trim(), etDesc.text.toString().trim())
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}
