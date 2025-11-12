package com.openisle.android.data.model

/**
 * 发送到后端的认证请求
 * @param idToken 从 Google 获取的用户身份令牌
 */
data class AuthRequest(
    val idToken: String
)