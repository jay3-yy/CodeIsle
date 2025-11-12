package com.openisle.android.presentation.features.feed

import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.GravityCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.openisle.android.R
import com.openisle.android.databinding.ActivityMainBinding
import com.openisle.android.databinding.NavHeaderBinding
import com.openisle.android.presentation.common.session.SessionManager
import com.openisle.android.presentation.features.auth.LoginActivity
import com.openisle.android.presentation.features.profile.MyProfileActivity

class DrawerController(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val viewModel: MainViewModel,
    private val sessionManager: SessionManager
) {

    fun setup() {
        setupToolbarAndDrawer()
        setupCategoryChips()
        updateNavHeader()
    }

    private fun setupToolbarAndDrawer() {
        activity.setSupportActionBar(binding.toolbar)
        binding.navigationView.setNavigationItemSelectedListener(::onNavigationItemSelected)

        val toggle = ActionBarDrawerToggle(
            activity, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    fun updateNavHeader() {
        val headerView = binding.navigationView.getHeaderView(0)
        val headerBinding = NavHeaderBinding.bind(headerView)
        val userInfo = sessionManager.getUserInfo()

        if (sessionManager.isLoggedIn()) {
            headerBinding.userNameTextView.text = userInfo.name
            headerBinding.userEmailTextView.text = userInfo.email
            headerBinding.userAvatarImageView.load(userInfo.avatarUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_broken_image)
                transformations(CircleCropTransformation())
            }
            headerView.setOnClickListener {
                // 关键：补上 userId（Long）并按新签名传参
                activity.startActivity(
                    MyProfileActivity.newIntent(
                        context = activity,
                        userId = sessionManager.getUserId().toLong(),
                        name = userInfo.name,
                        email = userInfo.email,
                        avatarUrl = userInfo.avatarUrl
                    )
                )
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        } else {
            headerBinding.userNameTextView.text = userInfo.name
            headerBinding.userEmailTextView.text = userInfo.email
            headerBinding.userAvatarImageView.setImageResource(R.drawable.ic_account)
            headerView.setOnClickListener { navigateToLoginScreen() }
        }
    }

    private fun setupCategoryChips() {
        val headerView = binding.navigationView.getHeaderView(0)
        val chipGroup = headerView.findViewById<ChipGroup>(R.id.categoryChipGroup) ?: return

        viewModel.categories.observe(activity) { categories ->
            chipGroup.removeAllViews()
            categories.forEach { category ->
                val chip = Chip(ContextThemeWrapper(activity, R.style.Widget_App_Chip_Category)).apply {
                    text = category.name
                    setOnClickListener {
                        val categoryId = if (category.id == -1) null else category.id
                        activity.supportActionBar?.title =
                            if (categoryId == null) activity.getString(R.string.app_name) else category.name

                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                        binding.appBarLayout.setExpanded(true, true)
                        binding.postsRecyclerView.scrollToPosition(0)

                        viewModel.refreshPosts(newCategoryId = categoryId)
                    }
                }
                chipGroup.addView(chip)
            }
        }
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                if (sessionManager.isLoggedIn()) {
                    val userInfo = sessionManager.getUserInfo()
                    // 关键：补上 userId（Long）并按新签名传参
                    activity.startActivity(
                        MyProfileActivity.newIntent(
                            context = activity,
                            userId = sessionManager.getUserId().toLong(),
                            name = userInfo.name,
                            email = userInfo.email,
                            avatarUrl = userInfo.avatarUrl
                        )
                    )
                } else {
                    Toast.makeText(activity, "请先登录以查看资料", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_logout -> showLogoutConfirmationDialog()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(activity)
            .setTitle("退出")
            .setMessage("您确定要退出登录吗？")
            .setPositiveButton("确定") { _, _ -> performLogout() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun performLogout() {
        sessionManager.logout()
        navigateToLoginScreen()
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }
}
