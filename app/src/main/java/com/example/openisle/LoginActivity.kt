package com.example.openisle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            navigateToWelcome()
            return
        }

        setContentView(R.layout.activity_login)

        val googleLoginButton: Button = findViewById(R.id.googleLoginButton)
        val guestLoginButton: TextView = findViewById(R.id.guestLoginButton)

        val loginAction = {
            prefs.edit().putBoolean("is_logged_in", true).apply()
            navigateToWelcome()
        }

        googleLoginButton.setOnClickListener { loginAction() }
        guestLoginButton.setOnClickListener { loginAction() }
    }

    private fun navigateToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}