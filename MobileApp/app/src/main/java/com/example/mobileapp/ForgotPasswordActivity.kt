package com.example.mobileapp

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.usecase.SendPasswordResetEmailUseCase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        
        setupDependencies()
        setupEdgeToEdge()
        setupUI()
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
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "If this email is registered, a password reset email has been sent.",
                        Toast.LENGTH_LONG
                    ).show()
                }.onFailure {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        it.message ?: "Failed to send reset email",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        tvBackToLogin.setOnClickListener {
            finish() // Quay lại màn hình Login
        }
    }
}
