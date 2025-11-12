package com.openisle.android.presentation.util;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int spanCount;
    private final int spacing;
    private final boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // ✅ 关键修正：在执行任何操作前，先检查 LayoutManager 的类型
        if (!(parent.getLayoutManager() instanceof GridLayoutManager)) {
            // 如果不是网格布局，就直接返回，不做任何间距处理，从而避免崩溃
            super.getItemOffsets(outRect, view, parent, state);
            return;
        }

        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        // 只有在确认是 GridLayoutManager 后，才安全地进行后续操作
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        int spanIndex = lp.getSpanIndex();

        if (includeEdge) {
            outRect.left = spacing - spanIndex * spacing / spanCount;
            outRect.right = (spanIndex + 1) * spacing / spanCount;

            if (position < spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            outRect.left = spanIndex * spacing / spanCount;
            outRect.right = spacing - (spanIndex + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}