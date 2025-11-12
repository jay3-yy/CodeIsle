// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // 官方插件别名，保持不变
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // ✅ 修正 Hilt 插件的 ID
    id("com.google.dagger.hilt.android") version "2.48" apply false
    //alias(libs.plugins.google.gms.google.services) apply false
}