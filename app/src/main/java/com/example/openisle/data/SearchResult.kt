package com.example.openisle.data

import com.google.gson.annotations.SerializedName

data class SearchResult(
    @SerializedName("type")
    val type: String, // "POST" or "COMMENT" or "USER"

    @SerializedName("id")
    val id: Long,

    @SerializedName("text")
    val text: String, // 主要文本，如帖子标题

    @SerializedName("subText")
    val subText: String?, // 次要文本，如评论内容摘要

    @SerializedName("extra")
    val extra: String?, // 额外信息，如用户名

    @SerializedName("postId")
    val postId: Long? // 如果是评论，这里会有原帖的ID

)