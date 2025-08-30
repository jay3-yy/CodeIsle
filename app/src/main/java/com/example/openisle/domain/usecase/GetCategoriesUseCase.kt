package com.example.openisle.domain.usecase

import com.example.openisle.domain.model.Category
import com.example.openisle.domain.repository.PostRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: PostRepository
) {

    companion object {
        // 定义一个通用的 "All Posts" 分类，避免重复创建
        private val ALL_POSTS_CATEGORY = Category(
            id = -1,
            name = "All Posts",
            description = "All available posts",
            count = null,
            icon = "",
            smallIcon = ""
        )
    }

    suspend operator fun invoke(): List<Category> {
        return try {
            val remoteCategories = repository.getCategories()

            // 避免重复添加 "All Posts"
            val hasAllPosts = remoteCategories.any { it.name.equals("All Posts", ignoreCase = true) }

            if (hasAllPosts) {
                // 如果远程已有，则保持原顺序（或将其移到开头）
                remoteCategories.map { if (it.name.equals("All Posts", ignoreCase = true)) ALL_POSTS_CATEGORY else it }
                    .sortedBy { if (it.id == -1) 0 else 1 } // 把 "All Posts" 放在最前面
            } else {
                // 如果没有，则添加
                listOf(ALL_POSTS_CATEGORY) + remoteCategories
            }
        } catch (e: Exception) {
            // 错误时返回一个合理的默认结构
            listOf(ALL_POSTS_CATEGORY)
        }
    }
}