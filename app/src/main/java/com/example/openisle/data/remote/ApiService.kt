package com.example.openisle.data.remote
import com.example.openisle.data.SearchResult
import com.example.openisle.domain.model.Category
import com.example.openisle.domain.model.Comment
import com.example.openisle.domain.model.Post
import com.example.openisle.data.UserAggregate // 导入我们新建的 UserAggregate 类
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("posts")
    suspend fun getPosts(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("categoryId") categoryId: Int?
    ): List<Post>

    @GET("posts/{postId}/comments")
    suspend fun getCommentsForPost(@Path("postId") postId: Long): List<Comment>

    @GET("categories")
    suspend fun getCategories(): List<Category>

    // --- 新增代码在这里 ---
    /**
     * 根据用户标识符（ID或用户名）获取用户的聚合信息
     * @param identifier 用户的 ID 或 username
     * @return 返回包含了用户所有主页信息的 UserAggregate 对象
     */
    @GET("users/{identifier}/all")
    suspend fun getUserProfile(@Path("identifier") identifier: String): UserAggregate
    // 在 ApiService.kt 文件里

    @GET("search/global")
    suspend fun searchGlobal(@Query("keyword") keyword: String): List<SearchResult>
    @GET("posts/{postId}")
    suspend fun getPostById(@Path("postId") postId: Long): Post
}