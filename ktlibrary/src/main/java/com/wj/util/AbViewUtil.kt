/*
 * Copyright (C) 2012 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wj.util

import android.content.Context
import java.lang.Exception
import android.view.View
import android.util.DisplayMetrics
import android.widget.TextView
import android.text.TextPaint
import android.widget.AbsListView
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ListView
import android.widget.GridView
import android.view.View.MeasureSpec
import android.util.TypedValue
import android.graphics.Paint

/**
 * © 2012 amsoft.cn
 * 名称：AbViewUtil.java
 * 描述：View工具类.
 *
 * @author 还如一梦中
 * @version v1.0
 * @date：2013-01-17 下午11:52:13
 */
object AbViewUtil {
    var defaultScale = 2.0f //默认屏幕的密度

    /**
     * 无效值
     */
    const val INVALID = Int.MIN_VALUE

    /**
     * 描述：重置AbsListView的高度. item 的最外层布局要用
     * RelativeLayout,如果计算的不准，就为RelativeLayout指定一个高度
     *
     * @param absListView
     * the abs list view
     * @param lineNumber
     * 每行几个 ListView一行一个item
     * @param verticalSpace
     * the vertical space
     */
    fun setAbsListViewHeight(
        absListView: AbsListView,
        lineNumber: Int, verticalSpace: Int
    ) {
        val totalHeight = getAbsListViewHeight(absListView, lineNumber,
            verticalSpace)
        val params = absListView.layoutParams
        params.height = totalHeight
        (params as MarginLayoutParams).setMargins(0, 0, 0, 0)
        absListView.layoutParams = params
    }

    /**
     * 描述：获取AbsListView的高度.
     *
     * @param absListView            the abs list view
     * @param lineNumber            每行几个 ListView一行一个item
     * @param verticalSpace            the vertical space
     * @return the abs list view height
     */
    fun getAbsListViewHeight(
        absListView: AbsListView,
        lineNumber: Int, verticalSpace: Int
    ): Int {
        var totalHeight = 0
        val w = MeasureSpec.makeMeasureSpec(0,
            MeasureSpec.UNSPECIFIED)
        val h = MeasureSpec.makeMeasureSpec(0,
            MeasureSpec.UNSPECIFIED)
        absListView.measure(w, h)
        val mListAdapter = absListView.adapter ?: return totalHeight
        val count = mListAdapter.count
        if (absListView is ListView) {
            for (i in 0 until count) {
                val listItem = mListAdapter.getView(i, null, absListView)
                listItem.measure(w, h)
                totalHeight += listItem.measuredHeight
            }
            totalHeight = if (count == 0) {
                verticalSpace
            } else {
                (totalHeight
                        + absListView.dividerHeight * (count - 1))
            }
        } else if (absListView is GridView) {
            var remain = count % lineNumber
            if (remain > 0) {
                remain = 1
            }
            if (mListAdapter.count == 0) {
                totalHeight = verticalSpace
            } else {
                val listItem = mListAdapter.getView(0, null, absListView)
                listItem.measure(w, h)
                val line = count / lineNumber + remain
                totalHeight = line * listItem.measuredHeight + (line - 1) * verticalSpace
            }
        }
        return totalHeight
    }

