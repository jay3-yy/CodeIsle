package com.openisle.android.domain.model

import com.openisle.android.data.Author
import com.openisle.android.data.Reaction
import com.openisle.android.data.Tag
import org.jsoup.Jsoup

data class Post(
    val author: Author,
    val category: Category,
    val commentCount: Int,
    val content: String,
    val createdAt: String,
    val id: Long,
    val lastReplyAt: String,
    val pinnedAt: String,
    val reactions: List<Reaction>,
    val status: String,
    val tags: List<Tag>,
    val title: String,
    val views: Int,
    // ✅ 核心修复：添加缺失的 comments 属性
    val comments: List<Comment>?
) {
    /**
     * 计算属性：从 content (HTML) 中解析出第一张图片的 URL。
     */
    val coverImageUrl: String?
        get() {
            return try {
                val firstImage = Jsoup.parse(content).select("img").firstOrNull()
                firstImage?.attr("src")?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }
}