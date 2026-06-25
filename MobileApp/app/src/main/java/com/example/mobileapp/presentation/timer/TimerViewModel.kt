package com.example.mobileapp.presentation.timer

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel : ViewModel() {

    enum class Mode { WORK, BREAK }

    private var countDownTimer: CountDownTimer? = null

    private val _workMinutes = MutableStateFlow(25)
    val workMinutes: StateFlow<Int> = _workMinutes

    private val _breakMinutes = MutableStateFlow(5)
    val breakMinutes: StateFlow<Int> = _breakMinutes

    private val _mode = MutableStateFlow(Mode.WORK)
    val mode: StateFlow<Mode> = _mode

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _millisLeft = MutableStateFlow(25 * 60 * 1000L)
    val millisLeft: StateFlow<Long> = _millisLeft

    private val _totalMillis = MutableStateFlow(25 * 60 * 1000L)
    val totalMillis: StateFlow<Long> = _totalMillis

    val formattedTime: String
        get() {
            val m = (_millisLeft.value / 1000) / 60
            val s = (_millisLeft.value / 1000) % 60
            return "%02d:%02d".format(m, s)
        }

    fun setWorkMinutes(minutes: Int) {
        if (_isRunning.value) return
        _workMinutes.value = minutes
        if (_mode.value == Mode.WORK) resetToCurrentMode()
    }

    fun startPause() {
        if (_isRunning.value) pause() else start()
    }

    private fun start() {
        if (_millisLeft.value <= 0L) resetToCurrentMode()
        countDownTimer = object : CountDownTimer(_millisLeft.value, 100L) {
            override fun onTick(millisUntilFinished: Long) {
                _millisLeft.value = millisUntilFinished
            }
            override fun onFinish() {
                _millisLeft.value = 0L
                _isRunning.value = false
                switchMode()
            }
        }.start()
        _isRunning.value = true
    }

    private fun pause() {
        countDownTimer?.cancel()
        countDownTimer = null
        _isRunning.value = false
    }

    fun reset() {
        pause()
        resetToCurrentMode()
    }

    private fun resetToCurrentMode() {
        val ms = when (_mode.value) {
            Mode.WORK  -> _workMinutes.value * 60 * 1000L
            Mode.BREAK -> _breakMinutes.value * 60 * 1000L
        }
        _millisLeft.value = ms
        _totalMillis.value = ms
    }

    private fun switchMode() {
        _mode.value = if (_mode.value == Mode.WORK) Mode.BREAK else Mode.WORK
        resetToCurrentMode()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = TimerViewModel() as T
        }
    }
}
