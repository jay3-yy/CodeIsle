package com.example.openisle.data

data class Reaction(
    val commentId: Any,
    val id: Int,
    val postId: Int,
    val type: String,
    val user: String
)