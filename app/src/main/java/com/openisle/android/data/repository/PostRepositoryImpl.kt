package com.openisle.android.data.repository

import com.openisle.android.data.SearchResult
import com.openisle.android.data.UserAggregate
import com.openisle.android.data.remote.ApiService
import com.openisle.android.data.remote.dto.ReactionDto
import com.openisle.android.data.remote.dto.ReactionRequest
import com.openisle.android.domain.model.Category
import com.openisle.android.domain.model.Comment
import com.openisle.android.domain.model.Post
import com.openisle.android.domain.repository.PostRepository
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PostRepository {

    override suspend fun getPosts(page: Int, pageSize: Int, categoryId: Int?): List<Post> {
        return apiService.getPosts(page, pageSize, categoryId)
    }

    // 建议将返回值改为可空 Post?
    override suspend fun getPostById(postId: Long): Post? {
        return try {
            apiService.getPostById(postId)
        } catch (e: Exception) {
            // 在找不到帖子或网络错误时返回 null，防止应用崩溃
            null
        }
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

    // ✅ 新增：实现回应/点赞的方法
    override suspend fun reactToPost(token: String, postId: Long, reactionType: String): ReactionDto? {
        val request = ReactionRequest(type = reactionType)
        // ViewModel 中传递过来的 token 已经包含了 "Bearer " 前缀
        return apiService.reactToPost(token, postId, request)
    }
}