package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.usecase.IsCurrentUserEmailVerifiedUseCase
import com.example.mobileapp.domain.usecase.LoginUseCase
import com.example.mobileapp.domain.usecase.ReloadCurrentUserUseCase
import com.example.mobileapp.domain.usecase.SignOutUseCase
import com.example.mobileapp.presentation.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        setupEdgeToEdge()
        setupViewModel()
        setupUI()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewModel() {
        val repository = UserRepositoryImpl()
        val loginUseCase = LoginUseCase(repository)
        val reloadCurrentUserUseCase = ReloadCurrentUserUseCase(repository)
        val isCurrentUserEmailVerifiedUseCase = IsCurrentUserEmailVerifiedUseCase(repository)
        val signOutUseCase = SignOutUseCase(repository)
        viewModel = LoginViewModel(
            loginUseCase,
            reloadCurrentUserUseCase,
            isCurrentUserEmailVerifiedUseCase,
            signOutUseCase
        )
    }

    private fun setupUI() {
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnStartGame)
        val btnRegisterTab = findViewById<TextView>(R.id.btnRegisterTab)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)

        intent.getStringExtra("prefill_email")?.let { email ->
            etEmail.setText(email)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            viewModel.login(email, pass)
        }

        btnRegisterTab.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "Google Login coming soon!", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    handleLoginState(state, btnLogin)
                }
            }
        }
    }

    private fun handleLoginState(state: LoginViewModel.LoginState, btnLogin: Button) {
        when (state) {
            is LoginViewModel.LoginState.Loading -> {
                btnLogin.isEnabled = false
                btnLogin.text = "LOADING..."
            }
            is LoginViewModel.LoginState.Success -> {
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
                Toast.makeText(this, "Welcome Hero!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            is LoginViewModel.LoginState.UnverifiedEmail -> {
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
            is LoginViewModel.LoginState.Error -> {
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
}
