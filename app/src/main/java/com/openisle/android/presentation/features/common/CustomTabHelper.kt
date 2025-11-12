package com.openisle.android.presentation.features.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

object CustomTabHelper {
    fun openUrl(context: Context, url: String) {
        try {
            val builder = CustomTabsIntent.Builder()
            // 设置自定义标签页的颜色等
            // builder.setToolbarColor(ContextCompat.getColor(context, R.color.your_toolbar_color))
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            // 降级处理：如果 Custom Tabs 失败，使用默认浏览器
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (fallbackException: Exception) {
                // 如果连默认浏览器都无法打开，显示错误信息
                Toast.makeText(context, "无法打开链接: $url", Toast.LENGTH_SHORT).show()
            }
        }
    }
}