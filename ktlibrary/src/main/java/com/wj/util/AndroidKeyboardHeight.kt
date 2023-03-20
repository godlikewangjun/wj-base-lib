package com.wj.util

import android.R
import android.annotation.SuppressLint
import android.content.Context
import java.lang.Exception
import java.lang.Class
import android.view.View
import android.util.DisplayMetrics
import android.app.Activity
import android.os.Build
import android.view.ViewGroup
import android.graphics.Rect
import android.os.Build.VERSION
import android.widget.FrameLayout
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import java.lang.reflect.Field

/**
 * 自适应弹出键盘
 * @author wangjun
 * @version 1.0
 * @date 2016/12/5
 */
class AndroidKeyboardHeight {
    private var mChildOfContent: View? = null
    private var usableHeightPrevious = 0
    private var frameLayoutParams: ViewGroup.LayoutParams? = null
    private var contentHeight = 0
    private var isfirst = true
    private var activity: Activity
    private var statusBarHeight = 0
    var wh : IntArray?=null // 屏幕的宽和高
    private var view: View? = null
    private var rootView: View? = null

    private constructor(activity: Activity) {
        this.activity = activity
        init()
    }

    private constructor(activity: Activity, view: View) {
        this.view = view
        this.activity = activity
        init()
    }

    private fun init() {
        //获取状态栏的高度
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        statusBarHeight = activity.resources.getDimensionPixelSize(resourceId)
        val content = activity.findViewById<View>(R.id.content) as FrameLayout
        rootView = content.getChildAt(0)
        mChildOfContent = if (view == null) {
            rootView
        } else {
            view
        }


        //界面出现变动都会调用这个监听事件
        mChildOfContent!!.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (isfirst) {
                    frameLayoutParams = mChildOfContent!!.layoutParams
                    contentHeight = mChildOfContent!!.height //兼容华为等机型
                    isfirst = false
                }
                if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mChildOfContent!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener {
            if (!isfirst) {
                possiblyResizeChildOfContent()
            }
        }
    }

    //重新调整跟布局的高度
    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()

        //当前可见高度和上一次可见高度不一致 布局变动
        if (usableHeightNow != usableHeightPrevious) {
            //int usableHeightSansKeyboard2 = mChildOfContent.getHeight();//兼容华为等机型
            val usableHeightSansKeyboard = mChildOfContent!!.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                if (usableHeightSansKeyboard - getScreenWH(mChildOfContent!!.context)[1] != statusBarHeight) {
                    //frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                    frameLayoutParams!!.height =
                        usableHeightSansKeyboard - heightDifference + statusBarHeight
                } else if (isTranslucentStatus) {
                    frameLayoutParams!!.height =
                        usableHeightSansKeyboard - heightDifference + statusBarHeight
                } else {
                    frameLayoutParams!!.height = usableHeightSansKeyboard - heightDifference
                }
            } else {
                frameLayoutParams!!.height = contentHeight
            }
            mChildOfContent!!.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    fun resetHeight() {
        frameLayoutParams!!.height = contentHeight
        mChildOfContent!!.requestLayout()
        usableHeightPrevious = computeUsableHeight()
    }

    /**     * 计算mChildOfContent可见高度     ** @return      */
    private fun computeUsableHeight(): Int {
        val r = Rect()
        mChildOfContent!!.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top
    }

    /**
     * 是否是状态透明的style
     * @return
     */
    private val isTranslucentStatus: Boolean
        private get() {
            var b = false
            val attrsArray = intArrayOf(R.attr.windowTranslucentStatus)
            val typedArray = activity.obtainStyledAttributes(attrsArray)
            b = typedArray.getBoolean(0, b)
            typedArray.recycle()
            return b
        }

    /**
     * 获取屏幕大小
     *
     * 1是宽 2是高
     */
    fun getScreenWH(context: Context): IntArray {
        if (wh != null && wh!![0] != 0 && wh!![1] != 0) {
            return wh!!
        }
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay
            .getMetrics(displayMetrics)
        var width = 0
        var height = 0
        width = displayMetrics.widthPixels
        height = displayMetrics.heightPixels - getStatusBarHeight(context) // 去掉通知栏的高度
        val `is` = intArrayOf(width, height)
        wh = `is`
        return `is`
    }

    companion object {
        fun assistActivity(activity: Activity): AndroidKeyboardHeight {
            return AndroidKeyboardHeight(activity)
        }

        fun assistActivity(activity: Activity, view: View): AndroidKeyboardHeight {
            return AndroidKeyboardHeight(activity, view)
        }

        /**
         * 获取通知栏的高度
         *
         * @param context
         * @return
         */
        @SuppressLint("PrivateApi")
        fun getStatusBarHeight(context: Context): Int {
            var c: Class<*>?
            var obj: Any?
            var field: Field?
            var x = 0
            var statusBarHeight = 0
            try {
                c = Class.forName("com.android.internal.R\$dimen")
                obj = c.newInstance()
                field = c.getField("status_bar_height")
                x = field[obj]?.toString()!!.toInt()
                statusBarHeight = context.resources.getDimensionPixelSize(x)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
            return statusBarHeight
        }
    }
}