package com.openisle.android.data.remote

import com.openisle.android.data.SearchResult
import com.openisle.android.data.UserAggregate
import com.openisle.android.data.remote.dto.GoogleLoginRequest
import com.openisle.android.data.remote.dto.LoginResponse
import com.openisle.android.data.remote.dto.ReactionDto
import com.openisle.android.data.remote.dto.ReactionRequest
import com.openisle.android.data.remote.dto.UserDto
import com.openisle.android.domain.model.Category
import com.openisle.android.domain.model.Comment
import com.openisle.android.domain.model.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @GET("api/posts")
    suspend fun getPosts(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("categoryId") categoryId: Int?
    ): List<Post>

    @GET("api/posts/{postId}/comments")
    suspend fun getCommentsForPost(@Path("postId") postId: Long): List<Comment>

    @GET("api/categories")
    suspend fun getCategories(): List<Category>

    @GET("api/users/{identifier}/all")
    suspend fun getUserProfile(@Path("identifier") identifier: String): UserAggregate

    @GET("api/search/global")
    suspend fun searchGlobal(@Query("keyword") keyword: String): List<SearchResult>

    @GET("api/posts/{postId}")
    suspend fun getPostById(@Path("postId") postId: Long): Post

    @GET("api/reaction-types")
    suspend fun getReactionTypes(): List<String>

    @GET("api/users/me")
    suspend fun getMyProfile(@Header("Authorization") token: String): UserDto

    // ✅ 新增：为帖子添加回应的接口
    @POST("api/posts/{postId}/reactions")
    suspend fun reactToPost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Body request: ReactionRequest
    ): ReactionDto?
}