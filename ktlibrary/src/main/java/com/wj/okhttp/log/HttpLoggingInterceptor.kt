/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wj.okhttp.log

import java.lang.Exception
import java.io.IOException
import kotlin.jvm.JvmOverloads
import com.wj.util.AbLogUtil
import okhttp3.internal.platform.Platform
import java.util.TreeSet
import java.lang.NullPointerException
import kotlin.Throws
import java.nio.charset.Charset
import java.net.URLDecoder
import java.io.UnsupportedEncodingException
import com.wj.util.AbStrUtil
import java.io.EOFException
import okhttp3.*
import okhttp3.internal.http.promisesBody
import okio.*
import java.util.concurrent.TimeUnit

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors] or as a [ ][OkHttpClient.networkInterceptors].
 *
 * The format of the logs created by
 * this class should not be considered stable and may change slightly between releases. If you need
 * a stable logging format, use your own interceptor.
 */
class HttpLoggingInterceptor @JvmOverloads constructor(val logger: Logger = Logger.DEFAULT) :
    Interceptor {
    /**
     * 是否打印json格式化之前的数据
     * 默认是false
     */
    private val isPrintResult = false

    /**
     * 处理body解析,防止卡顿放在线程处理
     */
    interface BodyParsing {
        fun bodyResult(inputBody: String?): String
    }

    var bodyParsing: BodyParsing? = null

    enum class Level {
        /**
         * No logs.
         */
        NONE,

        /**
         * Logs request and response lines.
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
        `</pre> *
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
        `</pre> *
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
        `</pre> *
         */
        BODY
    }

    interface Logger {
        fun log(message: String?)

        companion object {
            /**
             * A [Logger] defaults output appropriate for the current platform.
             */
            val DEFAULT: Logger = object : Logger {
                override fun log(message: String?) {
                    if (style_log == 0) AbLogUtil.w(
                        HttpLoggingInterceptor::class.java, message!!) else Platform.get()
                        .log(message!!, Platform.WARN, null)
                }
            }
        }
    }

    private var headersToRedact = emptySet<String>()
    fun redactHeader(name: String) {
        val newHeadersToRedact: MutableSet<String> =
            TreeSet(java.lang.String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact.addAll(headersToRedact)
        newHeadersToRedact.add(name)
        headersToRedact = newHeadersToRedact
    }

    var level = Level.BODY

    /**
     * Change the level at which this interceptor logs.
     */
    fun setLevel(level: Level?): HttpLoggingInterceptor {
        if (level == null) throw NullPointerException("level == null. Use Level.NONE instead.")
        this.level = level
        return this
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = level
        val request: Request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }
        val logBody = level == Level.BODY
        val logHeaders = logBody || level == Level.HEADERS
        val requestBody = request.body
        val hasRequestBody = requestBody != null
        val connection = chain.connection()
        var requestStartMessage = ("--> "
                + request.method
                + ' ' + request.url
                + if (connection != null) " " + connection.protocol() else "")
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody!!.contentLength() + "-byte body)"
        }
        logger.log(requestStartMessage)
        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody!!.contentType() != null) {
                    logger.log("Content-Type: " + requestBody.contentType())
                }
                if (requestBody.contentLength() != -1L) {
                    logger.log("Content-Length: " + requestBody.contentLength())
                }
            }

