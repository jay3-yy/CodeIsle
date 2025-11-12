package com.openisle.android.data

import com.openisle.android.domain.model.User

// API 返回的最外层数据模型，包含了所有信息
data class UserAggregate(
    val user: User,
    val posts: List<PostMeta>,
    val replies: List<CommentInfo>
)