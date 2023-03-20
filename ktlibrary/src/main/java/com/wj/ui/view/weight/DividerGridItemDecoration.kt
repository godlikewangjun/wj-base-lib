package com.wj.ui.view.weight

import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import android.graphics.drawable.Drawable
import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.graphics.Rect
import com.wj.ktutils.R

/**
 * @author Administrator
 * @version 1.0
 * @date 2018/8/27
 */
class DividerGridItemDecoration : ItemDecoration {
    private var mDivider: Drawable
    private val wh = 1

    constructor(context: Context) {
        mDivider = context.resources.getDrawable(R.color.default_background_color)
    }

    constructor(mDivider: Drawable) {
        this.mDivider = mDivider
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    private fun getSpanCount(parent: RecyclerView): Int {
        // 列数
        var spanCount = -1
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            spanCount = layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            spanCount = layoutManager
                .spanCount
        }
        return spanCount
    }

    fun drawHorizontal(c: Canvas?, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val left = child.left - params.leftMargin
            val right = (child.right + params.rightMargin
                    + wh)
            val top = child.bottom + params.bottomMargin
            val bottom = top + wh
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c!!)
        }
    }

    fun drawVertical(c: Canvas?, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin
            val left = child.right + params.rightMargin
            val right = left + wh
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c!!)
        }
    }

    private fun isLastColum(
        parent: RecyclerView, pos: Int, spanCount: Int,
        childCount: Int
    ): Boolean {
        var childCount = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            if ((pos + 1) % spanCount == 0) // 如果是最后一列，则不需要绘制右边
            {
                return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager
                .orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0) // 如果是最后一列，则不需要绘制右边
                {
                    return true
                }
            } else {
                childCount -= childCount % spanCount
                if (pos >= childCount) // 如果是最后一列，则不需要绘制右边
                    return true
            }
        }
        return false
    }

    private fun isLastRaw(
        parent: RecyclerView, pos: Int, spanCount: Int,
        childCount: Int
    ): Boolean {
        var childCount = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            childCount = childCount - childCount % spanCount
            if (pos >= childCount) // 如果是最后一行，则不需要绘制底部
                return true
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager
                .orientation
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount) return true
            } else  // StaggeredGridLayoutManager 且横向滚动
            {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true
                }
            }
        }
        return false
    }

    override fun getItemOffsets(
        outRect: Rect, itemPosition: Int,
        parent: RecyclerView
    ) {
        val spanCount = getSpanCount(parent)
        val childCount = parent.adapter!!.itemCount
        if (isLastRaw(parent, itemPosition, spanCount, childCount)) // 如果是最后一行，则不需要绘制底部
        {
            outRect[0, 0, wh] = 0
        } else if (isLastColum(parent, itemPosition, spanCount, childCount)) // 如果是最后一列，则不需要绘制右边
        {
            outRect[0, 0, 0] = wh
        } else {
            outRect[0, 0, wh] = wh
        }
    }
}