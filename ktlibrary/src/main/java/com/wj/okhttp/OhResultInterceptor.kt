package com.wj.okhttp

/**
 * @author wangjun
 * @version 1.0
 * @date 2016/10/31
 */
abstract class OhResultInterceptor {
    fun Result(`object`: Any?): Boolean {
        return false
    }
}