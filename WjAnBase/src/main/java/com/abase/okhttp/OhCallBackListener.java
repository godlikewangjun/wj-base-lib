package com.abase.okhttp;

import android.os.Handler;
import android.os.Message;

/**
 * 基类响应
 *
 * @author wangjun
 * @version 2.0
 * @date 2016年2月1日
 */
public abstract class OhCallBackListener<T> {
    /**
     * 0 是上传 1是下载
     */
    public int ohtype = -1;
    private OhCallBackMessageInterface callBackMessageInterface;

    public OhCallBackListener() {
    }

    /**
     * 失败消息.
     */
    public void sendFailureMessage(int statusCode, String content, Exception e) {
        sendMessage(obtainMessage(OhHttpClient.FAILURE_MESSAGE, new Object[]{statusCode, content, e}));
    }

    /**
     * 成功.
     */
    public void sendSucessMessage(final T content) {
        sendMessage(obtainMessage(OhHttpClient.SUCCESS_MESSAGE, new Object[]{content}));
    }

    /**
     * 进度消息.
     *
     * @param bytesWritten the bytes written
     * @param totalSize    the total size
     */
    public void sendProgressMessage(long bytesWritten, long totalSize, boolean isend) {
        sendMessage(obtainMessage(OhHttpClient.PROGRESS_MESSAGE, new Object[]{bytesWritten, totalSize, isend}));
    }

    /**
     * 完成消息
     */
    public void sendFinshMessage() {
        sendMessage(obtainMessage(OhHttpClient.FINSH_MESSAGE, new Object[]{}));
    }

    /**
     * 开始消息
     */
    public void sendStartMessage() {
        sendMessage(obtainMessage(OhHttpClient.START_MESSAGE, new Object[]{}));
    }

    /**
     * 发送消息.
     *
     * @param msg the msg
     */
    private void sendMessage(Message msg) {
        if (msg != null && callBackMessageInterface != null) {
            callBackMessageInterface.handedMessage(msg);
        }
    }

    /**
     * 构造Message.
     *
     * @param responseMessage the response message
     * @param response        the response
     * @return the message
     */
    private Message obtainMessage(int responseMessage, Object response) {
        Message msg = Message.obtain();
        if (msg != null) {
            msg.what = responseMessage;
            msg.obj = response;
        }
        return msg;
    }

    /**
     * Gets the handler.
     *
     * @return the handler
     */
    public OhCallBackMessageInterface getHandler() {
        return callBackMessageInterface;
    }

    /**
     * 描述：设置Handler.
     *
     * @param handler the new handler
     */
    public void setHandler(OhCallBackMessageInterface handler) {
        this.callBackMessageInterface = handler;
    }


}
