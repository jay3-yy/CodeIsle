package com.example.openisle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.openisle.adapters.CommentAdapter
import com.example.openisle.network.RetrofitClient
import com.example.openisle.utils.EmojiManager
import com.google.android.material.imageview.ShapeableImageView
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentAdapter
    private val tag = "PostDetailActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // 1. 找到所有视图
        val titleTextView: TextView = findViewById(R.id.detailPostTitle)
        val authorTextView: TextView = findViewById(R.id.detailAuthorUsername)
        val contentTextView: TextView = findViewById(R.id.detailPostContent)
        val avatarImageView: ShapeableImageView = findViewById(R.id.detailAuthorAvatar)
        val commentsRecyclerView: RecyclerView = findViewById(R.id.commentsRecyclerView)

        // 2. 获取 Intent 数据
        val title = intent.getStringExtra("POST_TITLE")
        val author = intent.getStringExtra("POST_AUTHOR")
        val content = intent.getStringExtra("POST_CONTENT")
        val avatarUrl = intent.getStringExtra("POST_AVATAR_URL")
        val postId = intent.getLongExtra("POST_ID", -1L)

        // 3. 设置帖子详情
        titleTextView.text = title
        authorTextView.text = author
        avatarImageView.load(avatarUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
        }

        // 4. 初始化 Markwon
        val markwon = Markwon.builder(this)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(CoilImagesPlugin.create(this))
            .build()

        // 5. 预处理并渲染内容
        val emojiSize = (contentTextView.textSize * 1.2F).toInt()
        val processedContent = EmojiManager.replaceEmojisWithHtml(content ?: "", emojiSize)
        markwon.setMarkdown(contentTextView, processedContent)

        // 6. 设置评论区
        commentAdapter = CommentAdapter()
        commentsRecyclerView.adapter = commentAdapter
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        // 7. 获取评论
        if (postId != -1L) {
            fetchComments(postId)
        }

        // --- 新增代码在这里 ---
        // 8. 为顶部的作者头像设置点击事件
        avatarImageView.setOnClickListener {
            // "author" 变量里存的就是作者名
            val intent = Intent(this, UserProfileActivity::class.java).apply {
                putExtra(UserProfileActivity.EXTRA_USER_IDENTIFIER, author)
            }
            startActivity(intent)
        }
    }

    private fun fetchComments(postId: Long) {
        lifecycleScope.launch {
            try {
                val comments = RetrofitClient.apiService.getCommentsForPost(postId)
                commentAdapter.submitList(comments)
            } catch (e: Exception) {
                Log.e(tag, "获取评论失败", e)
            }
        }
    }
}