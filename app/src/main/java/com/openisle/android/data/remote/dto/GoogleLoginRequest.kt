package com.openisle.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequest(
    @SerializedName("idToken") // 确保和服务器 DTO 中的字段名一致
    val idToken: String
)