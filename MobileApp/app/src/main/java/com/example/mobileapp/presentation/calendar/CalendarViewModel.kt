package com.example.mobileapp.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.repository.FirestoreEventRepositoryImpl
import com.example.mobileapp.domain.model.Event
import com.example.mobileapp.domain.repository.EventRepository
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(private val eventRepository: EventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate

    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear

    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0-based
    val currentMonth: StateFlow<Int> = _currentMonth

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun selectDate(userId: String, year: Int, month: Int, day: Int) {
        val cal = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _selectedDate.value = cal.timeInMillis
        loadEventsForDate(userId, cal.timeInMillis)
    }

    fun loadEventsForDate(userId: String, date: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val dayEnd = cal.timeInMillis

        viewModelScope.launch {
            when (val result = eventRepository.getEventsForDate(userId, dayStart, dayEnd)) {
                is Resource.Success -> _events.value = result.data
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> {}
            }
        }
    }

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _currentYear.value)
            set(Calendar.MONTH, _currentMonth.value)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, -1)
        }
        _currentYear.value = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _currentYear.value)
            set(Calendar.MONTH, _currentMonth.value)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, 1)
        }
        _currentYear.value = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
    }

    fun addEvent(userId: String, title: String, time: String, description: String) {
        if (title.isBlank()) return
        val date = _selectedDate.value
        viewModelScope.launch {
            when (val result = eventRepository.addEvent(userId, title, time, description, date)) {
                is Resource.Success -> loadEventsForDate(userId, date)
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteEvent(userId: String, eventId: String) {
        viewModelScope.launch {
            eventRepository.deleteEvent(userId, eventId)
            _events.value = _events.value.filter { it.id != eventId }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CalendarViewModel(FirestoreEventRepositoryImpl()) as T
        }
    }
}
