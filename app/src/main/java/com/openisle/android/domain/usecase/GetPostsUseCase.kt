package com.openisle.android.domain.usecase

import com.openisle.android.domain.model.Post
import com.openisle.android.domain.repository.PostRepository
// ▼▼▼ 修正 #1：添加对自定义 Result 密封类的导入 ▼▼▼
import com.openisle.android.domain.util.Result
import com.openisle.android.domain.util.Result.Error
import com.openisle.android.domain.util.Result.Success
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20,
        categoryId: Int? = null
    ): Result<List<Post>> { // 这里的 Result 现在指向我们自定义的密封类
        return try {
            require(page >= 1) { "Page must be >= 1" }
            require(pageSize >= 1) { "PageSize must be >= 1" }
            val posts = repository.getPosts(page, pageSize, categoryId)
            // ▼▼▼ 修正 #2：使用 Success 数据类的构造函数，而不是 Result.success() ▼▼▼
            Success(posts)
        } catch (e: Exception) {
            // ▼▼▼ 修正 #3：使用 Error 数据类的构造函数，而不是 Result.failure() ▼▼▼
            Error(e)
        }
    }
}