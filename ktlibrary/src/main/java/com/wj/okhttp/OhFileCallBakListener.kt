package com.wj.okhttp

import java.lang.Exception

/**
 * 文件监听回调
 * @author wangjun
 * @version 2.0
 * @date 2016年2月2日
 */
abstract class OhFileCallBakListener : OhCallBackListener<Any>(), OhProgressListener {
    /**
     * 连接成功
     */
    abstract fun onSuccess(content: String)

    /**
     * 服务器连接失败
     */
    abstract fun onFailure(code: String, content: String)

    /**
     * 连接接错误
     */
    abstract fun onError(e: Exception)

    /**
     * 完成
     */
    abstract fun onFinish()

    /**
     * 开始
     */
    open fun onStart() {}
}