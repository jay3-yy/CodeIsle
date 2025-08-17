package com.example.openisle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.openisle.R
import com.example.openisle.data.SearchResult

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    private val results = mutableListOf<SearchResult>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.resultType)
        val text: TextView = view.findViewById(R.id.resultText)
        val subText: TextView = view.findViewById(R.id.resultSubText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]

        // --- 还原后的逻辑 ---
        // 不再处理头像，统一显示 text 和 subText
        holder.type.text = "[${result.type}]"
        holder.text.text = result.text

        if (!result.subText.isNullOrEmpty()) {
            holder.subText.visibility = View.VISIBLE
            holder.subText.text = result.subText
        } else {
            holder.subText.visibility = View.GONE
        }

        // 点击事件逻辑已被移除
    }

    override fun getItemCount() = results.size

    fun setData(newResults: List<SearchResult>) {
        results.clear()
        results.addAll(newResults)
        notifyDataSetChanged()
    }
}