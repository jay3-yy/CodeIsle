package com.example.openisle // 确保包名和你的项目一致

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 获取我们的“记事本” (SharedPreferences)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // 2. 从“记事本”里读取“是否已同意”的记录，如果找不到，默认为 false
        val hasAgreed = prefs.getBoolean("has_agreed_privacy", false)

        // 3. 判断是否已经同意过
        if (hasAgreed) {
            // 如果已经同意过，直接跳转到主界面
            navigateToMain()
        } else {
            // 如果是第一次运行，才显示欢迎页的布局
            setContentView(R.layout.activity_welcome)

            // 找到“同意”按钮
            val agreeButton: Button = findViewById(R.id.agreeButton)

            // 为按钮设置点击事件
            agreeButton.setOnClickListener {
                // 当用户点击时，在“记事本”里记下“已同意”
                prefs.edit().putBoolean("has_agreed_privacy", true).apply()

                // 然后跳转到主界面
                navigateToMain()
            }
        }
    }

    /**
     * 一个专门负责跳转到 MainActivity 的方法
     */
    private fun navigateToMain() {
        // 创建意图，准备跳转
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        // 关键一步：销毁当前的 WelcomeActivity
        // 这样用户在主界面按返回键时，就不会再回到这个欢迎页了
        finish()
    }
}