    /**
     * 测量这个view
     * 最后通过getMeasuredWidth()获取宽度和高度.
     * @param view 要测量的view
     * @return 测量过的view
     */
    fun measureView(view: View) {
        var p = view.layoutParams
        if (p == null) {
            p = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width)
        val lpHeight = p.height
        val childHeightSpec: Int
        childHeightSpec = if (lpHeight > 0) {
            MeasureSpec.makeMeasureSpec(lpHeight,
                MeasureSpec.EXACTLY)
        } else {
            MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED)
        }
        view.measure(childWidthSpec, childHeightSpec)
    }

    /**
     * 获得这个View的宽度
     * 测量这个view，最后通过getMeasuredWidth()获取宽度.
     * @param view 要测量的view
     * @return 测量过的view的宽度
     */
    fun getViewWidth(view: View): Int {
        measureView(view)
        return view.measuredWidth
    }

    /**
     * 获得这个View的高度
     * 测量这个view，最后通过getMeasuredHeight()获取高度.
     * @param view 要测量的view
     * @return 测量过的view的高度
     */
    fun getViewHeight(view: View): Int {
        measureView(view)
        return view.measuredHeight
    }

    /**
     * 从父亲布局中移除自己
     * @param v
     */
    fun removeSelfFromParent(v: View) {
        val parent = v.parent
        if (parent != null) {
            if (parent is ViewGroup) {
                parent.removeView(v)
            }
        }
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 描述：dip转换为px.
     *
     * @param context the context
     * @param dipValue the dip value
     * @return px值
     */
    fun dip2px(context: Context, dipValue: Float): Float {
        val mDisplayMetrics = AbAppUtil.getDisplayMetrics(context)
        return applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, mDisplayMetrics)
    }

    /**
     * 描述：px转换为dip.
     *
     * @param context the context
     * @param pxValue the px value
     * @return dip值
     */
    fun px2dip(context: Context, pxValue: Float): Float {
        val mDisplayMetrics = AbAppUtil.getDisplayMetrics(context)
        return pxValue / mDisplayMetrics!!.density
    }

    /**
     * 描述：sp转换为px.
     *
     * @param context the context
     * @param spValue the sp value
     * @return sp值
     */
    fun sp2px(context: Context, spValue: Float): Float {
        val mDisplayMetrics = AbAppUtil.getDisplayMetrics(context)
        return applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, mDisplayMetrics)
    }

    /**
     * 描述：px转换为sp.
     *
     * @param context the context
     * @return sp值
     */
    fun px2sp(context: Context, pxValue: Float): Float {
        val mDisplayMetrics = AbAppUtil.getDisplayMetrics(context)
        return pxValue / mDisplayMetrics!!.scaledDensity
    }

    /**
     * dp转成px
     * @param context
     * @param dp
     * @return
     */
    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    /**
     * dp转成px
     * @param context
     * @param dp
     * @return
     */
    fun dp2px(context: Context?, scale: Float, dp: Float): Int {
        return (dp * scale + 0.5f).toInt()
    }

    /**
     * 描述：根据屏幕大小缩放.
     *
     * @param context the context
     * @return the int
     */
    fun scale(context: Context, value: Float): Int {
        val mDisplayMetrics = AbAppUtil.getDisplayMetrics(context)
        return scale(context, mDisplayMetrics!!.widthPixels,
            mDisplayMetrics.heightPixels, value)
    }

    /**
     * 描述：根据屏幕大小缩放.
     *
     * @param displayWidth the display width
     * @param displayHeight the display height
     * @param pxValue the px value
     * @return the int
     */
    fun scale(context: Context?, displayWidth: Int, displayHeight: Int, pxValue: Float): Int {
        if (pxValue == 0f) {
            return 0
        }
        var scale = 1f
        try {
            val scaleWidth = displayWidth.toFloat() / AbAppConfig.UI_WIDTH
            val scaleHeight = displayHeight.toFloat() / AbAppConfig.UI_HEIGHT
            scale = Math.min(scaleWidth, scaleHeight)
        } catch (e: Exception) {
        }
        return Math.round(pxValue * scale + 0.5f)
    }

    /**
     * TypedValue官方源码中的算法，任意单位转换为PX单位
     * @param unit  TypedValue.COMPLEX_UNIT_DIP
     * @param value 对应单位的值
     * @param metrics 密度
     * @return px值
     */
    fun applyDimension(
        unit: Int, value: Float,
        metrics: DisplayMetrics?
    ): Float {
        when (unit) {
            TypedValue.COMPLEX_UNIT_PX -> return value
            TypedValue.COMPLEX_UNIT_DIP -> return value * metrics!!.density
            TypedValue.COMPLEX_UNIT_SP -> return value * metrics!!.scaledDensity
            TypedValue.COMPLEX_UNIT_PT -> return value * metrics!!.xdpi * (1.0f / 72)
            TypedValue.COMPLEX_UNIT_IN -> return value * metrics!!.xdpi
            TypedValue.COMPLEX_UNIT_MM -> return value * metrics!!.xdpi * (1.0f / 25.4f)
        }
        return 0f
    }

    /**
     *
     * 描述：View树递归调用做适配.
     * AbAppConfig.uiWidth = 1080;
     * AbAppConfig.uiHeight = 700;
     * scaleContentView((RelativeLayout)findViewById(R.id.rootLayout));
     * 要求布局中的单位都用px并且和美工的设计图尺寸一致，包括所有宽高，Padding,Margin,文字大小
     * @param contentView
     */
    fun scaleContentView(contentView: ViewGroup) {
        scaleView(contentView)
        if (contentView.childCount > 0) {
            for (i in 0 until contentView.childCount) {
                if (contentView.getChildAt(i) is ViewGroup) {
                    scaleContentView(contentView.getChildAt(i) as ViewGroup)
                } else {
                    scaleView(contentView.getChildAt(i))
                }
            }
        }
    }

    /**
     * 按比例缩放View，以布局中的尺寸为基准
     * @param view
     */
    fun scaleView(view: View) {
        if (view is TextView) {
            val textView = view
            setTextSize(textView, textView.textSize)
        }
        val params = view.layoutParams as ViewGroup.LayoutParams
        if (null != params) {
            var width = INVALID
            var height = INVALID
            if (params.width != ViewGroup.LayoutParams.WRAP_CONTENT
                && params.width != ViewGroup.LayoutParams.MATCH_PARENT
            ) {
                width = params.width
            }
            if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT
                && params.height != ViewGroup.LayoutParams.MATCH_PARENT
            ) {
                height = params.height
            }

            //size
            setViewSize(view, width, height)

            // Padding
            setPadding(view,
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                view.paddingBottom)
        }

        // Margin
        if (view.layoutParams is MarginLayoutParams) {
            val mMarginLayoutParams = view
                .layoutParams as MarginLayoutParams
            if (mMarginLayoutParams != null) {
                setMargin(view,
                    mMarginLayoutParams.leftMargin,
                    mMarginLayoutParams.topMargin,
                    mMarginLayoutParams.rightMargin,
                    mMarginLayoutParams.bottomMargin)
            }
        }
    }

    /**
     * 按比例缩放View，以布局中的尺寸为基准 根据dp
     * @param view
     */
    fun scaleView(context: Context, view: View) {
        if (view is TextView) {
            val textView = view
            setTextSize(textView, dp2px(context, textView.textSize).toFloat())
        }
        val params = view.layoutParams as ViewGroup.LayoutParams
        if (null != params) {
            var width = INVALID
            var height = INVALID
            if (params.width != ViewGroup.LayoutParams.WRAP_CONTENT
                && params.width != ViewGroup.LayoutParams.MATCH_PARENT
            ) {
                width = params.width
            }
            if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT
                && params.height != ViewGroup.LayoutParams.MATCH_PARENT
            ) {
                height = params.height
            }
            //            width=(int) dp2px(context,defaultSancle, width);
//            height=(int) dp2px(context,defaultSancle,height);
//            width=(int) px2dip(context, width);
//            height=(int) px2dip(context, height);
            //size
            setViewSize(view, width, height)

            // Padding
            setPadding(view,
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                view.paddingBottom)
        }

        // Margin
        if (view.layoutParams is MarginLayoutParams) {
            val mMarginLayoutParams = view
                .layoutParams as MarginLayoutParams
            if (mMarginLayoutParams != null) {
                setMargin(view,
                    mMarginLayoutParams.leftMargin,
                    mMarginLayoutParams.topMargin,
                    mMarginLayoutParams.rightMargin,
                    mMarginLayoutParams.bottomMargin)
            }
        }
    }

    /**
     * 缩放文字大小
     * @param textView button
     * @param size sp值
     * @return
     */
    fun setSPTextSize(textView: TextView, size: Float) {
        val scaledSize = scale(textView.context, size).toFloat()
        textView.textSize = scaledSize
    }

    /**
     * 缩放文字大小,这样设置的好处是文字的大小不和密度有关，
     * 能够使文字大小在不同的屏幕上显示比例正确
     * @param textView button
     * @param sizePixels px值
     * @return
     */
    fun setTextSize(textView: TextView, sizePixels: Float) {
        val scaledSize = scale(textView.context, sizePixels).toFloat()
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledSize)
    }

    /**
     * 缩放文字大小
     * @param context
     * @param textPaint
     * @param sizePixels px值
     * @return
     */
    fun setTextSize(context: Context, textPaint: TextPaint, sizePixels: Float) {
        val scaledSize = scale(context, sizePixels).toFloat()
        textPaint.textSize = scaledSize
    }

    /**
     * 缩放文字大小
     * @param context
     * @param paint
     * @param sizePixels px值
     * @return
     */
    fun setTextSize(context: Context, paint: Paint, sizePixels: Float) {
        val scaledSize = scale(context, sizePixels).toFloat()
        paint.textSize = scaledSize
    }

    /**
     * 设置View的PX尺寸
     * @param view  如果是代码new出来的View，需要设置一个适合的LayoutParams
     * @param widthPixels
     * @param heightPixels
     */
    fun setViewSize(view: View, widthPixels: Int, heightPixels: Int) {
        val scaledWidth = scale(view.context, widthPixels.toFloat())
        val scaledHeight = scale(view.context, heightPixels.toFloat())
        val params = view.layoutParams
        if (params == null) {
            AbLogUtil.e(AbViewUtil::class.java,
                "setViewSize出错,如果是代码new出来的View，需要设置一个适合的LayoutParams")
            return
        }
        if (widthPixels != INVALID) {
            params.width = scaledWidth
        }
        if (heightPixels != INVALID) {
            params.height = scaledHeight
        }
        view.layoutParams = params
    }

    /**
     * 设置PX padding.
     *
     * @param view the view
     * @param left the left padding in pixels
     * @param top the top padding in pixels
     * @param right the right padding in pixels
     * @param bottom the bottom padding in pixels
     */
    fun setPadding(
        view: View, left: Int,
        top: Int, right: Int, bottom: Int
    ) {
        val scaledLeft = scale(view.context, left.toFloat())
        val scaledTop = scale(view.context, top.toFloat())
        val scaledRight = scale(view.context, right.toFloat())
        val scaledBottom = scale(view.context, bottom.toFloat())
        view.setPadding(scaledLeft, scaledTop, scaledRight, scaledBottom)
    }

    /**
     * 设置 PX margin.
     *
     * @param view the view
     * @param left the left margin in pixels
     * @param top the top margin in pixels
     * @param right the right margin in pixels
     * @param bottom the bottom margin in pixels
     */
    fun setMargin(
        view: View, left: Int, top: Int,
        right: Int, bottom: Int
    ) {
        val scaledLeft = scale(view.context, left.toFloat())
        val scaledTop = scale(view.context, top.toFloat())
        val scaledRight = scale(view.context, right.toFloat())
        val scaledBottom = scale(view.context, bottom.toFloat())
        if (view.layoutParams is MarginLayoutParams) {
            val mMarginLayoutParams = view
                .layoutParams as MarginLayoutParams
            if (mMarginLayoutParams != null) {
                if (left != INVALID) {
                    mMarginLayoutParams.leftMargin = scaledLeft
                }
                if (right != INVALID) {
                    mMarginLayoutParams.rightMargin = scaledRight
                }
                if (top != INVALID) {
                    mMarginLayoutParams.topMargin = scaledTop
                }
                if (bottom != INVALID) {
                    mMarginLayoutParams.bottomMargin = scaledBottom
                }
                view.layoutParams = mMarginLayoutParams
            }
        }
    }
}