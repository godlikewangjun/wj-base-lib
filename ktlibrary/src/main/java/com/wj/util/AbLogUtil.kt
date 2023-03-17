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
import java.lang.Class
import android.util.Log

/**
 * 名称：AbLogUtil.java
 * 描述：日志工具类.
 */
object AbLogUtil {
    /** debug开关.  */
    var D = true

    /**每行日志最多好长 长了就换行继续显示 */
    private const val LOG_MAXLENGTH = 1024 * 4 - 10

    /**
     * debug日志
     * @param tag
     * @param message
     */
    fun d(tag: String?, message: String) {
        var message = message
        if (D) {
            val strLength = message.length
            if (strLength <= LOG_MAXLENGTH) {
                Log.d(tag, message)
                return
            }
            while (message.length > LOG_MAXLENGTH) {
                Log.d(tag, message.substring(0, LOG_MAXLENGTH))
                message = message.substring(LOG_MAXLENGTH)
            }
        }
    }

    /**
     * debug日志
     * @param context
     * @param message
     */
    fun d(context: Context, message: String) {
        val tag = context.javaClass.simpleName
        d(tag, message)
    }

    /**
     * debug日志
     * @param clazz
     * @param message
     */
    fun d(clazz: Class<*>, message: String) {
        val tag = clazz.simpleName
        d(tag, message)
    }

    /**
     * info日志
     * @param tag
     * @param message
     */
    fun i(tag: String?, message: String) {
        var message = message
        if (D) {
            val strLength = message.length
            val start = 0
            val end = LOG_MAXLENGTH
            var count = 1
            if (strLength % LOG_MAXLENGTH > 0) {
                count = strLength / LOG_MAXLENGTH + 1
            } else if (strLength % LOG_MAXLENGTH == 0) {
                count = strLength / LOG_MAXLENGTH
            }
            if (strLength <= LOG_MAXLENGTH) {
                Log.i(tag, message)
                return
            }
            while (message.length > LOG_MAXLENGTH) {
                Log.i(tag, message.substring(0, LOG_MAXLENGTH))
                message = message.substring(LOG_MAXLENGTH)
            }
        }
    }

    /**
     * info日志
     * @param context
     * @param message
     */
    fun i(context: Context, message: String) {
        val tag = context.javaClass.simpleName
        i(tag, message)
    }

    /**
     * info日志
     * @param clazz
     * @param message
     */
    fun i(clazz: Class<*>, message: String) {
        val tag = clazz.simpleName
        i(tag, message)
    }

    /**
     * error日志
     * @param tag
     * @param message
     */
    fun e(tag: String?, message: String) {
        var message = message
        if (D) {
            val strLength = message.length
            val start = 0
            val end = LOG_MAXLENGTH
            var count = 1
            if (strLength % LOG_MAXLENGTH > 0) {
                count = strLength / LOG_MAXLENGTH + 1
            } else if (strLength % LOG_MAXLENGTH == 0) {
                count = strLength / LOG_MAXLENGTH
            }
            if (strLength <= LOG_MAXLENGTH) {
                Log.e(tag, message)
                return
            }
            while (message.length > LOG_MAXLENGTH) {
                Log.e(tag, message.substring(0, LOG_MAXLENGTH))
                message = message.substring(LOG_MAXLENGTH)
            }
        }
    }

    /**
     * error日志
     * @param context
     * @param message
     */
    fun e(context: Context, message: String) {
        val tag = context.javaClass.simpleName
        e(tag, message)
    }

    /**
     * error日志
     * @param clazz
     * @param message
     */
    fun e(clazz: Class<*>, message: String) {
        val tag = clazz.simpleName
        e(tag, message)
    }

    /**
     * error日志
     * @param tag
     * @param message
     */
    fun w(tag: String?, message: String) {
        var message = message
        if (D) {
            val strLength = message.length
            val start = 0
            val end = LOG_MAXLENGTH
            var count = 1
            if (strLength % LOG_MAXLENGTH > 0) {
                count = strLength / LOG_MAXLENGTH + 1
            } else if (strLength % LOG_MAXLENGTH == 0) {
                count = strLength / LOG_MAXLENGTH
            }
            if (strLength <= LOG_MAXLENGTH) {
                Log.w(tag, message)
                return
            }
            while (message.length > LOG_MAXLENGTH) {
                Log.w(tag, message.substring(0, LOG_MAXLENGTH))
                message = message.substring(LOG_MAXLENGTH)
            }
        }
    }

    /**
     * error日志
     * @param context
     * @param message
     */
    fun w(context: Context, message: String) {
        val tag = context.javaClass.simpleName
        w(tag, message)
    }

    /**
     * error日志
     * @param clazz
     * @param message
     */
    fun w(clazz: Class<*>, message: String) {
        val tag = clazz.simpleName
        w(tag, message)
    }

    /**
     * 设置日志的开关
     * @param e
     */
    fun closeLog() {
        D = false
    }
}