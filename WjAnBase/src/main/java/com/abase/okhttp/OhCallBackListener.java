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
    private static OhResultInterceptor ohResultInterceptor;

    public OhCallBackListener() {
    }

    /**
     * The handler.
     */
    private Handler mHandler;

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
        if(ohResultInterceptor!=null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(!ohResultInterceptor.Result(content)){
                        sendMessage(obtainMessage(OhHttpClient.SUCCESS_MESSAGE, new Object[]{content}));
                    }
                }
            });

        }
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
        if (msg != null && mHandler != null) {
            msg.sendToTarget();
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
        Message msg;
        if (mHandler != null) {
            msg = mHandler.obtainMessage(responseMessage, response);
        } else {
            msg = Message.obtain();
            if (msg != null) {
                msg.what = responseMessage;
                msg.obj = response;
            }
        }
        return msg;
    }

    /**
     * Gets the handler.
     *
     * @return the handler
     */
    public Handler getHandler() {
        return mHandler;
    }

    /**
     * 描述：设置Handler.
     *
     * @param handler the new handler
     */
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    /**
     * 设置返回成功的拦截
     */
    public static void setResultInterceptor(OhResultInterceptor resultInterceptor) {
        OhCallBackListener.ohResultInterceptor = resultInterceptor;
    }

}
