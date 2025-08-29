package com.example.openisle.utils

import java.util.regex.Pattern

object EmojiManager {
    private const val TIEBA_EMOJI_URL_TEMPLATE =
        "https://cdn.jsdelivr.net/gh/microlong666/tieba_mobile_emotions@master/image_emoticon%s.png"

    // 使用正则表达式来查找所有 :tiebaXX: 格式
    private val tiebaRegex = Pattern.compile(":tieba(\\d+):")

    /**
     * 这个方法是我们的核心功能：
     * 它会查找所有 :tiebaXX: 代码，并将其替换为 HTML 的 <img> 标签
     */
    fun replaceEmojisWithHtml(text: String, emojiSize: Int): String {
        val matcher = tiebaRegex.matcher(text)
        return matcher.replaceAll { matchResult ->
            val emojiId = matchResult.group(1) // 获取数字ID，比如 "62"
            val url = String.format(TIEBA_EMOJI_URL_TEMPLATE, emojiId)
            // 返回一个标准的 HTML 图片标签，并设置大小
            "<img src=\"$url\" width=\"$emojiSize\" height=\"$emojiSize\" />"
        }
    }
}