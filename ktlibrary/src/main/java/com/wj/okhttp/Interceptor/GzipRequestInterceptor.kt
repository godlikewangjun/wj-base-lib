package com.wj.okhttp.Interceptor

import java.io.IOException
import kotlin.Throws
import okio.BufferedSink
import okio.GzipSink
import okhttp3.*
import okio.buffer

/**
 * @author Admin
 * @version 1.0
 * @date 2018/1/5
 */
/**
 * 拦截器压缩http请求体，许多服务器无法解析
 */
class GzipRequestInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        if (originalRequest.body == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest)
        }
        val compressedRequest = originalRequest.newBuilder().header("Content-Encoding", "gzip")
            .method(originalRequest.method, gzip(originalRequest.body)).build()
        return chain.proceed(compressedRequest)
    }

    /**
     * gzip压缩
     */
    private fun gzip(body: RequestBody?): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return body!!.contentType()
            }

            override fun contentLength(): Long {
                return -1 // 无法知道压缩后的数据大小
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val gzipSink = GzipSink(sink).buffer()
                body!!.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }
}