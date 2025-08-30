package com.example.openisle.presentation.features.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openisle.domain.model.Category
import com.example.openisle.domain.model.Post
import com.example.openisle.domain.usecase.GetCategoriesUseCase
import com.example.openisle.domain.usecase.GetPostsUseCase
import com.example.openisle.domain.util.Result
import com.example.openisle.domain.util.Result.Error
import com.example.openisle.domain.util.Result.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

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

    fun refreshPosts(newCategoryId: Int? = null) {
        // 如果指定了新的分类ID，则更新当前分类
        if (newCategoryId != null) {
            this.currentCategoryId = if (newCategoryId == -1) null else newCategoryId
        }

        // 重置分页参数
        currentPage = 1
        isLastPage = false

        Log.d("MainViewModel", "Refreshing posts for category: $currentCategoryId")

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val pageToLoad = if (currentCategoryId == null) {
                    val totalPages = 10
                    Random.nextInt(1, totalPages + 1)
                } else {
                    1
                }

                Log.d("MainViewModel", "Loading page: $pageToLoad for category: $currentCategoryId")

                val result = getPostsUseCase(page = pageToLoad, pageSize = 20, categoryId = currentCategoryId)

                when (result) {
                    is Success -> {
                        val posts = result.value
                        Log.d("MainViewModel", "Successfully loaded ${posts.size} posts")
                        _posts.value = posts

                        // 清除之前的错误信息
                        _error.value = ""
                    }
                    is Error -> {
                        val errorMessage = "Failed to load posts: ${result.exception.message}"
                        _error.value = errorMessage
                        Log.e("MainViewModel", "Load posts failed", result.exception)
                    }
                }

            } catch (e: Exception) {
                val errorMessage = "Unexpected error: ${e.message}"
                _error.value = errorMessage
                Log.e("MainViewModel", "Unexpected error in refreshPosts", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMorePosts() {
        if (_isLoadingMore.value == true || isLastPage) {
            Log.d("MainViewModel", "Skip loadMore: isLoading=${_isLoadingMore.value}, isLastPage=$isLastPage")
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                currentPage++
                Log.d("MainViewModel", "Loading more posts, page: $currentPage")

                val result = getPostsUseCase(page = currentPage, pageSize = 20, categoryId = currentCategoryId)

                when (result) {
                    is Success -> {
                        val newPosts = result.value
                        Log.d("MainViewModel", "Loaded ${newPosts.size} more posts")

                        if (newPosts.isNotEmpty()) {
                            val currentPosts = _posts.value ?: emptyList()
                            _posts.value = currentPosts + newPosts
                        } else {
                            isLastPage = true
                            Log.d("MainViewModel", "Reached last page")
                        }
                    }
                    is Error -> {
                        val errorMessage = "Failed to load more posts: ${result.exception.message}"
                        _error.value = errorMessage
                        Log.e("MainViewModel", "Load more posts failed", result.exception)
                        currentPage-- // 回退页码
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Unexpected error in loadMore: ${e.message}"
                _error.value = errorMessage
                Log.e("MainViewModel", "Unexpected error in loadMorePosts", e)
                currentPage-- // 回退页码
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun loadCategories() {
        Log.d("MainViewModel", "Loading categories...")

        viewModelScope.launch {
            try {
                val categoryList = getCategoriesUseCase()
                Log.d("MainViewModel", "Successfully loaded ${categoryList.size} categories")

                // 确保分类列表不为空，如果为空则添加默认的"所有文章"分类
                if (categoryList.isEmpty()) {
                    Log.w("MainViewModel", "Categories list is empty, adding default category")
                    // ▼▼▼【修正】使用正确的 Category 构造函数参数 ▼▼▼
                    val defaultCategory = Category(id = -1, name = "All Posts", description = "All posts", count = null, icon = "", smallIcon = "")
                    _categories.value = listOf(defaultCategory)
                } else {
                    // 检查是否已经有"All Posts"分类，如果没有则添加
                    val hasAllPostsCategory = categoryList.any { it.id == -1 || it.name.equals("All Posts", ignoreCase = true) }
                    val finalCategoryList = if (!hasAllPostsCategory) {
                        // ▼▼▼【修正】使用正确的 Category 构造函数参数 ▼▼▼
                        val allPostsCategory = Category(id = -1, name = "All Posts", description = "All posts", count = null, icon = "", smallIcon = "")
                        listOf(allPostsCategory) + categoryList
                    } else {
                        categoryList
                    }
                    _categories.value = finalCategoryList
                }

                // 清除错误信息
                _error.value = ""

            } catch (e: Exception) {
                val errorMessage = "Failed to load categories: ${e.message}"
                _error.value = errorMessage
                Log.e("MainViewModel", "Error loading categories", e)

                // 如果加载失败，至少提供一个默认分类
                // ▼▼▼【修正】使用正确的 Category 构造函数参数 ▼▼▼
                val defaultCategory = Category(id = -1, name = "All Posts", description = "All posts", count = null, icon = "", smallIcon = "")
                _categories.value = listOf(defaultCategory)
            }
        }
    }
}