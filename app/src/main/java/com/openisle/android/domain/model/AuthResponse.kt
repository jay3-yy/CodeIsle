package com.openisle.android.data.model

/**
 * 从后端接收的认证响应
 * @param token 后端生成的用于后续API请求的认证令牌 (JWT)
 */
data class AuthResponse(
    val token: String
)