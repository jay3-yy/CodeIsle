package com.example.openisle.data

// 在用户主页里显示的简化的评论信息
data class CommentInfo(
    val id: Long,
    val content: String,
    val createdAt: String,
    val post: PostMeta // 嵌套一个简化的 Post，方便我们知道这条评论属于哪个帖子
)