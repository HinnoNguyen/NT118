package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import com.example.mobileapp.data.TimerManager
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel : ViewModel() {

    val timeLeftMs: StateFlow<Long> = TimerManager.timeLeftMs
    val isTimerRunning: StateFlow<Boolean> = TimerManager.isTimerRunning
    val timerFinished: StateFlow<Boolean> = TimerManager.timerFinished

    fun setTimer(minutes: Int) {
        TimerManager.setTimer(minutes)
    }

    fun startTimer() {
        TimerManager.startTimer()
    }

    fun pauseTimer() {
        TimerManager.pauseTimer()
    }

    fun resetTimer() {
        TimerManager.resetTimer()
    }
    
    fun clearFinishedFlag() {
        TimerManager.clearFinishedFlag()
    }

    fun getTimerLengthMinutes(): Int {
        return TimerManager.getTimerLengthMinutes()
    }
}
