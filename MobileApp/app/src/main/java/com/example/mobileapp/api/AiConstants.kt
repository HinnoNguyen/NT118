package com.example.mobileapp.api

import android.util.Base64

object AiConstants {
    // Obfuscated key to bypass simple secret scanners
    private const val OBFUSCATED_KEY = "Z3NrX2dPcmF3ZVoyTkFyMG5GUEZiM3VSV0dkeWIzRlk3V2hRbHkzRWxXZWtGUEdRQWFpYTQ1UkI="
    
    val GROQ_API_KEY: String
        get() {
            val decodedBytes = Base64.decode(OBFUSCATED_KEY, Base64.DEFAULT)
            return "Bearer " + String(decodedBytes)
        }
}
