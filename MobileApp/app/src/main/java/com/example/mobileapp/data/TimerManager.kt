package com.example.mobileapp.data

import android.os.CountDownTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerManager {

    private var countDownTimer: CountDownTimer? = null

    private val _timeLeftMs = MutableStateFlow(25 * 60 * 1000L)
    val timeLeftMs: StateFlow<Long> = _timeLeftMs.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerFinished = MutableStateFlow(false)
    val timerFinished: StateFlow<Boolean> = _timerFinished.asStateFlow()

    private var currentTimerLengthMs = 25 * 60 * 1000L

    fun setTimer(minutes: Int) {
        stopTimer()
        currentTimerLengthMs = minutes * 60 * 1000L
        _timeLeftMs.value = currentTimerLengthMs
        _timerFinished.value = false
    }

    fun startTimer() {
        if (_isTimerRunning.value) return

        countDownTimer = object : CountDownTimer(_timeLeftMs.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeftMs.value = millisUntilFinished
            }

            override fun onFinish() {
                _timeLeftMs.value = 0
                _isTimerRunning.value = false
                _timerFinished.value = true
            }
        }.start()

        _isTimerRunning.value = true
        _timerFinished.value = false
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isTimerRunning.value = false
    }

    fun resetTimer() {
        stopTimer()
        _timeLeftMs.value = currentTimerLengthMs
        _timerFinished.value = false
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        _isTimerRunning.value = false
    }
    
    fun clearFinishedFlag() {
        _timerFinished.value = false
    }

    fun getTimerLengthMinutes(): Int {
        return (currentTimerLengthMs / 60000).toInt()
    }
}
