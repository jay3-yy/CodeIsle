package com.openisle.android.domain.repository

import com.openisle.android.data.SearchResult
import com.openisle.android.data.UserAggregate
import com.openisle.android.data.remote.dto.ReactionDto
import com.openisle.android.domain.model.Category
import com.openisle.android.domain.model.Comment
import com.openisle.android.domain.model.Post

interface PostRepository {
    suspend fun getPosts(page: Int, pageSize: Int, categoryId: Int?): List<Post>
    suspend fun getPostById(postId: Long): Post? // 建议将返回值改为可空 Post? 以处理找不到帖子的情况
    suspend fun getCategories(): List<Category>
    suspend fun getCommentsForPost(postId: Long): List<Comment>
    suspend fun getUserProfile(identifier: String): UserAggregate
    suspend fun searchGlobal(keyword: String): List<SearchResult>

    // ✅ 新增：添加回应/点赞的方法
    suspend fun reactToPost(token: String, postId: Long, reactionType: String): ReactionDto?
}