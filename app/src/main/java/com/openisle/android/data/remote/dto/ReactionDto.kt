package com.openisle.android.data.remote.dto

// 用于接收服务器返回的回应信息
data class ReactionDto(
    val id: Long,
    val type: String,
    val user: String,
    val postId: Long?,
    val commentId: Long?,
    var reward: Int = 0 // 后端代码显示可能会有奖励
)