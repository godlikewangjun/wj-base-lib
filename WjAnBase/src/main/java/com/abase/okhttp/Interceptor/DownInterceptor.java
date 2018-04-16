package com.abase.okhttp.Interceptor;

import com.abase.okhttp.OhFileCallBakListener;
import com.abase.okhttp.ProgressResponseBody;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 下载进度拦截
 * @author Admin
 * @version 1.0
 * @date 2018/1/5
 */

public class DownInterceptor implements Interceptor {
    private OhFileCallBakListener callbackListener;

    public DownInterceptor(OhFileCallBakListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //拦截
        Response originalResponse = chain.proceed(chain.request());
        //包装响应体并返回
        return originalResponse.newBuilder().body(new
                ProgressResponseBody(originalResponse.body(),
                callbackListener)).build();
    }
}
