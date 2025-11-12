package com.openisle.android.presentation.features.postdetail

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import com.openisle.android.R
import com.openisle.android.domain.model.Comment
import com.openisle.android.utils.EmojiManager
// ▼▼▼ 修正点：添加 imports ▼▼▼
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
// ▲▲▲ 修正点 ▲▲▲
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

sealed interface CommentListItem {
    val id: Long
    data class MainComment(val comment: Comment) : CommentListItem { override val id: Long = comment.id }
    data class Reply(val comment: Comment) : CommentListItem { override val id: Long = comment.id }
}

interface OnCommentClickListener {
    fun onAvatarClick(imageUrl: String)
    fun onUsernameClick(userId: Int)
}

class CommentAdapter(private val listener: OnCommentClickListener) :
    ListAdapter<CommentListItem, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private lateinit var markwon: Markwon

    // ▼▼▼ 修正点 1：添加变量和方法以存储楼主 ID ▼▼▼
    private var postAuthorId: Int = -1

    fun setPostAuthorId(id: Int) {
        this.postAuthorId = id
    }
    // ▲▲▲ 修正点 1 ▲▲▲

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorAvatar: ImageView = itemView.findViewById(R.id.commentAuthorAvatar)
        val authorUsername: TextView = itemView.findViewById(R.id.commentAuthorUsername)
        val content: TextView = itemView.findViewById(R.id.commentContent)
    }

    // ▼▼▼ 核心修改：在此处初始化 Markwon，并为其配置与正文一致的间距 ▼▼▼
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val imageLoader = recyclerView.context.imageLoader.newBuilder()
            .allowHardware(false) // 关键：确保评论内容里的图片也不会导致闪退
            .build()

        val density = recyclerView.context.resources.displayMetrics.density

        markwon = Markwon.builder(recyclerView.context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(CoilImagesPlugin.create(recyclerView.context, imageLoader))
            // ▼▼▼ 添加新插件以统一样式 ▼▼▼
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        // 与 MarkdownHelper.kt 保持一致
                        .blockMargin((8 * density).toInt())
                        .codeBlockMargin((8 * density).toInt())
                        .blockQuoteWidth((4 * density).toInt())
                }
            })
            // ▲▲▲ 修改结束 ▲▲▲
            .build()
    }
    // ▲▲▲ 修改结束 ▲▲▲

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentListItem.MainComment -> R.layout.item_comment
            is CommentListItem.Reply -> R.layout.item_reply
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = when (val item = getItem(position)) {
            is CommentListItem.MainComment -> item.comment
            is CommentListItem.Reply -> item.comment
        }

        // ▼▼▼ 修正点 2：实现楼主高亮（加粗并添加后缀）▼▼▼
        val isPostAuthor = comment.author?.id == postAuthorId
        if (isPostAuthor) {
            holder.authorUsername.text = "${comment.author?.username ?: "Unknown"} [楼主]"
            holder.authorUsername.setTypeface(null, Typeface.BOLD)
        } else {
            holder.authorUsername.text = comment.author?.username ?: "Unknown"
            holder.authorUsername.setTypeface(null, Typeface.NORMAL)
        }
        // ▲▲▲ 修正点 2 ▲▲▲

        val avatarUrl = comment.author?.avatar

        holder.authorAvatar.load(avatarUrl) {
            allowHardware(false)
            crossfade(true)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
        }

        holder.authorAvatar.setOnClickListener {
            avatarUrl?.let { url ->
                listener.onAvatarClick(url)
            }
        }

        holder.authorUsername.setOnClickListener {
            comment.author?.let { author ->
                listener.onUsernameClick(author.id)
            }
        }

        val emojiSize = (holder.content.textSize * 1.2F).toInt()
        val processedContent = EmojiManager.replaceEmojisWithHtml(comment.content, emojiSize)

        //  修正点 3：为评论内容设置与正文一致的行距
        holder.content.setLineSpacing(8f, 1.5f)
        markwon.setMarkdown(holder.content, processedContent)
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<CommentListItem>() {
        override fun areItemsTheSame(oldItem: CommentListItem, newItem: CommentListItem): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: CommentListItem, newItem: CommentListItem): Boolean {
            return oldItem == newItem
        }
    }
}