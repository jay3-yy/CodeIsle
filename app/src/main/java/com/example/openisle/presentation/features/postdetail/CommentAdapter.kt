package com.example.openisle.presentation.features.postdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.openisle.R
import com.example.openisle.domain.model.Comment
import com.example.openisle.utils.EmojiManager
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

// 定义了两种列表项：主评论和回复
sealed interface CommentListItem {
    val id: Long // 为DiffUtil提供一个唯一的、稳定的ID
    data class MainComment(val comment: Comment) : CommentListItem {
        override val id: Long = comment.id
    }
    data class Reply(val comment: Comment) : CommentListItem {
        override val id: Long = comment.id
    }
}

// 1. 修正接口：authorId 的类型改为 Int
interface OnAvatarClickListener {
    fun onAvatarClick(authorId: Int)
}

// 2. 继承自 ListAdapter，它能更高效地处理列表更新
class CommentAdapter(private val listener: OnAvatarClickListener) :
    ListAdapter<CommentListItem, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private var markwon: Markwon? = null

    // ViewHolder 保持不变
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorAvatar: ImageView = itemView.findViewById(R.id.commentAuthorAvatar)
        val authorUsername: TextView = itemView.findViewById(R.id.commentAuthorUsername)
        val content: TextView = itemView.findViewById(R.id.commentContent)
    }

    // 3. 不再需要手动管理列表，移除 displayList 和 getItemCount()
    // ListAdapter 会自动处理

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) { // 使用 getItem() 获取数据
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
        val item = getItem(position)
        val comment = when (item) {
            is CommentListItem.MainComment -> item.comment
            is CommentListItem.Reply -> item.comment
        }

        // 使用 ?. 安全调用
        holder.authorUsername.text = comment.author?.username ?: "Unknown"
        holder.authorAvatar.load(comment.author?.avatar) {
            crossfade(true)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
        }

        // 修正回调逻辑，确保 author 不为空
        holder.authorAvatar.setOnClickListener {
            comment.author?.let { author ->
                listener.onAvatarClick(author.id)
            }
        }

        val emojiSize = (holder.content.textSize * 1.2F).toInt()
        val processedContent = EmojiManager.replaceEmojisWithHtml(comment.content, emojiSize)
        markwon?.setMarkdown(holder.content, processedContent)
    }
    // 5. 新增 DiffUtil.ItemCallback，这是 ListAdapter 的核心
    // 它告诉 Adapter 如何判断两个Item是否相同，以及内容是否发生了变化
    class CommentDiffCallback : DiffUtil.ItemCallback<CommentListItem>() {
        override fun areItemsTheSame(oldItem: CommentListItem, newItem: CommentListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommentListItem, newItem: CommentListItem): Boolean {
            // MainComment 和 Reply 都是 data class，可以直接比较内容
            return oldItem == newItem
        }
    }
}