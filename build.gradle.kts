// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // 官方插件别名，保持不变
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // ✅ 新增/确保这一行存在，用于定义 Hilt 插件
    id("com.google.dagger.hilt.android") version "2.48" apply false
}