package com.example.openisle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.openisle.R
import com.example.openisle.data.CommentInfo
import com.example.openisle.utils.EmojiManager
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

class UserRepliesAdapter : RecyclerView.Adapter<UserRepliesAdapter.ViewHolder>() {
    private val replies = mutableListOf<CommentInfo>()
    // --- 新增：为这个 Adapter 创建一个 Markwon 实例 ---
    private var markwon: Markwon? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val content: TextView = view.findViewById(R.id.replyContent)
        val postTitle: TextView = view.findViewById(R.id.originalPostTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // --- 新增：在这里初始化 Markwon ---
        if (markwon == null) {
            markwon = Markwon.builder(parent.context)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(CoilImagesPlugin.create(parent.context))
                .build()
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_reply, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = replies[position]
        holder.postTitle.text = "回复于: ${reply.post.title}"

        // --- 关键改动：使用 Markwon 来渲染回复内容 ---
        val emojiSize = (holder.content.textSize * 1.2F).toInt()
        val processedContent = EmojiManager.replaceEmojisWithHtml(reply.content, emojiSize)
        markwon?.setMarkdown(holder.content, processedContent)
    }

    override fun getItemCount() = replies.size

    fun setData(newReplies: List<CommentInfo>) {
        replies.clear()
        replies.addAll(newReplies)
        notifyDataSetChanged()
    }
}