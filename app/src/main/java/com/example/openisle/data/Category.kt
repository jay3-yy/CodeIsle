package com.example.openisle.data

data class Category(
    val count: Int?, // 确保这里的 Int 后面有一个问号 ?
    val description: String,
    val icon: String,
    val id: Int,
    val name: String,
    val smallIcon: String
)