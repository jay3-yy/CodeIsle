plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")  // 使用 KAPT 而不是 KSP
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.openisle"
    compileSdk = 35  // 更新到 35 以满足依赖要求

    defaultConfig {
        applicationId = "com.example.openisle"
        minSdk = 24
        targetSdk = 34  // 保持 targetSdk 为 34（运行时行为）
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // 启用核心库的 API 解糖功能，以支持在旧版安卓上使用 java.time.*
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
        // 强制使用 Kotlin 1.9，避免 2.0+ 版本的 KAPT 兼容性问题
        languageVersion = "1.9"
        apiVersion = "1.9"
    }

    // 启用 View Binding
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // 使用稳定的模糊效果库
    implementation("jp.wasabeef:blurry:4.0.1")

    implementation("androidx.core:core-ktx:1.12.0")

    // --- 核心界面组件 ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // --- 网络请求核心 ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // --- 异步处理 ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // --- 图片加载 ---
    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-svg:2.4.0")

    // --- Markdown 渲染库 ---
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:image-coil:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")

    // --- API 解糖 ---
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // --- 其他库 ---
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // --- Hilt 和 ViewModel 的依赖 ---
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")  // 使用 KAPT

    // --- 测试依赖 ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("org.jsoup:jsoup:1.17.2")
}

// KAPT 配置
kapt {
    correctErrorTypes = true
}