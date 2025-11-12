package com.openisle.android.presentation.features.feed

import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.openisle.android.R
import com.openisle.android.databinding.ActivityMainBinding
import com.openisle.android.presentation.features.feed.adapters.PostAdapter
import com.openisle.android.presentation.util.GridSpacingItemDecoration

class PostListController(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val postAdapter: PostAdapter,
    private val viewModel: MainViewModel,
    private val onRequestShowBottomBar: () -> Unit,
    private val onRequestHideBottomBar: () -> Unit
) {
    private var gridSpacingDecoration: GridSpacingItemDecoration? = null
    private var isGridView = false

    fun setup() {
        binding.postsRecyclerView.adapter = postAdapter
        binding.postsRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.postsRecyclerView.clipToPadding = false

        gridSpacingDecoration = GridSpacingItemDecoration(
            2, (12 * activity.resources.displayMetrics.density).toInt(), true
        )

        // ▼▼▼ 【核心修改】替换为基于滚动方向的监听逻辑 ▼▼▼
        binding.postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                // —— 仅保留分页加载逻辑 ——
                val lm = rv.layoutManager ?: return
                val total = lm.itemCount
                val lastVisible = when (lm) {
                    is LinearLayoutManager -> lm.findLastVisibleItemPosition()
                    is GridLayoutManager -> lm.findLastVisibleItemPosition()
                    else -> 0
                }
                val isLoading = viewModel.isLoadingMore.value ?: false
                if (!isLoading && lastVisible >= total - 5) {
                    viewModel.loadMorePosts()
                }
            }
        })

    }

    fun toggleLayout(menuItem: MenuItem) {
        // ... 此方法保持不变 ...
        TransitionManager.beginDelayedTransition(binding.postsRecyclerView.parent as ViewGroup)
        isGridView = !isGridView
        if (isGridView) {
            binding.postsRecyclerView.layoutManager = GridLayoutManager(activity, 2)
            // ✅ 修正：使用新的公共常量
            postAdapter.setViewType(PostAdapter.MODE_GRID)
            menuItem.setIcon(R.drawable.ic_view_list)
            gridSpacingDecoration?.let { binding.postsRecyclerView.addItemDecoration(it) }
        } else {
            binding.postsRecyclerView.layoutManager = LinearLayoutManager(activity)
            // ✅ 修正：使用新的公共常量
            postAdapter.setViewType(PostAdapter.MODE_LIST)
            menuItem.setIcon(R.drawable.ic_view_module)
            gridSpacingDecoration?.let { binding.postsRecyclerView.removeItemDecoration(it) }
        }
    }
}