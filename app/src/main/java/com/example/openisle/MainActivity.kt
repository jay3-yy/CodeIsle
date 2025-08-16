package com.example.openisle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.openisle.adapters.PostAdapter
import com.example.openisle.data.Post
import com.example.openisle.network.RetrofitClient
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var postLayoutManager: LinearLayoutManager
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private var selectedCategoryId: Int? = null
    private var currentPage = 0
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == 999) {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                prefs.edit().clear().apply()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            } else {
                selectedCategoryId = if (menuItem.itemId == -1) null else menuItem.itemId
                supportActionBar?.title = menuItem.title
                resetAndFetchPosts()
            }
            drawerLayout.closeDrawers()
            true
        }

        val postsRecyclerView: RecyclerView = findViewById(R.id.postsRecyclerView)
        postAdapter = PostAdapter()
        postLayoutManager = LinearLayoutManager(this)
        postsRecyclerView.adapter = postAdapter
        postsRecyclerView.layoutManager = postLayoutManager

        postAdapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: Post) {
                val intent = Intent(this@MainActivity, PostDetailActivity::class.java).apply {
                    putExtra("POST_TITLE", post.title)
                    putExtra("POST_AUTHOR", post.author.username)
                    putExtra("POST_CONTENT", post.content)
                    putExtra("POST_AVATAR_URL", post.author.avatar)
                    putExtra("POST_ID", post.id.toLong())
                }
                startActivity(intent)
            }
        })

        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = postLayoutManager.childCount
                val totalItemCount = postLayoutManager.itemCount
                val firstVisibleItemPosition = postLayoutManager.findFirstVisibleItemPosition()
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 && firstVisibleItemPosition >= 0) {
                        currentPage++
                        fetchPosts()
                    }
                }
            }
        })

        fetchCategories()
        fetchPosts()
    }

    private fun fetchCategories() {
        lifecycleScope.launch {
            try {
                val categories = RetrofitClient.apiService.getCategories()
                val menu = navigationView.menu
                menu.clear()
                menu.add(0, -1, 0, "全部")
                categories.forEach { category ->
                    menu.add(0, category.id, 0, category.name)
                }
                menu.add(Menu.CATEGORY_SECONDARY, 999, 100, "退出登录")
                supportActionBar?.title = "全部"
            } catch (e: Exception) {
                Log.e(tag, "获取分类失败", e)
            }
        }
    }

    private fun resetAndFetchPosts() {
        currentPage = 0
        isLastPage = false
        postAdapter.setData(emptyList())
        fetchPosts()
    }

    private fun fetchPosts() {
        if (isLoading) return
        isLoading = true
        Log.d(tag, "fetchPosts: Attempting to fetch page $currentPage for category $selectedCategoryId")
        lifecycleScope.launch {
            try {
                val newPosts = RetrofitClient.apiService.getPosts(currentPage, pageSize, selectedCategoryId)
                if (newPosts.isNotEmpty()) {
                    if (currentPage == 0) {
                        postAdapter.setData(newPosts)
                    } else {
                        postAdapter.addData(newPosts)
                    }
                } else {
                    if (currentPage > 0) isLastPage = true
                }
            } catch (e: Exception) {
                Log.e(tag, "获取数据失败", e)
            } finally {
                isLoading = false
            }
        }
    }
}