package com.example.openisle.presentation.features.feed

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.example.openisle.R
import com.example.openisle.domain.model.Post
import com.example.openisle.presentation.features.feed.adapters.CategoryAdapter
import com.example.openisle.presentation.features.feed.adapters.OnPostClickListener
import com.example.openisle.presentation.features.feed.adapters.PostAdapter
import com.example.openisle.presentation.features.postdetail.PostDetailActivity
import com.example.openisle.presentation.features.search.SearchActivity
import com.example.openisle.presentation.util.GridSpacingItemDecoration
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import com.example.openisle.presentation.features.feed.MainViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnPostClickListener {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var mainContent: ViewGroup
    private lateinit var blurImageView: ImageView

    private val postAdapter by lazy { PostAdapter(this) }
    private lateinit var categoryAdapter: CategoryAdapter

    private var isCardView = false
    private var isDrawerOpened = false

    private var gridSpacingDecoration: GridSpacingItemDecoration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        bindViews()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mainContent.updatePadding(left = systemBars.left, right = systemBars.right)
            navigationView.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            appBarLayout.updatePadding(top = systemBars.top)
            postRecyclerView.updatePadding(bottom = systemBars.bottom)
            insets
        }

        setupToolbarAndDrawer()
        setupRecyclerViews()
        setupSwipeRefresh()
        observeViewModel()
        handleOnBackPressed()

        // 修改：先加载分类，成功后再加载文章
        loadInitialData()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        appBarLayout = findViewById(R.id.appBarLayout)
        postRecyclerView = findViewById(R.id.postsRecyclerView)
        val headerView = navigationView.getHeaderView(0)
        categoryRecyclerView = headerView.findViewById(R.id.categoryRecyclerView)
        mainContent = findViewById(R.id.mainContent)
        blurImageView = findViewById(R.id.blurImageView)
        blurImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(toolbar)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = object : ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                if (slideOffset > 0 && !isDrawerOpened) {
                    isDrawerOpened = true
                    Blurry.with(this@MainActivity).radius(25).sampling(2).capture(mainContent).into(blurImageView)
                    blurImageView.visibility = View.VISIBLE
                }
                blurImageView.alpha = slideOffset
            }
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                isDrawerOpened = true
            }
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                blurImageView.visibility = View.GONE
                isDrawerOpened = false
            }
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupRecyclerViews() {
        // 设置文章 RecyclerView
        postRecyclerView.layoutManager = LinearLayoutManager(this)
        postRecyclerView.adapter = postAdapter
        val spacingInPixels = dpToPx(12)
        gridSpacingDecoration = GridSpacingItemDecoration(2, spacingInPixels, true)

        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager ?: return
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = when (layoutManager) {
                    is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
                    is StaggeredGridLayoutManager -> layoutManager.findFirstVisibleItemPositions(null).minOrNull() ?: 0
                    else -> return
                }

                if (viewModel.isLoadingMore.value == false) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 && firstVisibleItemPosition >= 0) {
                        viewModel.loadMorePosts()
                    }
                }
            }
        })

        // 设置分类 RecyclerView
        setupCategoryRecyclerView()
    }

    private fun setupCategoryRecyclerView() {
        // 确保分类 RecyclerView 有正确的布局管理器
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)

        // 初始化分类适配器
        categoryAdapter = CategoryAdapter { category ->
            val categoryId = if (category.id == -1) null else category.id
            supportActionBar?.title = if (categoryId == null) getString(R.string.app_name) else category.name
            viewModel.refreshPosts(newCategoryId = categoryId)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 设置适配器
        categoryRecyclerView.adapter = categoryAdapter

        // 添加一些调试信息
        categoryRecyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            android.util.Log.d("CategoryRV", "Category RecyclerView laid out: ${categoryRecyclerView.width} x ${categoryRecyclerView.height}")
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshPosts()
        }
        appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            swipeRefreshLayout.isEnabled = (verticalOffset == 0)
        }
    }

    private fun observeViewModel() {
        viewModel.posts.observe(this) { postList ->
            postAdapter.submitList(postList)
        }

        viewModel.categories.observe(this) { categoryList ->
            android.util.Log.d("MainActivity", "Categories received: ${categoryList.size}")
            categoryAdapter.submitList(categoryList)

            // 强制刷新分类 RecyclerView
            categoryRecyclerView.post {
                categoryAdapter.notifyDataSetChanged()
            }
        }

        viewModel.isRefreshing.observe(this) { isRefreshing ->
            swipeRefreshLayout.isRefreshing = isRefreshing
        }

        // 观察错误信息
        viewModel.error.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                android.util.Log.e("MainActivity", "Error: $errorMessage")
                // 这里可以显示 Toast 或 Snackbar
            }
        }
    }

    private fun loadInitialData() {
        // 先加载分类数据
        viewModel.loadCategories()

        // 然后加载默认文章（所有文章）
        viewModel.refreshPosts(newCategoryId = -1)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_toggle_layout -> {
                toggleLayout(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleLayout(menuItem: MenuItem) {
        isCardView = !isCardView
        val transition = TransitionSet().apply {
            addTransition(ChangeBounds().apply { interpolator = OvershootInterpolator(); duration = 400 })
            addTransition(Fade().setDuration(200))
        }
        TransitionManager.beginDelayedTransition(postRecyclerView.parent as ViewGroup, transition)

        if (isCardView) {
            postRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            postAdapter.currentViewType = PostAdapter.VIEW_TYPE_GRID
            menuItem.setIcon(R.drawable.ic_view_list)
            gridSpacingDecoration?.let { postRecyclerView.addItemDecoration(it) }
        } else {
            postRecyclerView.layoutManager = LinearLayoutManager(this)
            postAdapter.currentViewType = PostAdapter.VIEW_TYPE_LIST_CARD
            menuItem.setIcon(R.drawable.ic_view_module)
            gridSpacingDecoration?.let { postRecyclerView.removeItemDecoration(it) }
        }
        postAdapter.notifyItemRangeChanged(0, postAdapter.itemCount)
    }

    override fun onPostClick(post: Post) {
        val intent = PostDetailActivity.newIntent(this, post.id.toLong())
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}