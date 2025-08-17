package com.example.openisle.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.openisle.ImageViewerActivity
import com.example.openisle.R
import com.example.openisle.data.Comment
import com.example.openisle.utils.EmojiManager
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

sealed interface CommentListItem {
    data class MainComment(val comment: Comment) : CommentListItem
    data class Reply(val comment: Comment) : CommentListItem
}

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val displayList = mutableListOf<CommentListItem>()
    private var markwon: Markwon? = null

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorAvatar: ImageView = itemView.findViewById(R.id.commentAuthorAvatar)
        val authorUsername: TextView = itemView.findViewById(R.id.commentAuthorUsername)
        val content: TextView = itemView.findViewById(R.id.commentContent)
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayList[position]) {
            is CommentListItem.MainComment -> R.layout.item_comment
            is CommentListItem.Reply -> R.layout.item_reply
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        if (markwon == null) {
            markwon = Markwon.builder(parent.context)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(CoilImagesPlugin.create(parent.context))
                .build()
        }
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = displayList[position]
        val comment = when (item) {
            is CommentListItem.MainComment -> item.comment
            is CommentListItem.Reply -> item.comment
        }

        holder.authorUsername.text = comment.author.username
        holder.authorAvatar.load(comment.author.avatar) {
            crossfade(true)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
        }

        // --- 关键改动在这里 ---
        holder.authorAvatar.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, comment.author.avatar)
            }
            context.startActivity(intent)
        }

        val emojiSize = (holder.content.textSize * 1.2F).toInt()
        val processedContent = EmojiManager.replaceEmojisWithHtml(comment.content, emojiSize)
        markwon?.setMarkdown(holder.content, processedContent)
    }

    override fun getItemCount(): Int {
        return displayList.size
    }

    fun submitList(newComments: List<Comment>) {
        val flattenedList = mutableListOf<CommentListItem>()
        newComments.forEach { mainComment ->
            flattenedList.add(CommentListItem.MainComment(mainComment))
            mainComment.replies.forEach { reply ->
                flattenedList.add(CommentListItem.Reply(reply))
            }
        }

        displayList.clear()
        displayList.addAll(flattenedList)
        notifyDataSetChanged()
    }
}