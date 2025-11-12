package com.openisle.android.domain.model

data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val icon: String?, // 可为空
    val smallIcon: String?, // 可为空
    val count: Int? // 可为空
)