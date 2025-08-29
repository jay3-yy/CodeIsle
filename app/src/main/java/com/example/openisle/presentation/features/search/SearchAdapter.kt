package com.example.openisle.presentation.features.search

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
import com.example.openisle.data.SearchResult
import com.example.openisle.utils.EmojiManager
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

interface OnSearchResultClickListener {
    fun onSearchResultClick(result: SearchResult)
}

class SearchAdapter(private val listener: OnSearchResultClickListener) :
    ListAdapter<SearchResult, SearchAdapter.ViewHolder>(SearchResultDiffCallback()) {

    private var markwon: Markwon? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.resultAvatar)
        val type: TextView = view.findViewById(R.id.resultType)
        val text: TextView = view.findViewById(R.id.resultText)
        val subText: TextView = view.findViewById(R.id.resultSubText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (markwon == null) {
            markwon = Markwon.builder(parent.context)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(CoilImagesPlugin.create(parent.context))
                .build()
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position)
        val emojiSize = (holder.text.textSize * 1.2F).toInt()

        // 重置视图状态
        holder.text.text = ""
        holder.subText.text = ""

        when (result.type.uppercase()) {
            "USER" -> {
                holder.type.text = "[用户]"
                setMarkdownText(holder.text, result.text, emojiSize)
                setMarkdownText(holder.subText, result.subText, emojiSize)

                if (!result.avatarUrl.isNullOrEmpty()) {
                    holder.avatar.visibility = View.VISIBLE
                    holder.avatar.load(result.avatarUrl) {
                        crossfade(true)
                        placeholder(R.drawable.placeholder_avatar)
                        error(R.drawable.placeholder_avatar)
                    }
                } else {
                    holder.avatar.visibility = View.GONE
                }
            }
            "POST" -> {
                holder.type.text = "[帖子]"
                setMarkdownText(holder.text, result.text, emojiSize)
                val subTextContent = "${result.subText ?: ""}\n${result.extra ?: ""}".trim()
                setMarkdownText(holder.subText, subTextContent, emojiSize)
                holder.avatar.visibility = View.GONE
            }
            "COMMENT" -> {
                holder.type.text = "[评论]"
                setMarkdownText(holder.text, result.text, emojiSize)
                val subTextContent = "回复于: ${result.extra ?: ""}"
                setMarkdownText(holder.subText, subTextContent, emojiSize)
                holder.avatar.visibility = View.GONE
            }
            else -> {
                holder.type.text = "[未知]"
                setMarkdownText(holder.text, result.text, emojiSize)
                setMarkdownText(holder.subText, result.subText, emojiSize)
                holder.avatar.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            listener.onSearchResultClick(result)
        }
    }

    private fun setMarkdownText(textView: TextView, markdown: String?, emojiSize: Int) {
        val content = markdown ?: ""
        val processedContent = EmojiManager.replaceEmojisWithHtml(content, emojiSize)
        markwon?.setMarkdown(textView, processedContent)
        textView.visibility = if (textView.text.isNotEmpty()) View.VISIBLE else View.GONE
    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem.id == newItem.id && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    }
}