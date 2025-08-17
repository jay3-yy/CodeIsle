package com.example.openisle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.openisle.adapters.SearchAdapter
import com.example.openisle.network.RetrofitClient
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 显示返回按钮

        val recyclerView: RecyclerView = findViewById(R.id.searchResultsRecyclerView)
        searchAdapter = SearchAdapter()
        recyclerView.adapter = searchAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.isIconified = false // 直接展开搜索框
        searchView.queryHint = "搜索帖子、评论或用户..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // 当用户提交搜索时（按下回车）
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    performSearch(query)
                }
                return true
            }

            // 当搜索框文本变化时（我们暂时不做处理）
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        return true
    }

    // 处理 Toolbar 上的返回按钮点击事件
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun performSearch(keyword: String) {
        lifecycleScope.launch {
            try {
                val results = RetrofitClient.apiService.searchGlobal(keyword)
                searchAdapter.setData(results)
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}