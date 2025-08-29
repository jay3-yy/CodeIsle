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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random // 导入 Random 类

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private var currentPage = 1
    private var isLastPage = false
    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    fun refreshPosts(categoryId: Int? = null) {
        currentPage = 1
        isLastPage = false
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val totalPages = 10
                val randomPage = Random.nextInt(1, totalPages + 1)
                val postList = getPostsUseCase(page = randomPage, pageSize = 20, categoryId = categoryId)
                _posts.value = postList
            } catch (e: Exception) {
                _error.value = "Failed to load posts: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMorePosts(categoryId: Int? = null) {
        if (_isLoadingMore.value == true || isLastPage) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                currentPage++
                val newPosts = getPostsUseCase(page = currentPage, pageSize = 20, categoryId = categoryId)
                if (newPosts.isNotEmpty()) {
                    val currentPosts = _posts.value ?: emptyList()
                    _posts.value = currentPosts + newPosts
                } else {
                    isLastPage = true
                }
            } catch (e: Exception) {
                _error.value = "Failed to load more posts: ${e.message}"
                currentPage--
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                // 1. 从数据源获取原始分类列表
                val categoryList = getCategoriesUseCase()

                // ▼▼▼ 核心修改部分 ▼▼▼
                // 2. 先移除数据源中可能存在的 "All Posts"，再手动添加到第一位，确保唯一性
                val finalList = categoryList.toMutableList().apply {
                    removeAll { it.name.equals("All Posts", ignoreCase = true) }
                }

                // 3. 创建一个标准的 "All Posts" 选项
                val allPostsCategory = Category(id = -1, name = "All Posts", description = "", icon = null, smallIcon = null, count = null)

                // 4. 将我们自己的 "All Posts" 添加到列表的最前面
                finalList.add(0, allPostsCategory)

                // 5. 更新 LiveData
                _categories.value = finalList
                // ▲▲▲ 修改结束 ▲▲▲

            } catch (e: Exception) {
                _error.value = "Failed to load categories: ${e.message}"
            }
        }
    }
}