package com.wj.okhttp.body

import java.io.IOException
import kotlin.Throws
import okhttp3.MediaType
import okhttp3.ResponseBody
import com.wj.okhttp.OhFileCallBakListener
import okio.*

/**
 * 下载任务进度
 * @author Admin
 * @version 1.0
 * @date 2017/4/19
 */
class ProgressResponseBody
/**
 * 构造函数，赋值
 * @param responseBody 待包装的响应体
 * @param progressListener 回调接口
 */(
//实际的待包装响应体
    private val responseBody: ResponseBody, //进度回调接口
    private val progressListener: OhFileCallBakListener
) : ResponseBody() {
    //包装完成的BufferedSource
    private var bufferedSource: BufferedSource? = null

    /**
     * 重写调用实际的响应体的contentType
     * @return MediaType
     */
    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    /**
     * 重写调用实际的响应体的contentLength
     * @return contentLength
     * @throws IOException 异常
     */
    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    /**
     * 重写进行包装source
     * @return BufferedSource
     * @throws IOException 异常
     */
    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            //包装
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    /**
     * 读取，回调进度接口
     * @param source Source
     * @return Source
     */
    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            //当前读取字节数
            var totalBytesRead = 0L
            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                //回调，如果contentLength()不知道长度，会返回-1
                progressListener.onRequestProgress(totalBytesRead,
                    responseBody.contentLength(),
                    bytesRead == -1L)
                return bytesRead
            }
        }
    }
}