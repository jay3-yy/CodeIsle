package com.example.openisle.presentation.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openisle.data.UserAggregate
import com.example.openisle.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 定义一个UI State数据类，封装此屏幕的所有状态
data class UserProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserAggregate? = null,
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    // 使用单个 StateFlow 来管理整个UI的状态
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    // fetchUserProfile 方法负责更新 UiState
    fun fetchUserProfile(identifier: String) {
        viewModelScope.launch {
            // 更新为加载状态
            _uiState.value = UserProfileUiState(isLoading = true)
            try {
                // 请求成功，更新为成功状态
                val profile = repository.getUserProfile(identifier)
                _uiState.value = UserProfileUiState(userProfile = profile)
            } catch (e: Exception) {
                // 请求失败，更新为错误状态
                Log.e("UserProfileViewModel", "Failed to fetch profile", e)
                _uiState.value = UserProfileUiState(error = "加载用户资料失败: ${e.message}")
            }
        }
    }
}