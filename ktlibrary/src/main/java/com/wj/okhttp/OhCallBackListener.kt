package com.wj.okhttp

import java.lang.Exception
import android.os.Message

/**
 * 基类响应
 *
 * @author wangjun
 * @version 2.0
 * @date 2016年2月1日
 */
abstract class OhCallBackListener<T> {
    /**
     * 0 是上传 1是下载
     */
    var ohType = -1
    /**
     * Gets the handler.
     *
     * @return the handler
     */
    /**
     * 描述：设置Handler.
     *
     * @param handler the new handler
     */
    var handler: OhCallBackMessageInterface? = null

    /**
     * 失败消息.
     */
    fun sendFailureMessage(statusCode: Int, content: String, e: Exception?) {
        sendMessage(obtainMessage(OhHttpClient.FAILURE_MESSAGE,
            arrayOf<Any?>(statusCode, content, e)))
    }

    /**
     * 成功.
     */
    fun sendSuccessMessage(content: Any) {
        sendMessage(obtainMessage(OhHttpClient.SUCCESS_MESSAGE, arrayOf(content)))
    }

    /**
     * 进度消息.
     *
     * @param bytesWritten the bytes written
     * @param totalSize    the total size
     */
    fun sendProgressMessage(bytesWritten: Long, totalSize: Long, isend: Boolean) {
        sendMessage(obtainMessage(OhHttpClient.PROGRESS_MESSAGE,
            arrayOf<Any>(bytesWritten, totalSize, isend)))
    }

    /**
     * 完成消息
     */
    fun sendFinishMessage() {
        sendMessage(obtainMessage(OhHttpClient.FINSH_MESSAGE, arrayOf<Any>()))
    }

    /**
     * 开始消息
     */
    fun sendStartMessage() {
        sendMessage(obtainMessage(OhHttpClient.START_MESSAGE, arrayOf<Any>()))
    }

    /**
     * 发送消息.
     *
     * @param msg the msg
     */
    private fun sendMessage(msg: Message?) {
        if (msg != null && handler != null) {
            handler!!.handedMessage(msg)
        }
    }

    /**
     * 构造Message.
     *
     * @param responseMessage the response message
     * @param response        the response
     * @return the message
     */
    private fun obtainMessage(responseMessage: Int, response: Any): Message? {
        val msg = Message.obtain()
        if (msg != null) {
            msg.what = responseMessage
            msg.obj = response
        }
        return msg
    }
}