package com.example.mobileapp

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        
        setupEdgeToEdge()
        setupUI()
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
            if (email.isNotEmpty()) {
                // TODO: Triển khai logic gửi email khôi phục mật khẩu (ví dụ qua Firebase)
                Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        tvBackToLogin.setOnClickListener {
            finish() // Quay lại màn hình Login
        }
    }
}
