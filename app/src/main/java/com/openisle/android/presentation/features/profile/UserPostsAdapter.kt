package com.openisle.android.presentation.features.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.openisle.android.R
import com.openisle.android.data.PostMeta

class UserPostsAdapter(private val listener: OnPostClickListener) :
    ListAdapter<PostMeta, UserPostsAdapter.ViewHolder>(PostMetaDiffCallback()) {

    // 将接口定义移到类内部，标准做法
    interface OnPostClickListener {
        fun onPostClick(post: PostMeta)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.postTitle)
        val date: TextView = view.findViewById(R.id.postDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)
        holder.title.text = post.title
        holder.date.text = post.createdAt.substringBefore("T")

        holder.itemView.setOnClickListener {
            listener.onPostClick(post)
        }
    }

    class PostMetaDiffCallback : DiffUtil.ItemCallback<PostMeta>() {
        override fun areItemsTheSame(oldItem: PostMeta, newItem: PostMeta): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostMeta, newItem: PostMeta): Boolean {
            return oldItem == newItem
        }
    }
}