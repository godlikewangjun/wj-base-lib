@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("WjStringsKt")
package com.wj.ktutils

import android.app.Fragment
import android.content.Context
import com.abase.util.AbStrUtil
import com.abase.util.ToastUtil
import com.abase.util.Tools

/**
 * String扩展方法
 * @author Admin
 * @version 1.0
 * @date 2018/2/27
 */
inline fun CharSequence?.isNull(): Boolean =
        AbStrUtil.isEmpty(this.toString())

inline fun CharSequence?.isPhoneNum(): Boolean =
        AbStrUtil.isMobileNo(this.toString())

inline fun CharSequence?.isEmail(): Boolean =
        AbStrUtil.isEmail(this.toString())

inline fun Context?.showTip(string: String) =
       ToastUtil.showTip(this,string)

inline fun Fragment?.showTip(string: String) =
        ToastUtil.showTip(this!!.activity,string)

inline fun android.support.v4.app.Fragment?.showTip(string: String) =
        ToastUtil.showTip(this!!.activity,string)