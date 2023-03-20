package com.wj.util

import android.R
import android.content.Context
import android.graphics.Color
import java.lang.ref.SoftReference
import com.wj.ui.view.weight.StyleableToast

/**
 * toast 工具类
 * @author Admin
 * @version 1.0
 * @date 2018/8/2
 */
object ToastUtil {
    var toast // 提示
            : SoftReference<StyleableToast.Builder?>? = null
    private const val isShowDeBug = true // 提示
    var toastBuilderListener: ToastBuilderListener? = null

    /**
     * 显示toast
     *
     * @param context activity
     * @param text    显示的内容
     */
    fun showTip(context: Context, text: String?) {
        toast = SoftReference(cusBuild(context).text(text))
        toast!!.get()!!.show()
    }

    /**
     * 显示toast
     *
     * @param context activity
     * @param text    显示的内容
     */
    fun showTipOne(context: Context, text: String?, gravity: Int) {
        if (toast != null && toast!!.get() != null) {
            toast!!.get()!!.text(text)
        } else {
            toast = SoftReference(cusBuild(context).text(text).gravity(gravity))
        }
        toast!!.get()!!.show()
    }

    /**
     * 返回公共的构建参数
     * @param context
     * @return
     */
    private fun cusBuild(context: Context): StyleableToast.Builder {
        val builder = StyleableToast.Builder(context)
            .textColor(context.resources.getColor(R.color.white))
            .backgroundColor(Color.parseColor("#507DFE"))
        if (toastBuilderListener != null) toastBuilderListener!!.create(builder)
        return builder
    }

    interface ToastBuilderListener {
        fun create(builder: StyleableToast.Builder?)
    }
}