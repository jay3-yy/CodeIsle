// 在 data/repository/PostRepositoryImpl.kt 文件中
package com.example.openisle.data.repository

import com.example.openisle.data.SearchResult
import com.example.openisle.data.UserAggregate
import com.example.openisle.data.remote.ApiService // 修正了 import 路径
import com.example.openisle.domain.model.Category
import com.example.openisle.domain.model.Comment
import com.example.openisle.domain.model.Post
import com.example.openisle.domain.repository.PostRepository
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PostRepository {

    override suspend fun getPosts(page: Int, pageSize: Int, categoryId: Int?): List<Post> {
        return apiService.getPosts(page, pageSize, categoryId)
    }

    override suspend fun getCategories(): List<Category> {
        return apiService.getCategories()
    }

    override suspend fun getCommentsForPost(postId: Long): List<Comment> {
        return apiService.getCommentsForPost(postId)
    }

    override suspend fun getUserProfile(identifier: String): UserAggregate {
        return apiService.getUserProfile(identifier)
    }

    override suspend fun searchGlobal(keyword: String): List<SearchResult> {
        return apiService.searchGlobal(keyword)
    }

    // ▼▼▼ 补全这个缺失的方法实现 ▼▼▼
    override suspend fun getPostById(postId: Long): Post {
        return apiService.getPostById(postId)
    }
}