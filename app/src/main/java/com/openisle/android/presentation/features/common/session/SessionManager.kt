package com.openisle.android.presentation.common.session

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.openisle.android.data.remote.dto.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 使用 Hilt 保证在整个应用中只有一个实例
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_AVATAR = "user_avatar"
    }

    fun saveUserInfo(token: String, user: UserDto, email: String?) {
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_AUTH_TOKEN, token)
            putLong(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.username)
            putString(KEY_USER_AVATAR, user.avatar)
            putString(KEY_USER_EMAIL, email)
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getUserInfo(): UserInfo {
        return UserInfo(
            id = getUserId(),
            name = prefs.getString(KEY_USER_NAME, "请先登录") ?: "请先登录",
            email = prefs.getString(KEY_USER_EMAIL, "点击登录") ?: "点击登录",
            avatarUrl = prefs.getString(KEY_USER_AVATAR, null)
        )
    }

    fun logout() {
        prefs.edit { clear() }
    }
}