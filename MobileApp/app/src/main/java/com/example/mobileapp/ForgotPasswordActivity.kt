package com.example.mobileapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.usecase.SendPasswordResetEmailUseCase
import com.example.mobileapp.util.ThemeUtils
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.example.mobileapp.util.LocaleHelper.onAttach(newBase))
    }

    private lateinit var sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        
        setupDependencies()
        setupEdgeToEdge()
        setupUI()
        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val btnThemeToggle = findViewById<android.widget.Button>(R.id.btnThemeToggle) ?: return
        
        btnThemeToggle.setOnClickListener {
            val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", true)
            val newDarkMode = !isDarkMode
            if (!ThemeUtils.toggleTheme(this, null, btnThemeToggle, newDarkMode, animate = false)) {
                Toast.makeText(this, "Please wait 5s before toggling again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDependencies() {
        val repository = UserRepositoryImpl()
        sendPasswordResetEmailUseCase = SendPasswordResetEmailUseCase(repository)
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUI() {
        val etEmail = findViewById<EditText>(R.id.etResetEmail)
        val btnSendReset = findViewById<MaterialButton>(R.id.btnSendReset)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)
        val cardContainer = findViewById<LinearLayout>(R.id.cardContainer)
        val successContainer = findViewById<LinearLayout>(R.id.successContainer)
        val btnSuccessDone = findViewById<MaterialButton>(R.id.btnSuccessDone)

        btnSendReset.setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isBlank()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSendReset.isEnabled = false
            btnSendReset.text = "SENDING..."

            lifecycleScope.launch {
                val result = sendPasswordResetEmailUseCase.execute(email)
                btnSendReset.isEnabled = true
                btnSendReset.text = "SEND NEW PASSWORD"

                result.onSuccess {
                    cardContainer.visibility = View.GONE
                    successContainer.visibility = View.VISIBLE
                }.onFailure {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        it.message ?: "Failed to send reset email",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        btnSuccessDone.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.stay)
        }

        tvBackToLogin.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.stay)
        }
    }
}
