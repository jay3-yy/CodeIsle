package com.openisle.android.presentation.features.feed.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView // ✅ 显式使用 TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.openisle.android.R
import com.openisle.android.databinding.ItemPostBinding
import com.openisle.android.databinding.ItemPostCardBinding
import com.openisle.android.databinding.ItemPostGridBinding
import com.openisle.android.domain.model.Post
import io.noties.markwon.Markwon // ✅ 新增：导入 Markwon

class PostAdapter(
    private val listener: OnPostClickListener,
    private val markwon: Markwon // ✅ 修正：接收 Markwon 实例
) : ListAdapter<Post, RecyclerView.ViewHolder>(PostDiffCallback()) {

    // ✅ 修正：区分“模式”和“视图类型”
    private var currentDisplayMode = MODE_LIST

    companion object {
        // 用户切换的显示模式
        const val MODE_LIST = 0
        const val MODE_GRID = 1

        // RecyclerView 内部使用的视图类型
        private const val VIEW_TYPE_LIST_CARD = 10 // 列表-有图 (ItemPostCardBinding)
        private const val VIEW_TYPE_LIST_TEXT = 11 // 列表-无图 (ItemPostBinding)
        private const val VIEW_TYPE_GRID = 12      // 网格 (ItemPostGridBinding)
    }

    /**
     * ✅ 核心改动：根据“模式”和“内容”返回不同 ViewType
     */
    override fun getItemViewType(position: Int): Int {
        // 检查当前是列表模式还是网格模式
        return when (currentDisplayMode) {
            MODE_GRID -> VIEW_TYPE_GRID // 网格模式保持不变

            else -> { // MODE_LIST
                // 在列表模式下，检查帖子是否有图
                val post = getItem(position)
                val hasImage = !extractFirstImageUrl(post.content).isNullOrBlank()

                if (hasImage) {
                    VIEW_TYPE_LIST_CARD // 有图，使用 ItemPostCardBinding
                } else {
                    VIEW_TYPE_LIST_TEXT // 无图，使用 ItemPostBinding
                }
            }
        }
    }

    // ✅ 修正：重命名变量，更清晰
    fun setViewType(viewType: Int) {
        if (currentDisplayMode != viewType) {
            currentDisplayMode = viewType
            notifyItemRangeChanged(0, itemCount)
        }
    }

    /**
     * ✅ 核心改动：为新的 ViewType 创建 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_LIST_CARD -> PostListViewHolder(ItemPostCardBinding.inflate(inflater, parent, false))
            VIEW_TYPE_LIST_TEXT -> PostTextViewHolder(ItemPostBinding.inflate(inflater, parent, false)) // ✅ 新增
            VIEW_TYPE_GRID -> PostGridViewHolder(ItemPostGridBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid viewType $viewType")
        }
    }

    /**
     * ✅ 核心改动：分发绑定到新的 ViewHolder
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = getItem(position)
        when (holder) {
            is PostListViewHolder -> holder.bind(post)
            is PostTextViewHolder -> holder.bind(post) // ✅ 新增
            is PostGridViewHolder -> holder.bind(post)
        }
    }

    // ===== Utilities =====

    /** 把后端转义的 \!\[ 还原为 ![ */
    private fun normalizeMarkdown(md: String): String = md.replace("\\!\\[".toRegex(), "![")

    /** 提取 Markdown 中的第一张图片 URL：![alt](url) */
    private fun extractFirstImageUrl(md: String): String? {
        val fixed = normalizeMarkdown(md)
        val regex = Regex("""!\[[^\]]*]\(([^)]+)\)""")
        return regex.find(fixed)?.groupValues?.getOrNull(1)
    }

    /**
     * ✅ 修正：新的摘要生成函数
     * 它只做截断和清理，保留基础 Markdown
     */
    private fun createSnippet(md: String, maxLen: Int = 120): String {
        // 1. 替换代码块（在摘要中不好看）
        var t = md.replace(Regex("```[\\s\\S]*?```"), "[代码块]")

        // ⬇️ ⬇️ ⬇️ 修复：在这里移除图片链接 ⬇️ ⬇️ ⬇️
        // 移除 Markdown 图片链接，替换为 [图片]
        t = t.replace(Regex("""!\[[^\]]*\]\(([^)]+)\)"""), "[图片]")
        // ⬆️ ⬆️ ⬆️ 修复完成 ⬆️ ⬆️ ⬆️

        // 2. 移除结构性元素（标题、引用、列表标记）
        t = t.replace(Regex("(?m)^\\s{0,3}#{1,6}\\s*"), "") // 移除 # 标题
        t = t.replace(Regex("(?m)^\\s{0,3}>\\s*"), "") // 移除 > 引用
        t = t.replace(Regex("(?m)^\\s{0,3}([-*+]\\s+|\\d+\\.\\s+)"), "") // 移除列表标记

        // 3. 将换行符变为空格，并压缩空白
        t = t.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()

        // 4. 截断
        if (t.length <= maxLen) return t

        // 尝试在单词边界截断
        var cutOff = t.lastIndexOf(' ', maxLen)
        if (cutOff == -1 || cutOff < maxLen / 2) {
            cutOff = maxLen // 失败则硬截断
        }
        return t.substring(0, cutOff) + "…"
    }

    // ===== ViewHolders =====

    /**
     * ViewHolder 1: 列表 - 图文帖 (使用 ItemPostCardBinding)
     */
    inner class PostListViewHolder(private val binding: ItemPostCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) = with(binding) {
            postTitle.transitionName = "title_${post.id}"
            postCoverImageView.transitionName = "image_${post.id}"

            postTitle.text = post.title
            authorUsername.text = post.author.username
            createdAt.text = post.createdAt.substringBefore("T")
            commentCount.text = post.commentCount.toString()

            // 头像
            authorAvatar.load(post.author.avatar) {
                allowHardware(false)
                placeholder(R.drawable.placeholder_avatar)
                error(R.drawable.placeholder_avatar)
            }

            // ✅ 优化：此 ViewHolder 现在只处理有图的帖子，无需 if/else
            val firstImage = extractFirstImageUrl(post.content)
            postCoverImageView.isVisible = true // 总是可见
            postCoverImageView.load(firstImage) {
                allowHardware(false)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.placeholder_image)
            }

            // ✅ 核心修正：使用 Markwon 渲染摘要
            val snippet = createSnippet(post.content)
            markwon.setMarkdown(postExcerpt, snippet)

            // 点击
            root.setOnClickListener {
                val sharedViews: MutableMap<String, TextView> = hashMapOf(
                    postTitle.transitionName to postTitle
                )
                listener.onPostClick(post, sharedViews)
            }
            authorAvatar.setOnClickListener { listener.onAvatarClick(post.author.avatar) }
            authorUsername.setOnClickListener { listener.onUsernameClick(post.author.id) }
        }
    }

    /**
     * ✅ ViewHolder 2: 列表 - 纯文帖 (使用 item_post.xml 生成的 ItemPostBinding)
     */
    inner class PostTextViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // 绑定逻辑 (来自 item_post.xml)
        fun bind(post: Post) = with(binding) {
            postTitle.transitionName = "title_${post.id}"

            postTitle.text = post.title
            authorUsername.text = post.author.username
            createdAt.text = post.createdAt.substringBefore("T")
            commentCount.text = post.commentCount.toString()

            // 头像
            authorAvatar.load(post.author.avatar) {
                allowHardware(false)
                placeholder(R.drawable.placeholder_avatar)
                error(R.drawable.placeholder_avatar)
            }

            // (这个布局没有 postCoverImageView)

            // ✅ 核心修正：使用 Markwon 渲染摘要
            val snippet = createSnippet(post.content)
            markwon.setMarkdown(postExcerpt, snippet)

            // 点击
            root.setOnClickListener {
                val sharedViews: MutableMap<String, TextView> = hashMapOf(
                    postTitle.transitionName to postTitle
                )
                listener.onPostClick(post, sharedViews)
            }
            authorAvatar.setOnClickListener { listener.onAvatarClick(post.author.avatar) }
            authorUsername.setOnClickListener { listener.onUsernameClick(post.author.id) }
        }
    }


    /**
     * ViewHolder 3: 网格帖 (使用 item_post_grid.xml) - 保持不变
     */
    inner class PostGridViewHolder(private val binding: ItemPostGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) = with(binding) {
            postTitleTextView.transitionName = "title_${post.id}"
            postCoverImageView.transitionName = "image_${post.id}"

            postTitleTextView.text = post.title
            authorNameTextView.text = post.author.username

            authorAvatarImageView.load(post.author.avatar) {
                allowHardware(false)
                placeholder(R.drawable.placeholder_avatar)
                error(R.drawable.placeholder_avatar)
            }

            // 网格封面 (无摘要，无需修改)
            val firstImage = extractFirstImageUrl(post.content)
            if (firstImage.isNullOrBlank()) {
                postCoverImageView.isVisible = false
                coverPlaceholder.isVisible = true
            } else {
                coverPlaceholder.isVisible = false
                postCoverImageView.isVisible = true
                postCoverImageView.load(firstImage) {
                    allowHardware(false)
                    placeholder(R.drawable.placeholder_image)
                    error(R.drawable.placeholder_image)
                }
            }

            // 点击
            root.setOnClickListener {
                val sharedViews: MutableMap<String, TextView> = hashMapOf(
                    postTitleTextView.transitionName to postTitleTextView
                )
                listener.onPostClick(post, sharedViews)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}