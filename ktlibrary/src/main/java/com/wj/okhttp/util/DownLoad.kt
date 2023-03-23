package com.wj.okhttp.util

import android.content.Context
import com.wj.okhttp.OhCallBackListener
import com.wj.okhttp.OhHttpClient
import org.json.JSONObject
import org.json.JSONException
import java.lang.Exception
import com.wj.okhttp.db.SQLTools
import okhttp3.Response
import com.wj.util.AbLogUtil
import java.io.File
import com.wj.util.Tools
import com.wj.util.AbFileUtil
import java.util.ArrayList
import okio.*

/**
 * @author Admin
 * @version 1.0
 * @date 2017/7/6
 */
class DownLoad(private val context: Context) {
    private var isPause = false
    private var len: Long = 0
    private var sum = 0
    private var id: String? = null
    private var dir: String? = null

    /**
     * 保存文件
     */
    fun saveFile(
        response: Response, callbackListener: OhCallBackListener<out Any>, destFileDir: String,
        destFileName: String
    ) {
        var bufferedSink: BufferedSink? = null
        var source: BufferedSource? = null
        var buffer: Buffer? = null
        dir = destFileDir
        //打印成功返回的日志
        try {
            val url = response.request.url.toString()
            id = Tools.setMD5(url)
            source = response.body!!.source()
            var total = response.body!!.contentLength()
            AbLogUtil.i(
                OhHttpClient::class.java, """${response.request.url},
 下载文件地址：$destFileDir/$destFileName;大小:$total""")
            if (!SQLTools.Companion.init(context)!!.selectDownLoad(id).has("id")) {
                SQLTools.Companion.init(context)!!.saveDownloadInfo(id, total.toString() + "")
            }
            if (total == 0L) {
                AbFileUtil.deleteFile(File(destFileDir, ".temp"))
            }
            val jsonObject: JSONObject = SQLTools.Companion.init(context)!!.selectDownLoad(id)
            if (jsonObject != null && jsonObject.has("id")) {
                try {
                    total = jsonObject.getLong("totallength")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val dir = File(destFileDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, destFileName)
            if (!file.exists()) {
                file.createNewFile()
                bufferedSink = file.sink().buffer()
            } else {
                sum += file.length().toInt()
                bufferedSink = file.appendingSink().buffer()
            }
            buffer = bufferedSink.buffer()
            val list: ArrayList<String?> = OhHttpClient.init.destroyUrls
            while (!isPause) {
                if (isPause) {
                    break
                }
                len = source.read(buffer, bufferSize.toLong()).toInt().toLong()
                if (len == -1L) {
                    break
                }
                for (i in list.size - 1 downTo -1 + 1) {
                    if (list[i] == url) {
                        list.remove(list[i])
                        AbLogUtil.i(OhHttpClient::class.java, "$url,销毁了")
                        break
                    }
                }
                bufferedSink.emit()
                sum += len.toInt()
                if (file.length() >= total) {
                    bufferedSink.flush()
                    val newFile =
                        File(dir, destFileName.replace(".temp", "." + AbFileUtil.getFileType(file)))
                    file.renameTo(newFile) //重新命名
                    SQLTools.init(context)!!.delDownLoad(id)
                    SQLTools.init(context)!!.onDestroy()
                    callbackListener.sendSuccessMessage(newFile.absolutePath)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callbackListener.sendFailureMessage(405, "write apk erro", e)
        } finally {
            if (bufferedSink != null) {
                try {
//                    bufferedSink.flush();
                    buffer!!.clear()
                    source!!.timeout().clearDeadline()
                    bufferedSink.timeout().clearDeadline()
                    buffer.close()
                    source.close()
                    bufferedSink.close()
                    callbackListener.sendFinishMessage()
                    System.gc()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 取消
     */
    fun cancle() {
        isPause = true
    }

    /**
     * 取消
     */
    fun stop() {
        isPause = true
        AbFileUtil.deleteFile(File(dir, "$id.temp"))
    }

    companion object {
        //下载速度
        var bufferSize = 200 * 1024
    }
}