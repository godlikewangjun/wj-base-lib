package com.wj.okhttp

import java.lang.Class
import java.lang.reflect.ParameterizedType

/**
 * okhttp 的 返回调用接口
 * @author wangjun
 * @version 2.0
 * @date 2016年2月1日
 */
abstract class OhObjectListener<T> : OhCallBackListener<T>() {
    var classname: Class<T>? = null

    init {
        val className = this.javaClass.genericSuperclass
        if (className != null && className is ParameterizedType) {
            val pt = this.javaClass.genericSuperclass as ParameterizedType
            classname = pt.actualTypeArguments[0] as Class<T>
        }
    }

    /**
     * 服务器连接失败
     */
    abstract fun onFailure(code: Int, content: String?, error: Throwable?)

    /**
     * 连接成功
     */
    abstract fun onSuccess(content: T)

    /**
     * 完成
     */
    abstract fun onFinish()

    /**
     * 开始
     */
    open fun onStart() {}
}