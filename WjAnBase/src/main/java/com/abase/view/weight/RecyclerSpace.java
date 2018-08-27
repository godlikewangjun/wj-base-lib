package com.abase.view.weight;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * @author wangjun
 * @version 1.0
 * @date 2016/8/25
 */
public class RecyclerSpace extends RecyclerView.ItemDecoration {
    private int space;
    private int color = -1;
    private Drawable mDivider;
    private Paint mPaint;
    private int type=0;

    public int getColor() {
        return color;
    }

    public void setColor(@ColorRes int color) {
        this.color = color;
    }

    public RecyclerSpace(int space) {
        this.space = space;
    }

    public RecyclerSpace(int space, int color) {
        this.space = space;
        this.color = color;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(space * 2);
    }

    public RecyclerSpace(int space, int color, int type) {
        this.space = space;
        this.color = color;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(space * 2);
        this.type = type;
    }

    public RecyclerSpace(int space, Drawable mDivider) {
        this.space = space;
        this.mDivider = mDivider;
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition,
                               RecyclerView parent) {
        if (parent.getLayoutManager() != null) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager && !(parent.getLayoutManager() instanceof GridLayoutManager)) {
                if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    outRect.set(space, 0, space, 0);
                } else {
                    outRect.set(0, space, 0, space);
                }
            } else {
                if (type == 0) {
                    int spanCount = getSpanCount(parent);
                    int childCount = parent.getAdapter().getItemCount();
                    if (isLastRaw(parent, itemPosition, spanCount, childCount))// 如果是最后一行，则不需要绘制底部
                    {
                        outRect.set(0, 0, space, 0);
                    } else if (isLastColum(parent, itemPosition, spanCount, childCount))// 如果是最后一列，则不需要绘制右边
                    {
                        outRect.set(0, 0, 0, space);
                    } else {
                        outRect.set(0, 0, space,
                                space);
                    }
                }else{
                    outRect.set(space, space, space,
                            space);
                }


            }
        }
    }

    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {

            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager)
                    .getSpanCount();
        }
        return spanCount;
    }

    private boolean isLastColum(RecyclerView parent, int pos, int spanCount,
                                int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
            {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                {
                    return true;
                }
            } else {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                    return true;
            }
        }
        return false;
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                              int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            childCount = childCount -spanCount;
            if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                return true;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount)
                    return true;
            } else
            // StaggeredGridLayoutManager 且横向滚动
            {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (parent.getLayoutManager() != null) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager && !(parent.getLayoutManager() instanceof GridLayoutManager)) {
                if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    drawVertical(c, parent);
                } else {
                    drawHorizontal(c, parent);
                }
            } else {
                if (type == 0) {
                    drawGrideview(c, parent);
                } else {
                    drawGrideview1(c, parent);
                }
            }
        }
    }

    //绘制纵向 item 分割线

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + space;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    //绘制横向 item 分割线
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        int left = parent.getPaddingLeft();
        int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + layoutParams.bottomMargin;
            int bottom = top + space;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }

        }
    }

    //绘制grideview item 分割线 不是填充满的
    private void drawGrideview(Canvas canvas, RecyclerView parent) {
        GridLayoutManager linearLayoutManager = (GridLayoutManager) parent.getLayoutManager();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            //画横线
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            int left = child.getLeft() - params.leftMargin;
            int right = child.getRight() + params.rightMargin
                    + space;
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + space;
            if(i<childCount-getSpanCount(parent)){
                if(mDivider!=null){
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }else {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }


            //画竖线
            top = child.getTop() - params.topMargin;
            bottom = child.getBottom() + params.bottomMargin;
            left = child.getRight() + params.rightMargin;
            right = left + space;
            if((i+1)%getSpanCount(parent)!=0){
                if(mDivider!=null){
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }else {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
        }
    }

    /**边框*/
    private void drawGrideview1(Canvas canvas, RecyclerView parent) {
        GridLayoutManager linearLayoutManager = (GridLayoutManager) parent.getLayoutManager();
        int childSize = parent.getChildCount();
        int top, bottom, left, right, spancount;
        spancount = linearLayoutManager.getSpanCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            //画横线
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            top = child.getBottom() + layoutParams.bottomMargin;
            bottom = top + space;
            left = layoutParams.leftMargin + child.getPaddingLeft() + space;
            right = child.getMeasuredWidth() * (i + 1) + left + space * i;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
            //画竖线
            top = (layoutParams.topMargin + space) * (i / linearLayoutManager.getSpanCount() + 1);
            bottom = (child.getMeasuredHeight() + space) * (i / linearLayoutManager.getSpanCount() + 1) + space;
            left = child.getRight() + layoutParams.rightMargin;
            right = left + space;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }

            //画上缺失的线框
            if (i < spancount) {
                top = child.getTop() + layoutParams.topMargin;
                bottom = top + space;
                left = (layoutParams.leftMargin + space) * (i + 1);
                right = child.getMeasuredWidth() * (i + 1) + left + space * i;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
            if (i % spancount == 0) {
                top = (layoutParams.topMargin + space) * (i / linearLayoutManager.getSpanCount() + 1);
                bottom = (child.getMeasuredHeight() + space) * (i / linearLayoutManager.getSpanCount() + 1) + space;
                left = child.getLeft() + layoutParams.leftMargin;
                right = left + space;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
        }
    }
}