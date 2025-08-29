package com.example.openisle.presentation.features.postdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openisle.domain.model.Comment
import com.example.openisle.domain.model.Post
import com.example.openisle.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. 定义一个 UiState 数据类，用于封装此屏幕的所有状态
data class PostDetailUiState(
    val isLoading: Boolean = false,
    val post: Post? = null,
    val comments: List<Comment>? = null,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    // 2. 使用单个 StateFlow 来管理整个 UI 的状态
    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    // 3. 新增一个方法，用于统一获取帖子详情和评论
    fun fetchPostAndComments(postId: Long) {
        viewModelScope.launch {
            // 3.1 更新为加载状态
            _uiState.value = PostDetailUiState(isLoading = true)
            try {
                // 3.2 使用 async 并行获取帖子和评论，效率更高
                val postResult = async { repository.getPostById(postId) }
                val commentsResult = async { repository.getCommentsForPost(postId) }

                // 3.3 等待两个请求都完成后，更新为成功状态
                _uiState.value = PostDetailUiState(
                    isLoading = false,
                    post = postResult.await(),
                    comments = commentsResult.await()
                )
            } catch (e: Exception) {
                // 3.4 请求失败，更新为错误状态
                Log.e("PostDetailViewModel", "Failed to fetch post and comments", e)
                _uiState.value = PostDetailUiState(error = "加载失败: ${e.message}")
            }
        }
    }
}