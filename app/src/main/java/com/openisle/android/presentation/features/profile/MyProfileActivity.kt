package com.openisle.android.presentation.features.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.openisle.android.R

class MyProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        supportActionBar?.title = "我的资料"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val avatarImageView: ImageView = findViewById(R.id.avatarImageView)
        val nameTextView: TextView = findViewById(R.id.nameTextView)
        val emailTextView: TextView = findViewById(R.id.emailTextView)

        val userId   = intent.getLongExtra(EXTRA_USER_ID, -1L)
        val userName = intent.getStringExtra(EXTRA_USER_NAME)
        val userEmail = intent.getStringExtra(EXTRA_USER_EMAIL)
        val userAvatarUrl = intent.getStringExtra(EXTRA_USER_AVATAR_URL)

        if (userName != null) {
            nameTextView.text = userName
            emailTextView.text = userEmail

            avatarImageView.load(userAvatarUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
                transformations(CircleCropTransformation())
                allowHardware(false)
            }
        } else {
            nameTextView.text = "加载失败"
        }

        // 点击头像 / 姓名 / 邮箱 → 进入“用户详细资料页”
        val openDetail: () -> Unit = {
            if (userId > 0) {
                startActivity(UserProfileActivity.newIntent(this, userId))
            } else {
                Toast.makeText(this, "缺少用户ID，无法打开详细资料", Toast.LENGTH_SHORT).show()
            }
        }
        avatarImageView.setOnClickListener { openDetail() }
        nameTextView.setOnClickListener { openDetail() }
        emailTextView.setOnClickListener { openDetail() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_USER_NAME = "extra_user_name"
        private const val EXTRA_USER_EMAIL = "extra_user_email"
        private const val EXTRA_USER_AVATAR_URL = "extra_user_avatar_url"

        // 新签名：带上 userId，便于跳转到“用户详细资料页”
        fun newIntent(
            context: Context,
            userId: Long,
            name: String?,
            email: String?,
            avatarUrl: String?
        ): Intent {
            return Intent(context, MyProfileActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_USER_NAME, name)
                putExtra(EXTRA_USER_EMAIL, email)
                putExtra(EXTRA_USER_AVATAR_URL, avatarUrl)
            }
        }
    }
}
