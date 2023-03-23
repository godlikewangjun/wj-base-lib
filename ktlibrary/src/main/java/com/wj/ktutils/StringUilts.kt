@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("WjStringsKt")
package com.wj.ktutils

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.wj.util.AbStrUtil
import com.wj.util.ToastUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * String扩展方法
 * @author Admin
 * @version 1.0
 * @date 2018/2/27
 */
fun CharSequence?.isNull(): Boolean =
        AbStrUtil.isEmpty(this.toString())

fun CharSequence?.isPhoneNum(): Boolean =
        AbStrUtil.isMobileNo(this.toString())

fun CharSequence?.isEmail(): Boolean =
        AbStrUtil.isEmail(this.toString())

@OptIn(DelicateCoroutinesApi::class)
fun Context?.showTip(string: String)=
        GlobalScope.launch(Dispatchers.Main) {  ToastUtil.showTip(this@showTip!!,string) }


@OptIn(DelicateCoroutinesApi::class)
fun androidx.fragment.app.Fragment?.showTip(string: String) =
        GlobalScope.launch(Dispatchers.Main) {   this@showTip!!.context?.let { ToastUtil.showTip(it,string) } }

fun Activity?.showTip(string: String) =
        this!!.runOnUiThread { ToastUtil.showTip(this,string) }

fun Activity?.putValue(key: String,string: Any) =
       WjSP.getInstance()?.setValues(key,string)

fun Activity?.getValue(key: String,string: Any) =
        WjSP.getInstance()?.getValues(key,string)

fun Fragment?.putValue(key: String,string: Any) =
        WjSP.getInstance()?.setValues(key,string)

fun Fragment?.getValue(key: String,string: Any) =
        WjSP.getInstance()?.getValues(key,string)

fun Context?.putValue(key: String,string: Any) =
        WjSP.getInstance()?.setValues(key,string)

fun Context?.getValue(key: String,string: Any) =
        WjSP.getInstance()?.getValues(key,string)