package com.openisle.android.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 用于接收后端 /api/users/me 接口返回的用户信息
 */
data class UserDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("avatar")
    val avatar: String?
    // 您可以根据后端 UserDto 的实际结构添加更多字段
)