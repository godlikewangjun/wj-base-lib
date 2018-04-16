@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("WjDateUtilsKt")
package com.wj.ktutils

import com.abase.util.AbStrUtil
import java.util.*

/**
 * 时间操作
 * @author Admin
 * @version 1.0
 * @date 2018/3/27
 */
inline fun Date?.formatDate(): String =
        AbStrUtil.dateTimeFormat(this)