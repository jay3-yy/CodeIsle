package com.openisle.android.presentation.features.feed.adapters

import android.view.View
import com.openisle.android.domain.model.Post

interface OnPostClickListener {
    /**
     * 当整个帖子卡片被点击时调用。
     * @param post 被点击的帖子对象。
     * @param sharedViews 一个包含用于过渡动画的视图及其 transitionName 的 Map。
     */
    fun onPostClick(post: Post, sharedViews: Map<String, View>)

    /**
     * 当作者头像被点击时调用。
     * @param avatarUrl 头像的 URL。
     */
    fun onAvatarClick(avatarUrl: String)

    /**
     * 当帖子内容中的图片被点击时调用。
     * @param imageUrl 图片的 URL。
     */
    fun onContentImageClick(imageUrl: String)

    /**
     * 当作者用户名被点击时调用。
     * @param userId 作者的用户 ID。
     */
    fun onUsernameClick(userId: Int)
}