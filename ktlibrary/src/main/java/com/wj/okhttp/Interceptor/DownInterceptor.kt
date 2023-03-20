package com.wj.okhttp.Interceptor

import java.io.IOException
import okhttp3.Response
import okhttp3.Interceptor
import kotlin.Throws
import com.wj.okhttp.*

/**
 * 下载进度拦截
 * @author Admin
 * @version 1.0
 * @date 2018/1/5
 */
class DownInterceptor(private val callbackListener: OhFileCallBakListener) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        //拦截
        val originalResponse: Response = chain.proceed(chain.request())
        //包装响应体并返回
        return originalResponse.newBuilder().body(ProgressResponseBody(originalResponse.body,
            callbackListener)).build()
    }
}