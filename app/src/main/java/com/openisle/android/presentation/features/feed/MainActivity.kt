package com.openisle.android.presentation.features.feed

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.graphics.ColorUtils
import androidx.core.util.Pair
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.MaterialColors
import com.openisle.android.R
import com.openisle.android.databinding.ActivityMainBinding
import com.openisle.android.domain.model.Post
import com.openisle.android.presentation.common.session.SessionManager
import com.openisle.android.presentation.features.common.ImageViewerActivity
import com.openisle.android.presentation.features.feed.adapters.OnPostClickListener
import com.openisle.android.presentation.features.feed.adapters.PostAdapter
import com.openisle.android.presentation.features.postdetail.PostDetailActivity
import com.openisle.android.presentation.features.profile.UserProfileActivity
import com.openisle.android.presentation.features.search.SearchActivity
import com.openisle.android.presentation.util.MarkdownHelper
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.BlurView
import io.noties.markwon.Markwon
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnPostClickListener {

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val previewMarkwon: Markwon by lazy { MarkdownHelper.getPreviewMarkwon(this) }
    private val postAdapter by lazy { PostAdapter(this, previewMarkwon) }

    private lateinit var drawerController: DrawerController
    private lateinit var postListController: PostListController

    private lateinit var bottomBlurView: BlurView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var drawerBlurView: BlurView

    private var isBottomBarVisible = true
    private var launchGuard = false

    // ✅ 用于跟踪是否已经播放过动画
    private var hasPlayedAnimation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupRealtimeBlur()
        setupDrawerBlur()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        drawerController = DrawerController(this, binding, viewModel, sessionManager)
        postListController = PostListController(
            this, binding, postAdapter, viewModel,
            onRequestShowBottomBar = { showBottomBar() },
            onRequestHideBottomBar = { hideBottomBar() }
        )

        applyWindowInsets()
        drawerController.setup()
        postListController.setup()
        setupBottomNavigationBar()
        setupSwipeRefresh()
        observeViewModel()
        handleOnBackPressed()
        viewModel.loadCategories()
    }

    override fun onResume() {
        super.onResume()
        matchBlurViewHeight()
    }

    // ▼▼▼ 修正点：恢复并调整 onStop() 方法 ▼▼▼
    /**
     * 当 Activity 变为不可见时（例如用户导航到 PostDetailActivity），
     * 我们重置动画标志。
     * 这样，当用户返回 MainActivity 时，
     * `observeViewModel` 会再次检测到 `hasPlayedAnimation` 为 false，
     * 从而重新触发帖子列表的加载动画。
     *
     * 注意：这会禁用从 PostDetailActivity 返回的共享元素动画，
     * 因为我们强制重播列表动画。
     */
    override fun onStop() {
        super.onStop()
        hasPlayedAnimation = false
    }
    // ▲▲▲ 修正点 ▲▲▲

    private fun initViews() {
        bottomNavigationView = binding.bottomNavigationView
        bottomBlurView = binding.bottomBlurView
        drawerBlurView = binding.drawerBlurView
    }

    private fun setupRealtimeBlur() {
        val radius = 18f
        val blurTarget = binding.postsTarget
        val overlayColor = ColorUtils.setAlphaComponent(
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.WHITE),
            0x66
        )
        bottomBlurView.setupWith(blurTarget, radius, true)
            .setFrameClearDrawable(window.decorView.background)
            .setOverlayColor(overlayColor)
    }

    private fun setupDrawerBlur() {
        val radius = 20f
        val blurTarget = binding.mainContentTarget
        val overlayColor = ColorUtils.setAlphaComponent(
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.WHITE),
            0x33
        )

        drawerBlurView.setupWith(blurTarget, radius, false)
            .setFrameClearDrawable(window.decorView.background)
            .setOverlayColor(overlayColor)

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                drawerBlurView.alpha = slideOffset
            }

            override fun onDrawerOpened(drawerView: View) {
                drawerBlurView.setBlurAutoUpdate(true)
            }

            override fun onDrawerClosed(drawerView: View) {
                drawerBlurView.visibility = View.GONE
                drawerBlurView.setBlurAutoUpdate(false)
            }

            override fun onDrawerStateChanged(newState: Int) {
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    drawerBlurView.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun matchBlurViewHeight() {
        bottomNavigationView.post {
            bottomBlurView.updateLayoutParams<ViewGroup.LayoutParams> {
                height = bottomNavigationView.height
            }
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = sys.top)
            binding.postsRecyclerView.updatePadding(bottom = sys.bottom + bottomNavigationView.height)
            bottomNavigationView.updatePadding(bottom = sys.bottom)
            bottomBlurView.updatePadding(bottom = sys.bottom)
            insets
        }
    }

    private fun showBottomBar(immediate: Boolean = false) {
        isBottomBarVisible = true
        val duration = if (immediate) 0L else 250L
        bottomNavigationView.animate().translationY(0f).setDuration(duration).start()
        bottomBlurView.animate().translationY(0f).setDuration(duration).start()
    }

    private fun hideBottomBar(immediate: Boolean = false) {
        isBottomBarVisible = false
        val duration = if (immediate) 0L else 200L
        val totalHeight = bottomNavigationView.height.toFloat()
        bottomNavigationView.animate().translationY(totalHeight).setDuration(duration).start()
        bottomBlurView.animate().translationY(totalHeight).setDuration(duration).start()
    }

    private fun setupBottomNavigationBar() {
        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnItemReselectedListener { /* Do nothing */ }

        bottomNavigationView.setOnItemSelectedListener { item ->
            performHaptic()
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_discover -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    if (sessionManager.isLoggedIn()) {
                        startActivity(UserProfileActivity.newIntent(this, sessionManager.getUserId()))
                    } else {
                        Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
                    }
                    false
                }
                R.id.nav_add_post, R.id.nav_notifications -> {
                    Toast.makeText(this, "该功能暂未开放", Toast.LENGTH_SHORT).show()
                    false
                }
                else -> false
            }
        }
    }

    private fun performHaptic() {
        bottomNavigationView.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.GESTURE_END
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
        )
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshPosts()
        }
    }

    /**
     * ✅ 核心优化：当帖子数据加载完成后，播放逐个显示的动画
     */
    private fun observeViewModel() {
        viewModel.posts.observe(this) { posts ->
            // 提交新数据
            postAdapter.submitList(posts)

            // ▼▼▼ 修正点：确保只要 hasPlayedAnimation 为 false 就播放动画，无论 posts 是否为空 ▼▼▼
            if (!hasPlayedAnimation) { // 现在只检查 hasPlayedAnimation
                // 延迟一帧，确保 RecyclerView 已经布局完成
                binding.postsRecyclerView.post {
                    animatePostItems()
                    hasPlayedAnimation = true //
                }
            }
        }

        viewModel.isRefreshing.observe(this) { binding.swipeRefreshLayout.isRefreshing = it }

        viewModel.error.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * ✅ 新增：为 RecyclerView 中的帖子项添加逐个显示的动画
     * 带有模糊效果和非线性缓动
     */
    private fun animatePostItems() {
        val recyclerView = binding.postsRecyclerView
        val layoutManager = recyclerView.layoutManager ?: return

        // 获取当前可见的所有项
        val firstVisiblePosition = 0
        val lastVisiblePosition = minOf(layoutManager.childCount - 1, 8) // 最多动画前9个项

        for (i in firstVisiblePosition..lastVisiblePosition) {
            val child = layoutManager.getChildAt(i) ?: continue

            // ▼▼▼ 修正点：增强动画参数 ▼▼▼

            // 设置初始状态：不可见、更小、偏移更多
            child.alpha = 0f
            child.scaleX = 0.9f // (原 0.92f)
            child.scaleY = 0.9f // (原 0.92f)
            child.translationY = 100f // (原 60f)

            // 计算延迟：每个项依次出现，间隔更长
            val delay = i * 70L // (原 50L)
            val duration = 600L // (原 500L)
            val interpolator = OvershootInterpolator(1.0f) // (原 0.8f)

            // 透明度动画
            child.animate()
                .alpha(1f)
                .setStartDelay(delay)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()

            // 缩放动画
            child.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delay)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()

            // 位移动画
            child.animate()
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()

            // ✅ 模糊效果：增加初始模糊半径
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ValueAnimator.ofFloat(40f, 0f).apply { // (原 25f)
                    this.duration = duration
                    startDelay = delay
                    addUpdateListener { animator ->
                        val blurRadius = animator.animatedValue as Float
                        child.setRenderEffect(
                            if (blurRadius > 0.5f) {
                                android.graphics.RenderEffect.createBlurEffect(
                                    blurRadius, blurRadius,
                                    android.graphics.Shader.TileMode.CLAMP
                                )
                            } else null
                        )
                    }
                    start()
                }
            }
            // ▲▲▲ 修正点结束 ▲▲▲
        }

        // 为新滚动到的项添加动画监听
        // 这部分保持不变，用于首次加载后滚动时的动画
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private val animatedPositions = mutableSetOf<Int>()

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 只在向下滚动时添加动画
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager ?: return

                    for (i in 0 until layoutManager.childCount) {
                        val child = layoutManager.getChildAt(i) ?: continue
                        val position = recyclerView.getChildAdapterPosition(child)

                        // 如果这个位置还没有动画过，并且刚刚进入屏幕
                        if (position != RecyclerView.NO_POSITION &&
                            !animatedPositions.contains(position) &&
                            position > 8) { // 只为首次加载后的新项添加动画

                            animatedPositions.add(position)

                            // 添加简化的动画（因为用户在滚动）
                            child.alpha = 0f
                            child.translationY = 40f

                            child.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(350)
                                .setInterpolator(OvershootInterpolator(0.5f))
                                .start()
                        }
                    }
                }
            }
        })
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
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
                postListController.toggleLayout(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPostClick(post: Post, sharedViews: Map<String, View>) {
        if (launchGuard) return
        launchGuard = true
        val intent = PostDetailActivity.newIntent(this, post.id)

        // ▼▼▼ 修正点：不使用共享元素动画开启 PostDetailActivity ▼▼▼
        // 因为我们希望返回时重播 MainActivity 的列表动画，所以禁用共享元素过渡，
        // 否则返回动画会与列表重新加载动画冲突。
        startActivity(intent) // 直接启动，不带 ActivityOptionsCompat
        // ▲▲▲ 修正点 ▲▲▲

        // 如果你希望有Activity进入/退出动画（比如默认的滑动动画），可以这样：
        // startActivity(intent)
        // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) // 假设你有这些动画资源

        window.decorView.postDelayed({ launchGuard = false }, 700)
    }

    override fun onAvatarClick(avatarUrl: String) {
        startActivity(ImageViewerActivity.newIntent(this, avatarUrl))
    }

    override fun onContentImageClick(imageUrl: String) {
        startActivity(ImageViewerActivity.newIntent(this, imageUrl))
    }

    override fun onUsernameClick(userId: Int) {
        startActivity(UserProfileActivity.newIntent(this, userId.toLong()))
    }
}

private fun String.toGravatarUrl(): String {
    if (this.isBlank()) return "https://www.gravatar.com/avatar/?d=mp&s=256"
    val md5 = MessageDigest.getInstance("MD5")
        .digest(this.trim().lowercase().toByteArray())
        .joinToString("") { b -> "%02x".format(b) }
    return "https://www.gravatar.com/$md5?d=identicon&s=256"
}

private fun Context.actionBarSizePx(): Int {
    val tv = TypedValue()
    return if (this.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, this.resources.displayMetrics)
    } else {
        (56 * this.resources.displayMetrics.density).toInt()
    }
}