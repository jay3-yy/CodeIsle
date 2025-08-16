package com.example.openisle.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.openisle.R
import com.example.openisle.UserProfileActivity
import com.example.openisle.data.Comment
import com.example.openisle.utils.EmojiManager
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<Comment>()
    private var markwon: Markwon? = null

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorAvatar: ImageView = itemView.findViewById(R.id.commentAuthorAvatar)
        val authorUsername: TextView = itemView.findViewById(R.id.commentAuthorUsername)
        val content: TextView = itemView.findViewById(R.id.commentContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        if (markwon == null) {
            markwon = Markwon.builder(parent.context)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(CoilImagesPlugin.create(parent.context))
                .build()
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.authorUsername.text = comment.author.username
        holder.authorAvatar.load(comment.author.avatar) {
            crossfade(true)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
        }

        // --- 新增代码在这里 ---
        holder.authorAvatar.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UserProfileActivity::class.java).apply {
                putExtra(UserProfileActivity.EXTRA_USER_IDENTIFIER, comment.author.username)
            }
            context.startActivity(intent)
        }

        val emojiSize = (holder.content.textSize * 1.2F).toInt()
        val processedContent = EmojiManager.replaceEmojisWithHtml(comment.content, emojiSize)
        markwon?.setMarkdown(holder.content, processedContent)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    fun submitList(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}