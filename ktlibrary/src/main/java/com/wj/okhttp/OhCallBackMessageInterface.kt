package com.wj.okhttp

import android.os.Message

/**
 * 消息回调定义接口
 * @author Administrator
 * @version 1.0
 * @date 2018/10/31
 */
interface OhCallBackMessageInterface {
    //消息处理
    fun handedMessage(msg: Message)
}