package com.example.openisle.domain.usecase

import com.example.openisle.domain.model.Category
import com.example.openisle.domain.repository.PostRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: PostRepository
) {
    // 这个 UseCase 负责从仓库获取分类列表，并手动添加一个 “All Posts” 选项
    suspend operator fun invoke(): List<Category> {
        return try {
            val remoteCategories = repository.getCategories()
            // 在列表开头添加一个代表“全部”的分类对象
            val allCategory = Category(
                id = -1,
                name = "All Posts",
                description = "All available posts",
                count = null, // count 是可空的，所以给 null
                icon = "",     // 提供一个空的默认值
                smallIcon = "" // 提供一个空的默认值
            )
            listOf(allCategory) + remoteCategories
        } catch (e: Exception) {
            // 如果发生错误，也要返回一个结构正确的对象
            listOf(
                Category(
                    id = -1,
                    name = "All Posts",
                    description = "All available posts",
                    count = null,
                    icon = "",
                    smallIcon = ""
                )
            )
        }
    }
}