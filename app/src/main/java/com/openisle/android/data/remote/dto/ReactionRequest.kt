package com.openisle.android.data.remote.dto

import com.google.gson.annotations.SerializedName

// 用于向服务器发送用户选择的回应类型
data class ReactionRequest(
    @SerializedName("type")
    val type: String // 例如 "LIKE", "FIRE"
)