package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.auth.LoginViewModel
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Auth screen — login.
 * Communicates only with [LoginViewModel]; never imports repositories or use cases.
 */
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels { LoginViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        setupEdgeToEdge()
        setupUI()
        observeViewModel()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUI() {
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnStartGame)
        val btnRegisterTab = findViewById<TextView>(R.id.btnRegisterTab)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)

        btnLogin.setOnClickListener {
            viewModel.login(
                etEmail.text.toString().trim(),
                etPassword.text.toString()
            )
        }

        btnRegisterTab.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Redirecting to reset password...", Toast.LENGTH_SHORT).show()
        }

        btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "Google Login coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        val btnLogin = findViewById<Button>(R.id.btnStartGame)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            btnLogin.isEnabled = false
                            btnLogin.text = "LOADING..."
                        }
                        is Resource.Success -> {
                            btnLogin.isEnabled = true
                            btnLogin.text = "LOGIN"
                            Toast.makeText(this@LoginActivity, "Welcome Hero!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        }
                        is Resource.Error -> {
                            btnLogin.isEnabled = true
                            btnLogin.text = "LOGIN"
                            Toast.makeText(this@LoginActivity, resource.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> { /* idle — do nothing */ }
                    }
                }
            }
        }
    }
}
