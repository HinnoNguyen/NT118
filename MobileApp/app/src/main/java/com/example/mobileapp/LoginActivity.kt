package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.LoginViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.example.mobileapp.util.ThemeUtils
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        setupEdgeToEdge()
        setupUI()
        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val btnThemeToggle = findViewById<Button>(R.id.btnThemeToggle)

        btnThemeToggle.setOnClickListener {
            val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", true)
            val newDarkMode = !isDarkMode
            if (!ThemeUtils.toggleTheme(this, null, btnThemeToggle, newDarkMode, animate = false)) {
                Toast.makeText(this, "Please wait 5s before toggling again", Toast.LENGTH_SHORT).show()
            }
        }
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
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            
            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, pass)
            }
        }

        btnRegisterTab.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
            finish()
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    handleLoginState(state, btnLogin)
                }
            }
        }
    }

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(this)
        
        // TODO: Replace with your actual Web Client ID from Firebase Console
        val webClientId = getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )
                
                val credential = result.credential
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                }
            } catch (e: GetCredentialException) {
                Toast.makeText(this@LoginActivity, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLoginState(state: LoginViewModel.LoginState, btnLogin: Button) {
        when (state) {
            is LoginViewModel.LoginState.Loading -> {
                btnLogin.isEnabled = false
                btnLogin.text = getString(R.string.btn_loading)
                findViewById<View>(R.id.main)?.let { root ->
                    if (root.findViewById<View>(R.id.loadingOverlay) == null) {
                        val overlay = layoutInflater.inflate(R.layout.layout_loading_overlay, root as ViewGroup, false)
                        root.addView(overlay)
                    }
                    root.findViewById<View>(R.id.loadingOverlay)?.visibility = View.VISIBLE
                }
            }
            is LoginViewModel.LoginState.Success -> {
                findViewById<View>(R.id.loadingOverlay)?.visibility = View.GONE
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
                Toast.makeText(this, "Welcome Hero!", Toast.LENGTH_SHORT).show()
                BaseActivity.resetNavigationState()
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
            is LoginViewModel.LoginState.Error -> {
                findViewById<View>(R.id.loadingOverlay)?.visibility = View.GONE
                btnLogin.isEnabled = true
                btnLogin.text = getString(R.string.btn_login)
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
}
