package com.openisle.android.presentation.features.common

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.openisle.android.R
import com.openisle.android.presentation.features.auth.LoginActivity
import com.openisle.android.presentation.features.feed.MainActivity

class WelcomeActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("app_prefs", MODE_PRIVATE) }
    private var routed = false  // 避免重复路由

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查用户是否已同意过隐私政策
        val hasAgreed = prefs.getBoolean("has_agreed_privacy", false)

        if (hasAgreed) {
            // 如果已同意，直接根据登录状态跳转
            routeToNextScreen()
        } else {
            // 如果未同意，显示新的欢迎页
            setContentView(R.layout.activity_welcome)

            // ✅ 修正：只为 "agreeButton" 设置监听器
            findViewById<Button>(R.id.agreeButton).setOnClickListener {
                // 记录用户已同意
                prefs.edit().putBoolean("has_agreed_privacy", true).apply()
                // 跳转到下一页
                routeToNextScreen()
            }
        }
    }

    private fun routeToNextScreen() {
        if (routed) return
        routed = true

        // 根据应用的 SharedPreferences 检查是否已登录
        // 注意：这里的 'is_logged_in' key 应该与您 LoginActivity 和 MainActivity 中使用的保持一致
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            // 如果已登录，进入主页
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 如果未登录，进入登录页
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish() // 结束当前欢迎页
    }
}