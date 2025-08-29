package com.example.openisle.presentation.features.search

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.openisle.data.SearchResult
import com.example.openisle.databinding.ActivitySearchBinding
import com.example.openisle.presentation.features.postdetail.PostDetailActivity
import com.example.openisle.presentation.features.profile.UserProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
// ▼▼▼ 修正：直接实现 OnSearchResultClickListener 接口，去掉 "SearchAdapter." 前缀 ▼▼▼
class SearchActivity : AppCompatActivity(), OnSearchResultClickListener {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: ActivitySearchBinding
    private val searchAdapter by lazy { SearchAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.searchResultsRecyclerView.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.performSearch(it)
                }
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(this) { results ->
            searchAdapter.submitList(results)
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onSearchResultClick(result: SearchResult) {
        when (result.type) {
            "post" -> {
                val intent = PostDetailActivity.newIntent(this, result.id)
                startActivity(intent)
            }
            "user" -> {
                val intent = Intent(this, UserProfileActivity::class.java).apply {
                    putExtra("USER_ID", result.id.toInt())
                }
                startActivity(intent)
            }
            else -> {
                Toast.makeText(this, "未知的搜索结果类型", Toast.LENGTH_SHORT).show()
            }
        }
    }
}