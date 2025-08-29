package com.example.openisle.domain.model

import com.example.openisle.data.Author
import com.example.openisle.data.Reaction
import com.example.openisle.data.Tag
import org.jsoup.Jsoup

data class Post(  // 修正：从 "ost" 改为 "Post"
    val author: Author,
    val category: Category,
    val commentCount: Int,
    val content: String,
    val createdAt: String,
    val id: Int,
    val lastReplyAt: String,
    val pinnedAt: String,
    val reactions: List<Reaction>,
    val status: String,
    val tags: List<Tag>,
    val title: String,
    val views: Int
) {
    /**
     * 计算属性：从 content (HTML) 中解析出第一张图片的 URL。
     * 此版本经过优化，即使没有图片或解析出错也能安全返回 null。
     */
    val coverImageUrl: String?
        get() {
            return try {
                // 使用 Jsoup 解析 content 字符串，并查找第一个 <img> 元素
                val firstImage = Jsoup.parse(content).select("img").firstOrNull()
                // 如果找到了图片，则返回它的 "src" 属性，否则安全地返回 null
                firstImage?.attr("src")?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                // 在任何异常情况下都保证返回 null，防止应用崩溃
                null
            }
        }
}