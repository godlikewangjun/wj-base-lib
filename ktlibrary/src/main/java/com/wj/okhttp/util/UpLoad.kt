package com.wj.okhttp.util

import android.content.Context
import java.lang.Exception
import java.io.IOException
import com.wj.util.AbLogUtil
import kotlin.Throws
import com.wj.okhttp.OhHttpClient
import java.io.File
import com.wj.okhttp.OhFileCallBakListener
import java.io.RandomAccessFile
import java.io.FileInputStream
import java.io.FileNotFoundException
import com.wj.okhttp.OhProgressListener
import com.wj.okhttp.OhHttpParams
import java.lang.Runnable
import android.os.Handler
import android.os.Looper

/**
 * 断点上传
 *
 * @author Admin
 * @version 1.0
 * @date 2017/7/5
 */
class UpLoad {
    private var handlerT: Handler? = Handler(Looper.getMainLooper())
    private var upLoadRunnable: UpLoadRunnable? = null
    private var isPause = false
    private var ohProgressListener: OhProgressListener? = null
    private var context: Context? = null
    private val maxSize = 1024 * 100

    /**
     * 分段上传
     *
     * @param url
     * @param requestParams
     * @param file
     * @param lastLength
     * @param callbackListener
     */
    fun upload(
        context: Context?, url: String, file: File, lastLength: Long,
        ohProgressListener: OhProgressListener?
    ): UpLoad {
        if (upLoadRunnable != null) {
            handlerT!!.removeCallbacks(upLoadRunnable!!)
        }
        isPause = false
        this.context = context
        this.ohProgressListener = ohProgressListener
        upLoadRunnable = UpLoadRunnable(file, null, url)
        handlerT!!.post(upLoadRunnable!!)
        return this
    }

    /**
     * 分段上传
     *
     * @param url
     */
    fun upload(
        context: Context?,
        url: String,
        file: File,
        ohHttpParams: OhHttpParams?,
        ohProgressListener: OhProgressListener?
    ): UpLoad {
        if (upLoadRunnable != null) {
            handlerT!!.removeCallbacks(upLoadRunnable!!)
        }
        isPause = false
        this.context = context
        this.ohProgressListener = ohProgressListener
        upLoadRunnable = UpLoadRunnable(file, ohHttpParams, url)
        handlerT!!.post(upLoadRunnable!!)
        return this
    }

    /**
     * 暂停取消请求
     */
    fun onPause() {
        if (handlerT != null && upLoadRunnable != null) {
            isPause = true
            handlerT!!.removeCallbacks(upLoadRunnable!!)
        }
    }

    /**
     * 重新开始
     */
    fun onReStart(): Boolean {
        if (handlerT != null && upLoadRunnable != null && !upLoadRunnable!!.isFinish) {
            isPause = false
            handlerT!!.post(upLoadRunnable!!)
            return true
        }
        return false
    }

    /**
     * 销毁
     */
    fun onDestroy() {
        if (handlerT != null && upLoadRunnable != null) {
            isPause = true
            handlerT!!.removeCallbacks(upLoadRunnable!!)
            handlerT = null
            upLoadRunnable = null
        }
    }

    /**
     * 线程执行类
     */
    internal inner class UpLoadRunnable(file: File, requestParams: OhHttpParams?, url: String) :
        Runnable {
        var lastSize: Long = 0
        var fileInputStream: FileInputStream? = null
        var canch = ByteArray(maxSize)
        var total: Long
        var remaining: Long = 0
        var requestParams: OhHttpParams?
        var url: String
        var file: File
        var accessFile: RandomAccessFile? = null
        var sourceid = ""
        var isFinish = false

        init {
            total = file.length()
            this.requestParams = requestParams
            this.url = url
            this.file = file
            try {
                stream()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /**
         * 读取流
         */
        @Throws(IOException::class)
        private fun stream() {
            if (fileInputStream != null) {
                remaining = fileInputStream!!.available().toLong()
            }
            if (accessFile == null || remaining > maxSize) {
                try {
                    if (accessFile == null) {
                        accessFile = RandomAccessFile(file, "r")
                    }
                    accessFile!!.seek(lastSize) // 仅上传未完成的文件内容
                    fileInputStream = FileInputStream(accessFile!!.fd)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                accessFile!!.seek(total - total % maxSize) // 仅上传未完成的剩余文件内容
                fileInputStream = FileInputStream(accessFile!!.fd)
                canch = ByteArray((total % maxSize).toInt())
                isFinish = true
            }
        }

        override fun run() {
            try {
                stream()
                AbLogUtil.d(UpLoad::class.java, "正在上传：" + url + "; 地址：" + file.absolutePath)
                fileInputStream!!.read(canch)
                OhHttpClient.init
                    .upFile(url, "files", requestParams, file, object : OhFileCallBakListener() {
                        override fun onSuccess(content: String) {
                            if (lastSize + maxSize <= total) {
                                lastSize += maxSize.toLong()
                            } else {
                                lastSize = total
                            }
                            ohProgressListener!!.onRequestProgress(lastSize,
                                total,
                                total == lastSize)
                            if (!isPause && !isFinish) {
                                handlerT!!.postDelayed(upLoadRunnable!!, 200)
                            }
                        }

                        override fun onFailure(code: String, content: String) {
                        }

                        override fun onError(e: Exception) {
                        }

                        override fun onFinish() {}
                        override fun onRequestProgress(
                            bytesWritten: Long,
                            contentLength: Long,
                            done: Boolean
                        ) {
                        }
                    })
            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}