//            request= request.newBuilder().removeHeader("Connection").addHeader("Connection", "close").build();
            val headers = request.headers
            var i = 0
            val count = headers.size
            while (i < count) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(name,
                        ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)
                ) {
                    logHeader(headers, i)
                }
                i++
            }
            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method)
            } else if (bodyHasUnknownEncoding(request.headers)) {
                logger.log("--> END " + request.method + " (encoded body omitted)")
            } else {
                var charset = UTF8
                val contentType = requestBody!!.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                val buffer = Buffer()
                if (contentType!!.subtype != "form-data") {
                    requestBody.writeTo(buffer)
                } else {
                    logger.log("form-data is not print RequestParams")
                }
                logger.log("")
                logger.log("--> RequestParams")
                if (isPlaintext(buffer)) { //form-data
                    var bodyStr = buffer.readString(charset!!)
                    try {
                        if (contentType.subtype != "form-data") bodyStr =
                            URLDecoder.decode(bodyStr, "utf-8") //反编码请求的参数
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                    if (OhHttpClient.init
                            .isJsonFromMat && (bodyStr.startsWith("{") || bodyStr.startsWith("["))
                    ) {
                        try {
                            logger.log("""
    
                                ${AbStrUtil.formatJson(bodyStr)}
                                """.trimIndent())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            logger.log(bodyStr)
                        }
                    } else {
                        logger.log(bodyStr)
                    }
                    logger.log("<-- RequestParams")
                    logger.log("--> END " + request.method
                            + " (" + requestBody.contentLength() + "-byte body)")
                } else {
                    logger.log("<-- RequestParams")
                    logger.log("--> END " + request.method + " (binary "
                            + requestBody.contentLength() + "-byte body omitted)")
                }
                buffer.close() //关闭流
            }
        }
        val startNs = System.nanoTime()
        val response: Response
        response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            logger.log("<-- HTTP FAILED: $e")
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody = response.body
        val contentLength = responseBody!!.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.log("<-- "
                + response.code
                + (if (response.message.isEmpty()) "" else ' '.toString() + response.message)
                + ' ' + response.request.url
                + " (times:" + tookMs + "," + (if (!logHeaders) ", $bodySize body" else "") + ')')
        if (logHeaders) {
            logger.log("--> Headers")
            val headers = response.headers
            var i = 0
            val count = headers.size
            while (i < count) {
                logHeader(headers, i)
                i++
            }
            logger.log("<-- Headers")
            if (!logBody || !response.promisesBody()) {
                logger.log("<-- END HTTP")
            } else if (bodyHasUnknownEncoding(response.headers)) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body.
                var buffer = source.buffer()
                var gzippedLength: Long? = null
                if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                    gzippedLength = buffer.size
                    var gzippedResponseBody: GzipSource? = null
                    try {
                        gzippedResponseBody = GzipSource(buffer.clone())
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                var charset = UTF8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                if (contentLength != 0L && contentType != null && contentType.subtype != null && contentType.subtype !== "vnd.android.package-archive") {
                    if (!isPlaintext(buffer)) {
                        logger.log("")
                        logger.log("<-- END HTTP (binary " + buffer.size + "-byte body omitted)")
                        return response
                    }
                    val buffer1 = buffer.clone()
                    var bodyStr = buffer1.readString(charset!!)
                    //                    buffer.close();
                    if (bodyParsing != null) bodyStr = bodyParsing!!.bodyResult(bodyStr)
                    if (OhHttpClient.init
                            .isJsonFromMat && bodyParsing == null && (bodyStr.startsWith("{") || bodyStr.startsWith(
                            "["))
                    ) {
                        if (isPrintResult) {
                            logger.log("FormatJsonIng-->")
                            logger.log(bodyStr)
                            logger.log("<--FormatJsonIng")
                        }
                        try {
                            logger.log("""
    
    ${AbStrUtil.formatJson(bodyStr)}
    """.trimIndent())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            logger.log(bodyStr)
                        }
                    } else {
                        logger.log("")
                        logger.log(bodyStr)
                    }
                    buffer1.close()
                }
                if (gzippedLength != null) {
                    logger.log("<-- END HTTP (" + buffer.size + "-byte, "
                            + gzippedLength + "-gzipped-byte body)")
                } else {
                    logger.log("<-- END HTTP (" + buffer.size + "-byte body)")
                }
            }
        }
        return response
    }

    private fun logHeader(headers: Headers, i: Int) {
        val value = if (headersToRedact.contains(headers.name(i))) "██" else headers.value(i)
        logger.log(headers.name(i) + ": " + value)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        var style_log = 0 //默认是0 自定义i的打印 1是okhttp的打印

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        fun isPlaintext(buffer: Buffer): Boolean {
            return try {
                val prefix = Buffer()
                val byteCount = if (buffer.size < 64) buffer.size else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                true
            } catch (e: EOFException) {
                false // Truncated UTF-8 sequence.
            }
        }

        private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
            val contentEncoding = headers["Content-Encoding"]
            return (contentEncoding != null && !contentEncoding.equals("identity",
                ignoreCase = true)
                    && !contentEncoding.equals("gzip", ignoreCase = true))
        }
    }
}