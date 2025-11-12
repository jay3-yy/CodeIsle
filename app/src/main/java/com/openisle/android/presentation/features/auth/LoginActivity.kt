package com.openisle.android.presentation.features.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.openisle.android.R
import com.openisle.android.presentation.features.feed.MainActivity

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "app_prefs"
    }

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 谷歌登录按钮：点击时提示“敬请期待”
        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            showMessage("敬请期待")
        }

        // 游客模式按钮：点击时进入游客模式
        findViewById<Button>(R.id.btnGuestMode)?.setOnClickListener {
            enterGuestMode()
        }
    }

    private fun enterGuestMode() {
        // 访客模式清除所有可能的旧登录信息
        sharedPrefs.edit().clear().apply()
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}