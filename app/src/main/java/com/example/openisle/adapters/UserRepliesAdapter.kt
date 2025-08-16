package com.example.openisle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.openisle.R
import com.example.openisle.data.CommentInfo

class UserRepliesAdapter : RecyclerView.Adapter<UserRepliesAdapter.ViewHolder>() {
    private val replies = mutableListOf<CommentInfo>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val content: TextView = view.findViewById(R.id.replyContent)
        val postTitle: TextView = view.findViewById(R.id.originalPostTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_reply, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = replies[position]
        holder.content.text = reply.content
        holder.postTitle.text = "回复于: ${reply.post.title}"
    }

    override fun getItemCount() = replies.size

    fun setData(newReplies: List<CommentInfo>) {
        replies.clear()
        replies.addAll(newReplies)
        notifyDataSetChanged()
    }
}