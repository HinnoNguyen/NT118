package com.example.mobileapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.mobileapp.data.TimerManager
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.google.firebase.FirebaseApp

class MobileAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase explicitly
        FirebaseApp.initializeApp(this)
        
        // Initialize TimerManager with UserRepository
        TimerManager.initialize(UserRepositoryImpl())

        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", true)
        val targetMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }
    }
}
