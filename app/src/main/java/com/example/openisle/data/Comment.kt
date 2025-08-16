package com.example.openisle.data // 确保包名和你的项目一致

// 我们复用之前创建过的 Author 类，因为评论的作者和帖子的作者结构应该是一样的
import com.example.openisle.data.Author

data class Comment(
    val id: Long,
    val content: String,
    val createdAt: String,
    val author: Author,
    val replies: List<Comment> // 用于接收子评论（回复）
)