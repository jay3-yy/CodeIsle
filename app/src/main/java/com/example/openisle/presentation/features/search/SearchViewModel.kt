package com.example.openisle.presentation.features.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openisle.data.SearchResult
import com.example.openisle.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun performSearch(keyword: String) {
        if (keyword.isBlank()) return
        viewModelScope.launch {
            try {
                _searchResults.postValue(repository.searchGlobal(keyword))
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed", e)
                _error.postValue("搜索失败: ${e.message}")
            }
        }
    }
}