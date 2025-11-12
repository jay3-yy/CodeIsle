package com.openisle.android.presentation.features.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openisle.android.domain.model.Category
import com.openisle.android.domain.model.Post
import com.openisle.android.domain.usecase.GetCategoriesUseCase
import com.openisle.android.domain.usecase.GetPostsUseCase
import com.openisle.android.domain.util.Result.Error
import com.openisle.android.domain.util.Result.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    // ... LiveData 声明保持不变 ...
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing
    private val _isLoadingMore = MutableLiveData<Boolean>()
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var currentCategoryId: Int? = null
    private var currentPage: Int = 1
    private var isLastPage: Boolean = false
    private var isInitialLoad = true // 添加一个标志位来确保只在首次加载时串联调用

    // ... refreshPosts 和 loadMorePosts 方法保持不变 ...
    fun refreshPosts(newCategoryId: Int? = null) {
        // ...
        if (newCategoryId != null) {
            this.currentCategoryId = if (newCategoryId == -1) null else newCategoryId
        }
        currentPage = 1
        isLastPage = false

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val pageToLoad = if (currentCategoryId == null) {
                    val totalPages = 10
                    Random.nextInt(1, totalPages + 1)
                } else {
                    1
                }
                val result = getPostsUseCase(page = pageToLoad, pageSize = 20, categoryId = currentCategoryId)
                when (result) {
                    is Success -> {
                        _posts.value = result.value
                        _error.value = ""
                    }
                    is Error -> {
                        _error.value = "Failed to load posts: ${result.exception.message}"
                        Log.e("MainViewModel", "Load posts failed", result.exception)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                Log.e("MainViewModel", "Unexpected error in refreshPosts", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMorePosts() {
        if (_isLoadingMore.value == true || isLastPage) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                currentPage++
                val result = getPostsUseCase(page = currentPage, pageSize = 20, categoryId = currentCategoryId)
                when (result) {
                    is Success -> {
                        val newPosts = result.value
                        if (newPosts.isNotEmpty()) {
                            val currentPosts = _posts.value ?: emptyList()
                            _posts.value = currentPosts + newPosts
                        } else {
                            isLastPage = true
                        }
                    }
                    is Error -> {
                        _error.value = "Failed to load more posts: ${result.exception.message}"
                        currentPage--
                    }
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error in loadMore: ${e.message}"
                currentPage--
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categoryList = getCategoriesUseCase()
                _categories.value = categoryList
                _error.value = ""

                // 修改：仅在首次成功加载分类后，自动加载“所有文章”
                if (isInitialLoad) {
                    isInitialLoad = false
                    refreshPosts(newCategoryId = -1)
                }

            } catch (e: Exception) {
                _error.value = "Failed to load categories: ${e.message}"
                Log.e("MainViewModel", "Error loading categories", e)
            }
        }
    }
}