package com.wj.ktutils

import android.content.Context
import com.wj.okhttp.OhFileCallBakListener
import com.wj.okhttp.OhHttpClient
import com.wj.okhttp.OhHttpParams
import com.wj.okhttp.OhObjectListener
import com.wj.okhttp.util.DownLoad
import com.wj.util.GsonUtil
import java.io.File

/**
 * 请求DSL 扩展
 * @author Admin
 * @version 1.0
 * @date 2018/1/4
 */

class HttpRequests {
    companion object {
        var GET = "get"
        var POST = "post"
        var PUT = "put"
        var DELETE = "delete"
    }

    var url: String = ""

    var requestType: String? = null
    var ohhttpparams: OhHttpParams? = null
    var classType: Class<*>? = null
    var tag: String? = null

    var success: (Any) -> Unit = { }
    var fail: (Int, String?, Throwable?) -> Unit = { _: Int, _: String?, _: Throwable? -> }
    var finish: () -> Unit = {}
    var start: () -> Unit = {}
    var listener: OhObjectListener<Any>? = null


}

class HttpFile {
    companion object {
        var DOWN = "down"
        var UPLOAD = "upload"
    }

    var url: String = ""
    /**
     * 必须填写
     */
    var context: Context? = null
    /**
     * 方便操作下载的逻辑
     */
    var downLoad: DownLoad?=null

    var param:String="files"

    var requestType: String? = null
    var ohhttpparams: OhHttpParams? = null
    var upFile: List<File>? = null
    var success: (Any) -> Unit = { }
    var fail: (s: String, s1: String) -> Unit = { _: String, _: String? -> }
    var finish: () -> Unit = {}
    var start: () -> Unit = {}
    var error: (e: Exception) -> Unit = {}
    var progress: (l: Long, l1: Long, b: Boolean) -> Unit = { _: Long, _: Long, _: Boolean? -> }
    var listener: OhFileCallBakListener? = null
}

/**
 * 构建普通的请求
 */
fun http(init: HttpRequests.() -> Unit) {//.后面是扩展方法 传入的是无参数的方法可以简写成 http{}
    val wrap = HttpRequests()

    wrap.init()

    executeForResult(wrap)
}


/**
 * 构建下载上传的请求
 */
fun httpfile(init: HttpFile.() -> Unit) {
    val wrap = HttpFile()

    wrap.init()

    httpDU(wrap)
}

/**
 * 下载上传
 */
private fun httpDU(wrap: HttpFile) {
    if (wrap.url.isNullOrEmpty()) {
        return
    }
    val http = OhHttpClient.init

    /**
     * 默认就只能返回string 自定义返回的类型要重写这个方法
     * 重写代表 单独的回调不能使用，只能在重写方法里面调用
     */
    val httplistener: OhFileCallBakListener
    if (wrap.listener == null) {
        httplistener = object : OhFileCallBakListener() {
            override fun onStart() {
                wrap.start()
            }


            override fun onSuccess(content: String) {
                wrap.success(content)
            }

            override fun onFailure(code: String, content: String) {
                wrap.fail(code, content)
            }

            override fun onError(e: java.lang.Exception) {
                wrap.error(e)
            }

            override fun onFinish() {
                wrap.finish()
            }

            override fun onRequestProgress(l: Long, l1: Long, b: Boolean) {
                wrap.progress(l, l1, b)
            }
        }
    } else {
        httplistener = wrap.listener!!
    }

    when (wrap.requestType) {
        HttpFile.DOWN -> wrap.downLoad=http.downFile(wrap.context!!, wrap.url, httplistener)
        HttpFile.UPLOAD -> http.upFiles(wrap.url,wrap.param, wrap.ohhttpparams, wrap.upFile!!, httplistener)
    }
}


/**
 * 执行请求 支持4中类型
 */
private fun executeForResult(wrap: HttpRequests) {
    if (wrap.url.isEmpty()) {
        return
    }
    val http = OhHttpClient.init
    /**
     * 默认就只能返回string 自定义返回的类型要重写这个方法
     * 重写代表 单独的回调不能使用，只能在重写方法里面调用
     */
    val httpListener=if (wrap.listener == null)
           object : OhObjectListener<String>(){
               override fun onSuccess(p0: String) {
                   if (wrap.classType == null) wrap.success(p0) else try {
                       wrap.success(GsonUtil.gson!!.fromJson(p0, wrap.classType))
                   }catch (e:java.lang.Exception){
                       println("error: $p0")
                   }
               }

               override fun onFailure(p0: Int, p1: String?, p2: Throwable?) {
                   wrap.fail(p0, p1, p2)
               }

               override fun onFinish() {
                   wrap.finish()
               }

               override fun onStart() {
                   wrap.start()
               }
            }
        else wrap.listener!!

    when (wrap.requestType) {
        HttpRequests.GET -> http.get(wrap.url,wrap.tag, httpListener as OhObjectListener<Any>)
        HttpRequests.POST -> http.post(wrap.url, wrap.ohhttpparams,wrap.tag,
            httpListener as OhObjectListener<Any>)
        HttpRequests.PUT -> http.put(wrap.url, wrap.ohhttpparams,wrap.tag,
            httpListener as OhObjectListener<Any>)
        HttpRequests.DELETE -> http.delete(wrap.url, wrap.ohhttpparams,wrap.tag, httpListener as OhObjectListener<Any>)
    }
}
