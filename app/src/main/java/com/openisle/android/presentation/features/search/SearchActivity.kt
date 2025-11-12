package com.openisle.android.presentation.features.search

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.openisle.android.data.SearchResult
import com.openisle.android.databinding.ActivitySearchBinding
import com.openisle.android.presentation.features.postdetail.PostDetailActivity
import com.openisle.android.presentation.features.profile.MyProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                query?.let { viewModel.performSearch(it) }
                binding.searchView.clearFocus()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
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
                // 新签名：需要 userId（Long）+ name + email + avatarUrl
                val intent = MyProfileActivity.newIntent(
                    context = this,
                    userId = result.id.toLong(),
                    name = result.text,
                    email = result.subText,   // 若 subText 不是邮箱，这里也可以传简介
                    avatarUrl = result.avatarUrl
                )
                startActivity(intent)
            }
            else -> Toast.makeText(this, "未知的搜索结果类型", Toast.LENGTH_SHORT).show()
        }
    }
}
