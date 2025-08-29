package com.example.openisle.presentation.util // 包名已修改

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * 为 StaggeredGridLayoutManager 或 GridLayoutManager 添加间距的 ItemDecoration。
 *
 * @param spanCount 列数
 * @param spacing 间距大小（像素）
 * @param includeEdge 是否包括列表的边缘
 */
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        if (position == RecyclerView.NO_POSITION) {
            return
        }

        val lp = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        val spanIndex = lp.spanIndex

        if (includeEdge) {
            // 如果包含边缘
            if (spanIndex == 0) {
                // 左边的列
                outRect.left = spacing
                outRect.right = spacing / 2
            } else {
                // 右边的列
                outRect.left = spacing / 2
                outRect.right = spacing
            }

            if (position < spanCount) {
                // 顶部的行
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            // 如果不包含边缘
            if (spanIndex == 0) {
                // 左边的列
                outRect.left = 0
                outRect.right = spacing / 2
            } else {
                // 右边的列
                outRect.left = spacing / 2
                outRect.right = 0
            }

            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}