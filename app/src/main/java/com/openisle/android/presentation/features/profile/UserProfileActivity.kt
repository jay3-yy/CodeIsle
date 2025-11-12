package com.openisle.android.presentation.features.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.openisle.android.R
import com.openisle.android.data.UserAggregate
import com.openisle.android.data.PostMeta
import com.openisle.android.databinding.ActivityUserProfileBinding
import com.openisle.android.presentation.features.common.ImageViewerActivity
import com.openisle.android.presentation.features.postdetail.PostDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity(), UserPostsAdapter.OnPostClickListener {

    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var binding: ActivityUserProfileBinding
    private val userPostsAdapter by lazy { UserPostsAdapter(this) }
    private val userRepliesAdapter by lazy { UserRepliesAdapter() }

    companion object {
        private const val EXTRA_USER_ID = "USER_ID"

        fun newIntent(context: Context, userId: Long): Intent {
            return Intent(context, UserProfileActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        observeViewModel()

        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (userId != -1L) {
            viewModel.fetchUserProfile(userId.toString())
        } else {
            Toast.makeText(this, "无效的用户ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerViews() {
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
                binding.progressBar.isVisible = state.isLoading
                binding.contentGroup.isGone = state.isLoading || state.error != null

                state.error?.let {
                    binding.errorTextView.isVisible = true
                    binding.errorTextView.text = it
                }

                state.userProfile?.let { userAggregate ->
                    binding.errorTextView.isGone = true
                    bindUserProfileData(userAggregate)
                }
            }
        }
    }

    private fun bindUserProfileData(userAggregate: UserAggregate) {
        val user = userAggregate.user

        binding.profileUsername.text = user.username
        binding.profileIntroduction.text =
            user.introduction ?: "这位用户很神秘，什么也没留下..."
        binding.profileLevel.text = getString(R.string.user_level_format, user.level)
        binding.profileJoinDate.text =
            getString(R.string.join_date_format, user.createdAt.substringBefore("T"))

        binding.profileAvatar.load(user.avatar) {
            crossfade(true)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
            transformations(CircleCropTransformation())
            allowHardware(false)
        }

        binding.statsPostCount.text = getString(R.string.stats_posts_format, user.postCount)
        binding.statsCommentCount.text = getString(R.string.stats_comments_format, user.commentCount)
        binding.statsLikeCount.text = getString(R.string.stats_likes_format, user.likeCount)

        binding.profileExpBar.max = user.maxExp
        binding.profileExpBar.progress = user.exp

        userPostsAdapter.submitList(userAggregate.posts)
        userRepliesAdapter.setData(userAggregate.replies)

        binding.profileAvatar.setOnClickListener { openImageViewer(user.avatar) }
    }

    override fun onPostClick(post: PostMeta) {
        val intent = PostDetailActivity.newIntent(this, post.id)
        startActivity(intent)
    }

    private fun openImageViewer(imageUrl: String) {
        val intent = ImageViewerActivity.newIntent(this, imageUrl)
        startActivity(intent)
    }
}
