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
import com.example.openisle.UserProfileActivity
import com.example.openisle.data.Post

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<Post>()

    interface OnItemClickListener {
        fun onItemClick(post: Post)
        fun onAvatarClick(author: com.example.openisle.data.Author)
    }
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorAvatar: ImageView = itemView.findViewById(R.id.authorAvatar)
        val authorUsername: TextView = itemView.findViewById(R.id.authorUsername)
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.authorUsername.text = post.author.username
        holder.postTitle.text = post.title
        holder.authorAvatar.load(post.author.avatar) {
            crossfade(true)
            placeholder(R.drawable.placeholder_avatar)
            error(R.drawable.placeholder_avatar)
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(post)
        }

        // --- 关键改动在这里 ---
        holder.authorAvatar.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, post.author.avatar)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun setData(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    fun addData(newPosts: List<Post>) {
        val startPosition = posts.size
        posts.addAll(newPosts)
        notifyItemRangeInserted(startPosition, newPosts.size)
    }
}