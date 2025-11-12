package com.openisle.android.presentation.features.common

/**
 * 这个工具类的作用是，将后端 ReactionType enum 的名称（例如 "LIKE", "FIRE"）
 * 映射为客户端需要显示的 Emoji 表情。
 */
object ReactionUiMapper {
    private val MAP: Map<String, String> = mapOf(
        // 来自 ReactionType.java 的映射
        "LIKE" to "👍",
        "DISLIKE" to "👎",
        "RECOMMEND" to "👍",
        "ANGRY" to "😠",
        "FLUSHED" to "😳",
        "STAR_STRUCK" to "🤩",
        "ROFL" to "🤣",
        "HOLDING_BACK_TEARS" to "🥹",
        "MIND_BLOWN" to "🤯",
        "POOP" to "💩",
        "CLOWN" to "🤡",
        "SKULL" to "💀",
        "FIRE" to "🔥",
        "EYES" to "👀",
        "FROWN" to "☹️",
        "HOT" to "🥵",
        "EAGLE" to "🦅",
        "SPIDER" to "🕷️",
        "BAT" to "🦇",
        "CHINA" to "🇨🇳",
        "USA" to "🇺🇸",
        "JAPAN" to "🇯🇵",
        "KOREA" to "🇰🇷",

        // 其他常见别名
        "THUMBS_UP" to "👍", "+1" to "👍", "UPVOTE" to "👍",
        "THUMBS_DOWN" to "👎", "-1" to "👎", "DOWNVOTE" to "👎",
        "HEART" to "❤️"
    )

    /**
     * 将后端的 ReactionType 枚举名称归一化并映射到 Emoji。
     * ✅ 修正：未知类型返回 null。
     * @return 成功映射则返回 Emoji 字符串，否则返回 null。
     */
    fun toEmoji(rawType: String): String? {
        // 尝试直接用大写形式匹配
        MAP[rawType.uppercase()]?.let { return it }
        // 尝试将 '-', ' ' 等字符替换为 '_' 后匹配
        val normalized = rawType.trim().replace('-', '_').replace(' ', '_').uppercase()
        MAP[normalized]?.let { return it }
        // 尝试更宽松的匹配，移除所有非字母数字下划线的字符
        val looser = normalized.replace(Regex("[^A-Z0-9_]+"), "")
        // ✅ 修正：如果找不到，直接返回 null
        return MAP[looser]
    }
}