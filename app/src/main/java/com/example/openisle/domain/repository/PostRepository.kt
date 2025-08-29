package com.example.openisle.domain.repository

import com.example.openisle.data.SearchResult
import com.example.openisle.data.UserAggregate
import com.example.openisle.domain.model.Category
import com.example.openisle.domain.model.Comment
import com.example.openisle.domain.model.Post

interface PostRepository {
    suspend fun getPosts(page: Int, pageSize: Int, categoryId: Int?): List<Post>
    suspend fun getPostById(postId: Long): Post
    suspend fun getCategories(): List<Category>
    suspend fun getCommentsForPost(postId: Long): List<Comment>

    // ▼▼▼【添加下面这两个新方法】▼▼▼
    suspend fun getUserProfile(identifier: String): UserAggregate
    suspend fun searchGlobal(keyword: String): List<SearchResult>

}