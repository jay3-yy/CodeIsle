package com.openisle.android.presentation.features.postdetail

import android.content.Context
import android.content.Intent
import android.graphics.text.LineBreaker // 1. 新增导入
import android.os.Build // 2. 新增导入
import android.os.Bundle
import android.text.Layout
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
// 3. 移除了未使用的 import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
// 4. 移除了未使用的 import coil.imageLoader
import coil.load
import com.openisle.android.R
import com.openisle.android.databinding.ActivityPostDetailBinding
import com.openisle.android.domain.model.Post
import com.openisle.android.presentation.features.common.CustomTabHelper
import com.openisle.android.presentation.features.common.ImageViewerActivity
import com.openisle.android.presentation.features.profile.UserProfileActivity
// 5. 移除了未使用的 import com.openisle.android.presentation.features.search.SearchActivity
import com.openisle.android.presentation.util.MarkdownHelper
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class PostDetailActivity : AppCompatActivity(), OnCommentClickListener {

    private val viewModel: PostDetailViewModel by viewModels()
    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var markwon: Markwon
    private lateinit var commentAdapter: CommentAdapter

    companion object {
        private const val EXTRA_POST_ID = "post_id"
        // ▼▼▼ 修正点：已修复 .class 语法错误 ▼▼▼
        fun newIntent(context: Context, postId: Long): Intent =
            Intent(context, PostDetailActivity::class.java).putExtra(EXTRA_POST_ID, postId)
        // ▲▲▲ 修正点 ▲▲▲
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.sharedElementsUseOverlay = false
        supportPostponeEnterTransition()

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 6. 修复：移除了不必要的 '?' 安全调用
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.doOnPreDraw { supportStartPostponedEnterTransition() }

        applyWindowInsets()
        setupToolbar()
        initializeMarkwon()
        setupRecyclerView()
        setupReadingProgress()

        hideReactionsUI()

        observeUiState()
        loadDataFromIntent()
    }

    // 7. 修复：抑制 'Markwon' 拼写误报
    @Suppress("SpellCheckingInspection")
    private fun initializeMarkwon() {
        markwon = MarkdownHelper.createWithLinkHandler(this) { url ->
            showLinkOptionsDialog(url)
        }

        binding.detailPostContent.apply {
            textSize = 17f
            setLineSpacing(8f, 1.5f)
            includeFontPadding = false
            letterSpacing = 0.01f

            // 8. 修复：处理 breakStrategy 的 API 兼容性
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                breakStrategy = LineBreaker.BREAK_STRATEGY_HIGH_QUALITY
            } else {
                @Suppress("DEPRECATION")
                breakStrategy = Layout.BREAK_STRATEGY_HIGH_QUALITY
            }
        }
    }

    private fun showLinkOptionsDialog(url: String) {
        val options = arrayOf("在应用内打开", "使用浏览器打开")
        AlertDialog.Builder(this)
            .setTitle(url)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> CustomTabHelper.openUrl(this, url)
                    1 -> {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        } catch (_: Exception) {
                            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updatePadding(top = sys.top)
            binding.contentScrollView.updatePadding(bottom = sys.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(this)
        binding.commentsRecyclerView.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(this@PostDetailActivity)
            isNestedScrollingEnabled = false
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private val hideProgress = Runnable {
        binding.readProgress.animate()
            .alpha(0f)
            .setDuration(300)
            .start()
    }

    private fun setupReadingProgress() {
        binding.readProgress.alpha = 0f
        binding.contentScrollView.viewTreeObserver.addOnScrollChangedListener {
            val v = binding.contentScrollView
            val max = (v.getChildAt(0).height - v.height).coerceAtLeast(1)
            val progress = (v.scrollY * 100f / max).toInt().coerceIn(0, 100)
            binding.readProgress.setProgressCompat(progress, true)
            binding.readProgress.animate()
                .alpha(1f)
                .setDuration(150)
                .start()
            binding.readProgress.removeCallbacks(hideProgress)
            binding.readProgress.postDelayed(hideProgress, 800)
        }
    }

    private fun loadDataFromIntent() {
        val postId = intent.getLongExtra(EXTRA_POST_ID, -1L)
        if (postId != -1L) viewModel.fetchPostAndData(postId)
        else {
            Toast.makeText(this, "无效的帖子ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState -> handleUiState(uiState) }
            }
        }
    }

    private fun handleUiState(uiState: PostDetailUiState) {
        binding.progressBar.isVisible = uiState.isLoading
        binding.contentScrollView.isVisible = !uiState.isLoading

        uiState.post?.let { post ->
            bindPostData(post)
            commentAdapter.setPostAuthorId(post.author.id)
            bindCommentsData(post)
            hideReactionsUI()
        }

        uiState.error?.let {
            if (it.isNotEmpty()) Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }

    private fun bindPostData(post: Post) {
        binding.detailPostTitle.text = post.title
        binding.detailAuthorUsername.text = post.author.username
        binding.detailPostTimestamp.text = post.createdAt.substringBefore("T")

        val cleanedContent = post.content.replace(Regex("[\\n\\r]{3,}"), "\n\n")

        binding.detailPostContent.alpha = 0f
        markwon.setMarkdown(binding.detailPostContent, cleanedContent)
        binding.detailPostContent.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(100)
            .start()

        binding.detailAuthorAvatar.load(post.author.avatar) {
            allowHardware(false)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
            crossfade(true)
            crossfade(250)
        }

        binding.detailAuthorAvatar.setOnClickListener { openImageViewer(post.author.avatar) }
        binding.detailAuthorUsername.setOnClickListener { openUserProfile(post.author.id) }
    }

    private fun hideReactionsUI() {
        binding.reactionsGroup.isVisible = false
        binding.availableReactionsTitle.isVisible = false
        binding.availableReactionsGroup.isVisible = false
        binding.reactionsGroup.removeAllViews()
        binding.availableReactionsGroup.removeAllViews()
    }

    private fun bindCommentsData(post: Post) {
        val safeComments = post.comments ?: emptyList()
        binding.commentsHeader.isVisible = safeComments.isNotEmpty()

        val commentListItems = safeComments.flatMap { mainComment ->
            val items = mutableListOf<CommentListItem>(CommentListItem.MainComment(mainComment))
            mainComment.replies.mapTo(items) { reply -> CommentListItem.Reply(reply) }
            items
        }

        commentAdapter.submitList(commentListItems)
    }

    private fun openImageViewer(imageUrl: String) {
        startActivity(ImageViewerActivity.newIntent(this, imageUrl))
    }

    private fun openUserProfile(userId: Int) {
        startActivity(UserProfileActivity.newIntent(this, userId.toLong()))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // 使用 finish() 来禁用共享元素返回动画
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAvatarClick(imageUrl: String) = openImageViewer(imageUrl)
    override fun onUsernameClick(userId: Int) = openUserProfile(userId)
}