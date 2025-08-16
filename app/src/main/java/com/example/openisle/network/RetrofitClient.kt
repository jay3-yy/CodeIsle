package com.example.openisle.network // 确保包名和你的项目一致

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 服务器的基础 URL 地址
    private const val BASE_URL = "https://www.open-isle.com/api/"

    // 创建一个日志拦截器，方便在 Logcat 中查看网络请求的详细信息
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 创建 OkHttpClient，并添加日志拦截器
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // 使用 lazy 委托，确保 Retrofit 实例只在第一次使用时被创建
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 设置基础 URL
            .client(client) // 设置我们自定义的 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
            .build()
    }

    // 对外暴露的 ApiService 接口实例
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}