package com.example.openisle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.openisle.adapters.UserPostsAdapter
import com.example.openisle.adapters.UserRepliesAdapter
import com.example.openisle.data.UserAggregate
import com.example.openisle.network.RetrofitClient
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class UserProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_IDENTIFIER = "user_identifier"
    }

    private val tag = "UserProfileActivity"
    private lateinit var postsAdapter: UserPostsAdapter
    private lateinit var repliesAdapter: UserRepliesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val userIdentifier = intent.getStringExtra(EXTRA_USER_IDENTIFIER)

        if (userIdentifier.isNullOrEmpty()) {
            Log.e(tag, "User identifier is missing!")
            finish()
            return
        }

        setupRecyclerViews()
        fetchUserProfile(userIdentifier)
    }

    private fun setupRecyclerViews() {
        val postsRecyclerView: RecyclerView = findViewById(R.id.postsRecyclerView)
        val repliesRecyclerView: RecyclerView = findViewById(R.id.repliesRecyclerView)

        postsAdapter = UserPostsAdapter()
        repliesAdapter = UserRepliesAdapter()

        postsRecyclerView.adapter = postsAdapter
        postsRecyclerView.layoutManager = LinearLayoutManager(this)

        repliesRecyclerView.adapter = repliesAdapter
        repliesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchUserProfile(identifier: String) {
        lifecycleScope.launch {
            try {
                val profileData = RetrofitClient.apiService.getUserProfile(identifier)
                updateUi(profileData)
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch user profile", e)
            }
        }
    }

    private fun updateUi(data: UserAggregate) {
        val avatar: ShapeableImageView = findViewById(R.id.profileAvatar)
        val username: TextView = findViewById(R.id.profileUsername)
        val level: TextView = findViewById(R.id.profileLevel)
        val expBar: ProgressBar = findViewById(R.id.profileExpBar)
        val introduction: TextView = findViewById(R.id.profileIntroduction)
        val joinDate: TextView = findViewById(R.id.profileJoinDate)
        val postCount: TextView = findViewById(R.id.statsPostCount)
        val commentCount: TextView = findViewById(R.id.statsCommentCount)
        val likeCount: TextView = findViewById(R.id.statsLikeCount)

        val user = data.user
        avatar.load(user.avatar) {
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
        }
        username.text = user.username
        introduction.text = user.introduction
        expBar.max = user.maxExp
        expBar.progress = user.exp

        level.text = getString(R.string.user_level_format, user.level)

        val formattedDate = if (user.createdAt.length >= 10) {
            user.createdAt.substring(0, 10).replace('-', '.')
        } else {
            "N/A"
        }
        joinDate.text = getString(R.string.join_date_format, formattedDate)

        postCount.text = getString(R.string.stats_posts_format, user.postCount)
        commentCount.text = getString(R.string.stats_comments_format, user.commentCount)
        likeCount.text = getString(R.string.stats_likes_format, user.likeCount)

        postsAdapter.setData(data.posts)
        repliesAdapter.setData(data.replies)
    }
}