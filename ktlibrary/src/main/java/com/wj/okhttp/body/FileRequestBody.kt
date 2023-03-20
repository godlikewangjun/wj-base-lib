package com.wj.okhttp.body

import java.lang.Exception
import java.io.IOException
import kotlin.Throws
import okhttp3.RequestBody
import okhttp3.MediaType
import java.io.File
import com.wj.okhttp.OhFileCallBakListener
import okhttp3.internal.closeQuietly
import java.io.InputStream
import java.io.RandomAccessFile
import java.io.FileInputStream
import java.io.FileNotFoundException
import okio.*

/**
 * 上传下载的进度监听
 *
 * @author Admin
 * @version 1.0
 * @date 2017/7/4
 */
class FileRequestBody(
    private val contentType: MediaType?,
    file: File,
    lastSize: Long,
    private val ohFileCallBakListener: OhFileCallBakListener
) : RequestBody() {
    private var fileInputStream: InputStream? = null
    private var lastSize: Long = 0
    private var totalLength: Long = 0
    var isPause = false
    private val maxSize: Long = 10

    /**
     * 构造传入必要的参数 回传
     *
     * @param contentType
     * @param file
     * @param lastSize              最后续传的地方
     * @param ohFileCallBakListener
     */
    init {
        totalLength = file.length()
        this.lastSize = lastSize
        if (lastSize > 0) {
            try {
                val accessFile = RandomAccessFile(file, "r")
                accessFile.seek(lastSize) // 仅上传未完成的文件内容
                fileInputStream = FileInputStream(accessFile.fd)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                fileInputStream = FileInputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        try {
            return fileInputStream!!.available().toLong()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return lastSize
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        var source: Source? = null
        try {
            source = fileInputStream!!.source()
            val buf = Buffer()
            var remaining: Long = 0
            //            sink.writeAll(source);
            while (source.read(sink.buffer(), maxSize).also { remaining = it } != -1L) {
                if (isPause || remaining < 1) {
                    break
                }
                lastSize += remaining
                ohFileCallBakListener.sendProgressMessage(lastSize,
                    totalLength,
                    totalLength == lastSize)
            }
            sink.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            source!!.closeQuietly()
        }
    }
}