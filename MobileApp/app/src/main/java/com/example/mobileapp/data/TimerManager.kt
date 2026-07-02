package com.example.mobileapp.data

import android.os.CountDownTimer
import com.example.mobileapp.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object TimerManager {

    private var userRepository: UserRepository? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(repository: UserRepository) {
        this.userRepository = repository
    }

    private var countDownTimer: CountDownTimer? = null

    private val _timeLeftMs = MutableStateFlow(25 * 60 * 1000L)
    val timeLeftMs: StateFlow<Long> = _timeLeftMs.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerFinished = MutableStateFlow(false)
    val timerFinished: StateFlow<Boolean> = _timerFinished.asStateFlow()

    private var currentTimerLengthMs = 25 * 60 * 1000L
    
    // Tracks accumulated milliseconds focused that haven't been awarded as minutes yet
    private var accumulatedFocusMs: Long = 0
    private var lastMillisUntilFinished: Long = 0

    fun setTimer(minutes: Int) {
        stopTimer()
        currentTimerLengthMs = minutes * 60 * 1000L
        _timeLeftMs.value = currentTimerLengthMs
        _timerFinished.value = false
        accumulatedFocusMs = 0
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        
        lastMillisUntilFinished = _timeLeftMs.value

        countDownTimer = object : CountDownTimer(_timeLeftMs.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Calculate how much time passed since last tick
                val diff = lastMillisUntilFinished - millisUntilFinished
                if (diff > 0) {
                    accumulatedFocusMs += diff
                }
                lastMillisUntilFinished = millisUntilFinished
                _timeLeftMs.value = millisUntilFinished
                
                // Award 1 EXP/Minute for every 60,000ms accumulated
                if (accumulatedFocusMs >= 60000) {
                    val minutesToAward = (accumulatedFocusMs / 60000).toInt()
                    awardFocusMinute(minutesToAward)
                    accumulatedFocusMs %= 60000
                }
            }

            override fun onFinish() {
                // Final accumulation
                val diff = lastMillisUntilFinished - 0
                if (diff > 0) {
                    accumulatedFocusMs += diff
                }
                
                _timeLeftMs.value = 0
                _isTimerRunning.value = false
                _timerFinished.value = true
                
                // Award any remaining accumulated time if it's significant (e.g. >= 30s)
                // or just clear the accumulation. For fairness, let's award one last minute
                // if we have at least 30s accumulated at the very end.
                if (accumulatedFocusMs >= 30000) {
                    awardFocusMinute(1)
                }
                accumulatedFocusMs = 0
                lastMillisUntilFinished = 0
            }
        }.start()

        _isTimerRunning.value = true
        _timerFinished.value = false
    }

    private fun awardFocusMinute(minutes: Int) {
        if (minutes <= 0) return
        val repository = userRepository ?: return
        val uid = repository.getCurrentUserId() ?: return
        scope.launch {
            repository.addFocusMinutes(uid, minutes)
        }
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isTimerRunning.value = false
        // We don't reset accumulatedFocusMs here, so it persists when we resume!
    }

    fun resetTimer() {
        stopTimer()
        _timeLeftMs.value = currentTimerLengthMs
        _timerFinished.value = false
        accumulatedFocusMs = 0
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
