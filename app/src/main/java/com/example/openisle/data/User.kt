package com.example.openisle.data

import com.google.gson.annotations.SerializedName

// 这是最终、与后端 API 完全匹配的 User 数据模型
data class User(
    val id: Int,
    val username: String,
    val avatar: String,
    val introduction: String?,
    val createdAt: String,

    // 使用 @SerializedName 将 JSON 中的字段名映射到我们的变量名
    @SerializedName("likesReceived")
    val likeCount: Int,

    @SerializedName("currentLevel")
    val level: Int,

    @SerializedName("experience")
    val exp: Int,

    @SerializedName("nextLevelExp")
    val maxExp: Int,

    // 这两个字段需要后端在 UserMapper 中添加对应的 setPostCount 和 setCommentCount 逻辑
    val postCount: Int,
    val commentCount: Int
)