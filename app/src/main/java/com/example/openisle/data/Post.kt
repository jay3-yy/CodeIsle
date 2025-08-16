package com.example.openisle.data

data class Post(
    val author: Author,
    val category: Category,
    val commentCount: Int,
    val content: String,
    val createdAt: String,
    val id: Int,
    val lastReplyAt: String,
    val pinnedAt: String,
    val reactions: List<Reaction>,
    val status: String,
    val tags: List<Tag>,
    val title: String,
    val views: Int
)