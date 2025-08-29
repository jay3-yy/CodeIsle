package com.example.openisle.domain.usecase

import com.example.openisle.domain.model.Post
import com.example.openisle.domain.repository.PostRepository
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int, categoryId: Int?): List<Post> {
        return repository.getPosts(page, pageSize, categoryId)
    }
}