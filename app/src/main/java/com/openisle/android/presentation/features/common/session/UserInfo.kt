package com.openisle.android.presentation.common.session

// 用于封装用户信息的简单数据类
data class UserInfo(
    val id: Long,
    val name: String,
    val email: String,
    val avatarUrl: String?
)