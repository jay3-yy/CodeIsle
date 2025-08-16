package com.example.openisle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.openisle.R
import com.example.openisle.data.PostMeta

class UserPostsAdapter : RecyclerView.Adapter<UserPostsAdapter.ViewHolder>() {
    private val posts = mutableListOf<PostMeta>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.postTitle)
        val date: TextView = view.findViewById(R.id.postDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.title.text = post.title
        holder.date.text = post.createdAt.substringBefore("T") // 只显示日期，去掉时间
    }

    override fun getItemCount() = posts.size

    fun setData(newPosts: List<PostMeta>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
}