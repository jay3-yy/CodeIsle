package com.example.openisle.domain.model

import com.example.openisle.data.Author

data class Comment(
    val id: Long,
    val content: String,
    val createdAt: String? = null,
    val author: Author? = null,
    val replies: List<Comment> = emptyList()
)