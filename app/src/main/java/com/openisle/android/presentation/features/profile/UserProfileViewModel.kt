package com.openisle.android.presentation.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openisle.android.data.UserAggregate
import com.openisle.android.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException // ✅ 引入 HttpException
import java.io.IOException // ✅ 引入 IOException
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
            } catch (e: Exception) { // ✅ 捕获所有可能的异常
                // 请求失败，更新为错误状态
                Log.e("UserProfileViewModel", "Failed to fetch profile", e)

                // ✅ 根据异常类型生成更具体的错误信息
                val errorMessage = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            400 -> "请求无效，请检查参数" // Bad Request
                            404 -> "用户不存在" // Not Found
                            500 -> "服务器内部错误，请稍后重试" // Internal Server Error
                            else -> "网络请求失败: HTTP ${e.code()}"
                        }
                    }
                    is IOException -> "网络连接失败，请检查您的网络" // 例如无网络连接
                    else -> "加载用户资料失败: 未知错误"
                }
                _uiState.value = UserProfileUiState(error = errorMessage)
            }
        }
    }
}