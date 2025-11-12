package com.openisle.android.presentation.features.postdetail

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openisle.android.data.remote.ApiService
import com.openisle.android.domain.model.Comment
import com.openisle.android.domain.model.Post
import com.openisle.android.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

data class PostDetailUiState(
    val isLoading: Boolean = false,
    val post: Post? = null,
    val comments: List<Comment>? = null,
    val availableReactions: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: PostRepository,
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    fun fetchPostAndData(postId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val postResult = async { repository.getPostById(postId) }
                val commentsResult = async { repository.getCommentsForPost(postId) }

                val post = postResult.await()
                if (post == null) {
                    throw IllegalStateException("服务器未找到该帖子 (ID: $postId)")
                }

                // ========== ✅ 崩溃修复点 ✅ ==========
                // 和评论一样，帖子的 content 也可能是 null
                val cleanedPost = post.copy(
                    content = (post.content ?: "").replace("\\!\\[", "!["), // <-- 添加空检查
                    pinnedAt = post.pinnedAt ?: ""
                )
                // ===================================

                _uiState.value = PostDetailUiState(
                    isLoading = false,
                    post = cleanedPost,
                    comments = commentsResult.await(), // 这里返回的 "Comment" 对象数据是空的
                    availableReactions = emptyList()
                )
            } catch (e: Exception) {
                Log.e("PostDetailViewModel", "Failed to fetch post data", e)
                // 发生错误时，将 comments 设为空列表，防止 Activity 读到旧数据
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载失败: ${e.message}",
                    comments = emptyList() // 确保评论列表被清空
                )
            }
        }
    }

    fun onReactToPost(postId: Long, reactionType: String) {
        _uiState.value = _uiState.value.copy(
            error = "表情回应功能暂未开放"
        )
    }
}