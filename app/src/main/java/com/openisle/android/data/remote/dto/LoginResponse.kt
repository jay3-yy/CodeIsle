package com.openisle.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String
    // 您可以根据需要，让服务器在成功时也返回用户信息，并在这里添加对应字段
    // val user: OpenIsleUser
)