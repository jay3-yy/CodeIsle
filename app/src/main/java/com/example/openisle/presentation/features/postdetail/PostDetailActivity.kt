package com.example.openisle.presentation.features.postdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.openisle.R
import com.example.openisle.databinding.ActivityPostDetailBinding
import com.example.openisle.domain.model.Post
import com.example.openisle.presentation.features.profile.UserProfileActivity
import com.example.openisle.utils.EmojiManager
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import kotlinx.coroutines.launch
import androidx.core.view.WindowInsetsCompat

@AndroidEntryPoint
class PostDetailActivity : AppCompatActivity(), OnAvatarClickListener {

    private lateinit var binding: ActivityPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels()
    private val commentAdapter by lazy { CommentAdapter(this) }
    private var markwon: Markwon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ▼▼▼ 实现沉浸式效果的核心代码 ▼▼▼
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ▼▼▼ 调整系统栏颜色和边距 ▼▼▼
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true

        // 为根滚动视图添加监听器，动态设置 padding 防止内容被遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentScrollView) { view, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 将系统状态栏和导航栏的高度作为 padding
            view.updatePadding(
                top = systemBarInsets.top,
                bottom = systemBarInsets.bottom
            )
            insets
        }

        initRecyclerView()
        observeViewModel()

        val postId = intent.getLongExtra(EXTRA_POST_ID, -1L)
        if (postId == -1L) {
            Toast.makeText(this, "无效的帖子ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.fetchPostAndComments(postId)
    }

    private fun initRecyclerView() {
        binding.commentsRecyclerView.adapter = commentAdapter
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.contentContainer.visibility = if (state.isLoading) View.INVISIBLE else View.VISIBLE

                state.error?.let {
                    Toast.makeText(this@PostDetailActivity, it, Toast.LENGTH_LONG).show()
                }

                state.post?.let { post ->
                    bindPostData(post)
                }

                state.comments?.let { comments ->
                    val flattenedList = mutableListOf<CommentListItem>()
                    comments.forEach { mainComment ->
                        flattenedList.add(CommentListItem.MainComment(mainComment))
                        mainComment.replies.forEach { reply ->
                            flattenedList.add(CommentListItem.Reply(reply))
                        }
                    }
                    commentAdapter.submitList(flattenedList)
                }
            }
        }
    }

    private fun bindPostData(post: Post) {
        binding.detailPostTitle.text = post.title
        binding.detailAuthorUsername.text = post.author.username
        binding.detailAuthorAvatar.load(post.author.avatar) {
            crossfade(true)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
        }
        binding.detailPostTimestamp.text = "· " + post.createdAt.substringBefore("T")

        binding.detailAuthorAvatar.setOnClickListener {
            onAvatarClick(post.author.id)
        }
        binding.detailAuthorUsername.setOnClickListener {
            onAvatarClick(post.author.id)
        }

        if (markwon == null) {
            markwon = Markwon.builder(this)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(CoilImagesPlugin.create(this))
                .build()
        }
        val emojiSize = (binding.detailPostContent.textSize * 1.2F).toInt()
        val processedContent = EmojiManager.replaceEmojisWithHtml(post.content, emojiSize)
        markwon?.setMarkdown(binding.detailPostContent, processedContent)
    }

    override fun onAvatarClick(authorId: Int) {
        val intent = Intent(this, UserProfileActivity::class.java).apply {
            putExtra("USER_ID", authorId)
        }
        startActivity(intent)
    }

    companion object {
        private const val EXTRA_POST_ID = "POST_ID"

        fun newIntent(context: Context, postId: Long): Intent {
            return Intent(context, PostDetailActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
            }
        }
    }
}