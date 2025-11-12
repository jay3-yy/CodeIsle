package com.openisle.android.domain.model

import com.openisle.android.data.Author

data class Comment(
    val id: Long,
    val content: String,
    val createdAt: String? = null,
    val author: Author? = null,
    val replies: List<Comment> = emptyList()
)