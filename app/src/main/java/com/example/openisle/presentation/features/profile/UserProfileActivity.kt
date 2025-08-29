package com.example.openisle.presentation.features.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.openisle.R
import com.example.openisle.data.PostMeta
import com.example.openisle.databinding.ActivityUserProfileBinding
import com.example.openisle.presentation.features.postdetail.PostDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity(), UserPostsAdapter.OnPostClickListener {

    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var binding: ActivityUserProfileBinding
    private val userPostsAdapter by lazy { UserPostsAdapter(this) }
    private val userRepliesAdapter by lazy { UserRepliesAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerViews()
        observeViewModel()

        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId != -1) {
            viewModel.fetchUserProfile(userId.toString())
        } else {
            Toast.makeText(this, "无效的用户ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initRecyclerViews() {
        binding.postsRecyclerView.apply {
            adapter = userPostsAdapter
            layoutManager = LinearLayoutManager(this@UserProfileActivity)
            isNestedScrollingEnabled = false
        }

        binding.repliesRecyclerView.apply {
            adapter = userRepliesAdapter
            layoutManager = LinearLayoutManager(this@UserProfileActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.contentGroup.visibility = if (state.isLoading || state.error != null) View.GONE else View.VISIBLE
                state.error?.let {
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = state.error
                }
                state.userProfile?.let { userAggregate ->
                    binding.errorTextView.visibility = View.GONE
                    val user = userAggregate.user
                    binding.profileAvatar.load(user.avatar) {
                        crossfade(true)
                        placeholder(R.drawable.placeholder_avatar)
                    }
                    binding.profileUsername.text = user.username
                    binding.profileIntroduction.text = user.introduction ?: "这位用户很神秘，什么也没留下..."
                    binding.profileLevel.text = getString(R.string.user_level_format, user.level)
                    binding.profileExpBar.max = user.maxExp
                    binding.profileExpBar.progress = user.exp
                    binding.statsPostCount.text = getString(R.string.stats_posts_format, user.postCount)
                    binding.statsCommentCount.text = getString(R.string.stats_comments_format, user.commentCount)
                    binding.statsLikeCount.text = getString(R.string.stats_likes_format, user.likeCount)
                    binding.profileJoinDate.text = getString(R.string.join_date_format, user.createdAt.substringBefore("T"))
                    userPostsAdapter.submitList(userAggregate.posts)
                    userRepliesAdapter.setData(userAggregate.replies)
                }
            }
        }
    }

    override fun onPostClick(post: PostMeta) {
        val intent = PostDetailActivity.newIntent(this, post.id)
        startActivity(intent)
    }
}