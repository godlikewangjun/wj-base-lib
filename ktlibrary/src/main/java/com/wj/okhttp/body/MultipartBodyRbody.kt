package com.wj.okhttp.body

import java.io.IOException
import kotlin.Throws
import okhttp3.RequestBody
import okhttp3.MediaType
import com.wj.okhttp.OhFileCallBakListener
import okio.*

/**
 * 多文件上传监听
 *
 * @author Admin
 * @version 1.0
 * @date 2018/1/26
 */
class MultipartBodyRbody(
    private val requestBody: RequestBody,
    private val ohFileCallBakListener: OhFileCallBakListener
) : RequestBody() {
    private var bufferedSink: BufferedSink? = null
    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        if (bufferedSink == null) {
            bufferedSink = sink(sink).buffer()
        }
        requestBody.writeTo(bufferedSink!!)
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink!!.flush()
    }

    private fun sink(sink: Sink): Sink {
        return object : ForwardingSink(sink) {
            private var current: Long = 0
            private var total: Long = 0
            private var last: Long = 0
            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (total == 0L) {
                    total = contentLength()
                }
                current += byteCount
                val now = current
                if (last < now) {
                    ohFileCallBakListener.onRequestProgress(current, total, total == current)
                    last = now
                }
            }
        }
    }
}