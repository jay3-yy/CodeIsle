package com.example.openisle.presentation.features.feed.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.openisle.R
import com.example.openisle.databinding.ItemPostCardBinding
import com.example.openisle.databinding.ItemPostGridBinding
import com.example.openisle.domain.model.Post
import io.noties.markwon.Markwon

interface OnPostClickListener {
    fun onPostClick(post: Post)
}

class PostAdapter(private val listener: OnPostClickListener) : ListAdapter<Post, RecyclerView.ViewHolder>(PostDiffCallback()) {

    companion object {
        const val VIEW_TYPE_LIST_CARD = 1
        const val VIEW_TYPE_GRID = 2
    }

    var currentViewType = VIEW_TYPE_LIST_CARD
    private var markwon: Markwon? = null

    // 用于列表视图的 ViewHolder (对应 item_post_card.xml)
    inner class CardViewHolder(val binding: ItemPostCardBinding) : RecyclerView.ViewHolder(binding.root)

    // 用于网格视图的 ViewHolder (对应 item_post_grid.xml)
    inner class GridViewHolder(val binding: ItemPostGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return currentViewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (markwon == null) {
            markwon = Markwon.builder(parent.context).build()
        }

        return when (viewType) {
            VIEW_TYPE_GRID -> {
                val binding = ItemPostGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                GridViewHolder(binding)
            }
            else -> { // 默认为列表视图
                val binding = ItemPostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CardViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = getItem(position)
        when (holder) {
            is CardViewHolder -> bindCardView(holder, post)
            is GridViewHolder -> bindGridView(holder, post)
        }
    }

    /**
     * 绑定列表视图 (item_post_card.xml) 的数据
     */
    private fun bindCardView(holder: CardViewHolder, post: Post) {
        // ▼▼▼ 核心修改：同步这里的 ID 和 item_post_card.xml 一致 ▼▼▼
        holder.binding.apply {
            // 加载作者信息
            authorAvatar.load(post.author.avatar) {
                placeholder(R.drawable.placeholder_avatar) // 添加占位符
            }
            authorUsername.text = post.author.username
            createdAt.text = post.createdAt.substringBefore("T")

            // 加载标题和评论数
            markwon?.setMarkdown(postTitle, post.title)
            commentCount.text = post.commentCount.toString()

            // 列表视图不显示封面图，根据您的最新布局，这个ID不存在于列表中
            // 如果需要显示，请确保 item_post_card.xml 中有对应ID的ImageView

            root.setOnClickListener { listener.onPostClick(post) }
        }
    }

    /**
     * 绑定网格视图 (item_post_grid.xml) 的数据
     */
    private fun bindGridView(holder: GridViewHolder, post: Post) {
        // ▼▼▼ 核心修改：同步这里的 ID 和 item_post_grid.xml 一致 ▼▼▼
        holder.binding.apply {
            authorNameTextView.text = post.author.username
            authorAvatarImageView.load(post.author.avatar) {
                placeholder(R.drawable.placeholder_avatar) // 添加占位符
            }
            markwon?.setMarkdown(postTitleTextView, post.title)
            root.setOnClickListener { listener.onPostClick(post) }

            // 加载封面图
            val imageUrl = post.coverImageUrl
            if (imageUrl != null) {
                postCoverImageView.visibility = View.VISIBLE
                postCoverImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder_image)
                }
            } else {
                postCoverImageView.visibility = View.GONE
            }
        }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}