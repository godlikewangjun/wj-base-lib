package com.wj.okhttp

import okhttp3.Credentials.basic
import android.content.Context
import org.json.JSONObject
import org.json.JSONException
import java.lang.Exception
import com.wj.okhttp.db.SQLTools
import java.io.IOException
import com.wj.okhttp.log.HttpLoggingInterceptor
import com.wj.util.AbLogUtil
import kotlin.Throws
import java.nio.charset.Charset
import java.io.UnsupportedEncodingException
import java.io.File
import com.wj.util.Tools
import com.wj.util.AbFileUtil
import java.util.ArrayList
import com.wj.okhttp.util.DownLoad
import com.wj.okhttp.cookies.PersistentCookieStore
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import com.wj.okhttp.Interceptor.GzipRequestInterceptor
import com.wj.okhttp.body.MultipartBodyRbody
import com.wj.okhttp.body.FileRequestBody
import com.wj.okhttp.Interceptor.DownInterceptor
import com.wj.eventbus.WjEventBus
import com.wj.util.GsonUtil
import java.util.Objects
import android.annotation.SuppressLint
import android.os.Message
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.jvm.Synchronized
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.*
import java.net.*
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 * okhttp封装类的客户端
 *
 * @author wangjun
 * @version 2.0
 * @date 2016年2月1日
 */
class OhHttpClient {
    /**
     * 是否格式化请求成功的json字符串
     */
    var isJsonFromMat = true
        private set

    /**
     * 限制上传下载的速度即是时间间隔
     */
    var downUpTime = -1
    /**
     * 获取销毁的urls 存量 最大缓存10条
     *
     * @return 销毁的请求url集合
     */
    /**
     * 销毁的请求url集合 最大缓存10条
     */
    val destroyUrls = ArrayList<String?>()
    var client: OkHttpClient? = null
    private var cookieStore //cookies
            : PersistentCookieStore? = null
    var logging: HttpLoggingInterceptor? = HttpLoggingInterceptor() //打印日志
        private set
    private val UTF8 = Charset.forName("UTF-8")

    fun setLogging(logging: HttpLoggingInterceptor?): OhHttpClient {
        this.logging = logging
        return this
    }

    /**
     * 设置格式化请求成功的json
     */
    fun setJsonFromMat(jsonFromMat: Boolean): OhHttpClient? {
        isJsonFromMat = jsonFromMat
        return ohHttpClient
    }

    /**
     * 关闭自己的日志
     */
    fun closeLog() {
        AbLogUtil.closeLog()
    }

    /**
     * 添加头
     */
    fun setHeaders(mHeaders: Headers?): OhHttpClient? {
        headers = mHeaders
        return ohHttpClient
    }


    /**
     * 设置Cookies
     */
    fun setCookies(context: Context): OhHttpClient? {
        if (cookieStore == null) {
            cookieStore = PersistentCookieStore(context)
        }
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        client!!.newBuilder().cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                if (cookies.isNotEmpty()) {
                    for (item in cookies) {
                        cookieStore!!.add(url, item)
                    }
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore!![url]
            }
        }).build()
        return ohHttpClient
    }

    /**
     * 设置读取缓存的头
     */
    fun setCacheHeader(context: Context): OhHttpClient? {
        if (headers != null) {
            headers!!.newBuilder().add("Cache-Control", "max-stale=10")
        } else {
            headers = Headers.Builder().add("Cache-Control", "max-stale=10").build()
        }
        val file: File = if (CACHEPATH == null) {
            context.cacheDir
        } else {
            File(CACHEPATH!!)
        }
        val cache = Cache(file, cacheSize.toLong())
        client!!.newBuilder().cache(cache).build()
        return ohHttpClient
    }

    /**
     * 销毁请求的url
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun destroyUrl(url: String?) {
        if (destroyUrls.size > 10) {
            destroyUrls.removeAt(0)
        }
        for (i in destroyUrls.indices) {
            if (destroyUrls[i] == url) {
                destroyUrls.removeAt(i)
                break
            }
        }
        destroyUrls.add(url)
        if (client != null && url != null) {
//            if(queue==null){
//                queue=new LinkedList<String>();
//            }
//            queue.offer(url);
            GlobalScope.launch(Dispatchers.IO) {
                synchronized (client!!.dispatcher.javaClass) {
                    for (call in  client!!.dispatcher.queuedCalls()){
                        if (url == call.request().tag()) {
                            call.cancel()
                        }
                    }
                    for (call in  client!!.dispatcher.runningCalls()){
                        if (url == call.request().tag()) {
                            call.cancel()
                        }
                    }
                }
            }
        }
    }

    /**
     * 销毁请求的url
     */
    fun isHaveUrl(url: String?): Boolean {
        if (client != null && url != null) {
            synchronized(client!!.dispatcher.javaClass) {
                for (call in client!!.dispatcher.queuedCalls()) {
                    if (url == call.request().tag()) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 销毁所有url
     */
    fun destroyAll() {
        if (client != null) {
            client!!.dispatcher.cancelAll()
        }
        ohHttpClient = null
    }

    /**
     * 配置http
     */
    private fun setOkHttpClient() {
        val builder = client!!.newBuilder()
        builder.connectTimeout(CONNECTTIMEOUT.toLong(), TimeUnit.SECONDS)
        builder.writeTimeout(WRITETIMEOUT.toLong(), TimeUnit.SECONDS)
        builder.readTimeout(READTIMEOUT.toLong(), TimeUnit.SECONDS)
        builder.retryOnConnectionFailure(true) //错误重连
        val protocols: MutableList<Protocol> = ArrayList()
        protocols.add(Protocol.HTTP_1_1) // 这里如果，只指定h2的话会抛异常
        protocols.add(Protocol.HTTP_2) // 这里如果，只指定h2的话会抛异常
        try {
            builder.sslSocketFactory(TLSSocketFactory(), trustManager)
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        builder.protocols(protocols).hostnameVerifier(HostnameVerifier { hostname, session ->
            // 放过host验证
            true
        })
        client = builder.build()
    }

    /**
     * 需要手动开起打印日志防止，其他公共头有问题
     */
    fun setLogcat() {
        val builder = client!!.newBuilder()
        //默认就设置日志打印
        if (logging == null) {
            logging = HttpLoggingInterceptor()
        }
        logging!!.level = HttpLoggingInterceptor.Level.BODY
        builder.addNetworkInterceptor(logging!!)
        client = builder.build()
    }

    /**
     * 设置是否gzip压缩请求body
     */
    fun setGzip(isGzip: Boolean) {
        if (isGzip) {
            client = client!!.newBuilder().addInterceptor(GzipRequestInterceptor()).build() // 加入拦截器
        } else {
            (client!!.interceptors as ArrayList).remove(GzipRequestInterceptor())
        }
    }

    /**
     * 自定义tag
     * get请求
     */
    operator fun get(url: String, tag: String?, callbackListener: OhObjectListener<out Any>) {
        haveNoBody(url, callbackListener, 0, tag)
    }

    /**
     * 自定义tag
     * post请求
     */
    fun post(url: String, tag: String?, callbackListener: OhObjectListener<out Any>) {
        haveNoBody(url, callbackListener, 2, tag)
    }

    /**
     * 自定义tag
     * delete请求
     */
    fun delete(url: String, tag: String?, callbackListener: OhCallBackListener<out Any>) {
        haveNoBody(url, callbackListener, 1, tag)
    }

    /**
     * get请求
     */
    fun get(url: String, callbackListener: OhObjectListener<out Any>) {
        haveNoBody(url, callbackListener, 0, null)
    }

    /**
     * post请求
     */
    fun post(url: String, callbackListener: OhObjectListener<out Any>) {
        haveNoBody(url, callbackListener, 2, null)
    }

    /**
     * delete请求
     */
    fun delete(url: String, callbackListener: OhCallBackListener<out Any>) {
        haveNoBody(url, callbackListener, 1, null)
    }

    /**
     * 不携带参数的返回string的统一方法 0 是get 1是delete
     */
    private fun haveNoBody(
        url: String,
        callbackListener: OhCallBackListener<out Any>,
        type: Int,
        tag: String?
    ) {
        val builder = Request.Builder().url(url).tag(tag)
        when (type) {
            0 -> builder.get()
            1 -> builder.delete()
        }
        headers?.let {   builder.headers(it) }
        val request: Request = builder.build()
        client!!.newCall(request).enqueue(OKHttpCallBack(request, callbackListener))
    }

    /**
     * 自定义tag
     * post请求
     */
    fun post(
        url: String, requestParams: OhHttpParams?, tag: String?,
        callbackListener: OhObjectListener<out Any>
    ) {
        haveBody(url, requestParams, callbackListener, 0, tag)
    }

    /**
     * 自定义tag
     * put请求
     */
    fun put(
        url: String, requestParams: OhHttpParams?, tag: String?,
        callbackListener: OhObjectListener<out Any>
    ) {
        haveBody(url, requestParams, callbackListener, 1, tag)
    }

    /**
     * 自定义tag
     * patch请求
     */
    fun patch(
        url: String, requestParams: OhHttpParams?, tag: String?,
        callbackListener: OhObjectListener<out Any>
    ) {
        haveBody(url, requestParams, callbackListener, 2, tag)
    }

    /**
     * 自定义tag
     * dedelete请求
     */
    fun delete(
        url: String, requestParams: OhHttpParams?, tag: String?,
        callbackListener: OhCallBackListener<out Any>
    ) {
        haveBody(url, requestParams, callbackListener, 3, tag)
    }

    /**
     * post请求
     */
    fun post(
        url: String, requestParams: OhHttpParams?,
        callbackListener: OhObjectListener<out Any>
    ) {
        haveBody(url, requestParams, callbackListener, 0, null)
    }

    /**
     * put请求
     */
    fun put(
        url: String, requestParams: OhHttpParams?,
        callbackListener: OhObjectListener<out Any>
    ) {
        haveBody(url, requestParams, callbackListener, 1, null)
    }

    /**
     * patch请求
     */
    fun patch(
        url: String, requestParams: OhHttpParams?,
        callbackListener: OhObjectListener<out Any>
    ) {
        haveBody(url, requestParams, callbackListener, 2, null)
    }

    /**
     * delete请求
     */
    fun delete(
        url: String, requestParams: OhHttpParams?,
        callbackListener: OhCallBackListener<out Any>
    ) {
        haveBody(url, requestParams, callbackListener, 3, null)
    }

    /**
     * 携带参数的返回string的统一方法 0 是post 1是put 2是patch 3是delete
     */
    private fun haveBody(
        url: String, requestParams: OhHttpParams?,
        callbackListener: OhCallBackListener<out Any>, type: Int, tag: String?
    ) {
        val body: RequestBody = if (requestParams!!.keys.contains(JSONTYE)) {
            (requestParams[JSONTYE] as String).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        } else {
            val requestBody = FormBody.Builder()
            val keys = requestParams.keys
            for (i in keys.indices) {
                requestBody.add(keys[i], requestParams[keys[i]].toString())
            }
            requestBody.build()
        }
        val builder = Request.Builder().url(url)
        if (tag == null) builder.tag(tag) else builder.tag(url) // 设置tag
        when (type) {
            0 -> builder.post(body)
            1 -> builder.put(body)
            2 -> builder.patch(body)
            3 -> builder.delete(body)
        }
        headers?.let { builder.headers(headers!!) }
        val request: Request = builder.build()
        client!!.newCall(request).enqueue(OKHttpCallBack(request, callbackListener))
    }

    /**
     * 上传文件
     * 模拟表单提交，因为头不能有中文,所有文件名要进行编码
     */
    fun upFile(
        url: String?, param: String?, requestParams: OhHttpParams?, file: File,
        callbackListener: OhFileCallBakListener
    ) {
        // 写入文件流 用于单个文件上传
        // text/x-markdown; charset=utf-8
        val requestBody = file.asRequestBody("image/png".toMediaTypeOrNull())

        // 调用多个文件上传的body 封装进入
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(param!!, getFileEndCode(file.name), requestBody)
        // 添加post参数
        if (requestParams != null) {
            var key: String
            val iterator: Iterator<String?> = requestParams.params.keys.iterator()
            while (iterator.hasNext()) {
                key = iterator.next()!!
                multipartBody.addFormDataPart(key, requestParams[key].toString())
            }
        }
        // 封装请求
        val builder = Request.Builder().url(url!!).tag(url) // 设置tag
        headers?.let { builder.headers(it) }
        builder.post(multipartBody.build())
        val request: Request = builder.build()
        callbackListener.ohType = 0 // 设置类型是上传
        client!!.newCall(request).enqueue(OKHttpCallBack(request, callbackListener))
    }

    /**
     * 上传文件
     * 模拟表单提交，因为头不能有中文,所有文件名要进行编码
     */
    fun upFiles(
        url: String?, param: String?, requestParams: OhHttpParams?, files: List<File>,
        callbackListener: OhFileCallBakListener
    ) {
        // 调用多个文件上传的body 封装进入
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        // 添加多个文件
        for (i in files.indices) {
            multipartBody.addFormDataPart(param!!,
                getFileEndCode(files[i].name),
                files[i].asRequestBody("application/octet-stream".toMediaTypeOrNull()))
        }
        // 添加post参数
        if (requestParams != null) {
            var key: String
            val iterator: Iterator<String?> = requestParams.params.keys.iterator()
            while (iterator.hasNext()) {
                key = iterator.next()!!
                multipartBody.addFormDataPart(key, requestParams.get(key).toString())
            }
        }
        // 封装请求
        val builder = Request.Builder().url(url!!).tag(url) // 设置tag
        headers?.let { builder.headers(it) }
        builder.post(MultipartBodyRbody(multipartBody.build(), callbackListener))
        val request: Request = builder.build()
        callbackListener.ohType = 0 // 设置类型是上传
        client!!.newCall(request).enqueue(OKHttpCallBack(request, callbackListener))
    }

    /**
     * 编码文件名，防止okHttp3在header中出现中文报错
     *
     * @param fileName
     * @return
     */
    private fun getFileEndCode(fileName: String): String {
        try {
            return URLEncoder.encode(fileName, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return Tools.setMD5(fileName)
    }

    /**
     * 上传文件流
     *
     * @param url              地址
     * @param requestParams    携带的参数
     * @param file             文件
     * @param type             默认是application/octet-stream
     * @param callbackListener 回调
     * @return
     */
    fun upFileStream(
        url: String, requestParams: OhHttpParams?, file: File, type: String?,
        callbackListener: OhFileCallBakListener
    ): FileRequestBody {
        var requestType = type
        if (requestType == null) {
            requestType = "application/octet-stream"
        }
        // 写入文件流 用于单个文件上传
        // text/x-markdown; charset=utf-8
        val requestBody = FileRequestBody(requestType.toMediaTypeOrNull(), file, 0, callbackListener)
        // 调用多个文件上传的body 封装进入
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(AbFileUtil.getFileType(file)!!, file.name, requestBody)
        // 添加post参数
        if (requestParams != null) {
            var key: String
            val iterator: Iterator<String?> = requestParams.params.keys.iterator()
            while (iterator.hasNext()) {
                key = iterator.next()!!
                multipartBody.addFormDataPart(key, requestParams[key].toString())
            }
        }
        // 封装请求
        val builder = Request.Builder().url(url).tag(url) // 设置tag
        headers?.let {  builder.headers(it) }
        builder.post(multipartBody.build())
        val request: Request = builder.build()
        callbackListener.ohType = 0 // 设置类型是上传
        client!!.newCall(request).enqueue(OKHttpCallBack(request, callbackListener))
        return requestBody
    }

    /**
     * 下载文件
     *
     * @param url
     * @param isBreakpoint     是否支持断点下载
     * @param callbackListener
     */
    fun downFile(
        context: Context,
        url: String,
        callbackListener: OhFileCallBakListener
    ): DownLoad? {
        var isLoading = false
        synchronized(client!!.dispatcher.javaClass) {
            //当前任务在下载队列就返回
            for (call in client!!.dispatcher.queuedCalls()) {
                if (url == call.request().tag()) {
                    isLoading = true
                    return null
                }
            }
            for (call in client!!.dispatcher.runningCalls()) {
                if (url == call.request().tag()) call.cancel()
            }
        }
        if (isLoading) {
            return null
        }
        val builder = Request.Builder().url(url).tag(url).get() // 设置tag
        headers?.let { builder.headers(it) }
        val id = Tools.setMD5(url)
        val file = File(DOWNDIR, "$id.temp")
        if (file.exists()) {
            var total = ""
            val jsonObject: JSONObject = SQLTools.init(context)!!.selectDownLoad(id)
            if (jsonObject != null && jsonObject.has("id")) {
                try {
                    total = jsonObject.getLong("totallength").toString() + ""
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            builder.header("range", "bytes=" + file.length() + "-" + total) //断点续传要用到的，指示下载的区间
            AbLogUtil.d(OhHttpClient::class.java,
                "bytes=" + file.length() + "-" + total + "  本地存的文件长度：" + jsonObject.toString())
        }
        val request: Request = builder.build()
        val interceptors = client!!.networkInterceptors
        for (i in interceptors.indices) {
            if (interceptors[i] is DownInterceptor) {
                break
            } else if (i == interceptors.size - 1) {
                client =
                    client!!.newBuilder().addNetworkInterceptor(DownInterceptor(callbackListener))
                        .build()
            }
        }
        //清理请求的
        destroyUrls.clear()
        val downLoad = DownLoad(context)
        callbackListener.ohType = 1 // 设置类型是下载
        client!!.newCall(request).enqueue(OKHttpCallBack(request, downLoad, callbackListener))
        return downLoad
    }

    /**
     * 统一的处理okhttp的返回结果的格式
     */
    inner class OKHttpCallBack : Callback {
        private var callbackListener: OhCallBackListener<out Any>? = null
        private var request: Request
        private var failNum = 0 // 失败次数
        var time //起始时间
                : Long = 0
        private var downLoad: DownLoad? = null

        constructor(request: Request, callbackListener: OhCallBackListener<out Any>) {
            this.request = request
            init(callbackListener)
        }

        constructor(
            request: Request,
            downLoad: DownLoad?,
            callbackListener: OhCallBackListener<out Any>
        ) {
            this.request = request
            this.downLoad = downLoad
            init(callbackListener)
        }

        fun init(callbackListener: OhCallBackListener<out Any>) {
            this.callbackListener = callbackListener
            this.callbackListener!!.handler=ResponderHandler(callbackListener)
            // 设置handler
            this.callbackListener!!.sendStartMessage() //开始
        }

        @OptIn(DelicateCoroutinesApi::class)
        override fun onFailure(call: Call, e: IOException) {
            if (SocketTimeoutException::class.java == e.cause) {
                AbLogUtil.e(OhHttpClient::class.java, request.url.toString() + ", 请检查网络,连接超时")
                //网络不好重试
                AbLogUtil.d(OhHttpClient::class.java,
                    "连接不到:" + request.url.toString() + ",重试" + failNum + "次")
                if (failNum > 2) { //失败消息为0
                    callbackListener!!.sendFailureMessage(0,
                        "连接不到:" + request.url.toString() + ",重试超过最大的次数" + failNum, null)
                    callbackListener!!.sendFinishMessage()

                    //打印错误和发送通知方便全局处理
                    e.printStackTrace()
                    GlobalScope.launch(Dispatchers.Main) {
                        WjEventBus.getInit().post(OKHTTP_TIMEOUT, 0)
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        client!!.newCall(request).enqueue(this@OKHttpCallBack)
                        failNum++
                    }
                }
            } else { //code==-1 不是超时错误
                AbLogUtil.e(OhHttpClient::class.java, request.url.toString() + "," + e.message)
                e.printStackTrace()
                callbackListener!!.sendFailureMessage(-1, e.message!!, e)
                WjEventBus.getInit().post(OKHTTP_FAILURE, e) //错误了就通知全局进行处理
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            val url = request.url.toString()
            val code = response.code
            // 存在服务器返回压缩就解压缩 okhttp自动解压就不做处理
//			if (response.header("Content-Encoding") != null && response.header("Content-Encoding").equals("gzip")) {
//				body = gunzip(body);
//			}
//            WjEventBus.getInit().post(response.headers());
            if (code in 200..299) { // 服务器响应成功 206是断点
                if (callbackListener!=null && callbackListener is OhObjectListener<out Any>) { // 请求sring的监听
                    val responseBody = response.body
                    var charset = UTF8
                    assert(responseBody != null)
                    val contentType = responseBody!!.contentType()
                    if (contentType != null) {
                        charset = contentType.charset(UTF8)
                    }
                    assert(charset != null)
                    val body = responseBody.source().readString(charset!!)
                    if (String::class.java != (callbackListener as OhObjectListener<out Any>).classname) {
                        try {
                            GsonUtil.gson.fromJson(body,
                                (callbackListener as OhObjectListener<out Any>).classname)
                                ?.let { callbackListener?.sendSuccessMessage(it) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            AbLogUtil.e(OhHttpClient::class.java,
                                (callbackListener as OhObjectListener<*>).classname.toString() + ";" + url + ",返回json格式化错误" + body)
                            if (failNum == 3) {
                                (callbackListener as OhObjectListener<*>).onFailure(400,
                                    "类格式化错误",
                                    e)
                            }
                            return
                        }
                    } else callbackListener?.sendSuccessMessage(body)
                } else if (callbackListener is OhFileCallBakListener) { // 请求文件的监听
                    if (callbackListener?.ohType == 0) { // 上传
                        val bodyError = Objects.requireNonNull(response.body!!).string()
                        AbLogUtil.i(OhHttpClient::class.java, "$url,$bodyError")
                        callbackListener?.sendSuccessMessage(bodyError)
                    } else if (callbackListener?.ohType == 1) { // 下载
                        val name = Tools.setMD5(url)
                        downLoad!!.saveFile(response, callbackListener!!, DOWNDIR, "$name.temp")
                        downLoad = null
                        return
                    }
                }
            } else if (code == 301 || code == 302) { // 重试
                AbLogUtil.d(OhHttpClient::class.java, url + ",重试" + failNum + "次")
                if (failNum > 2) {
                    callbackListener!!.sendFailureMessage(response.code,
                        request.url.toString() + ",重试超过最大的次数" + failNum, null)
                    callbackListener!!.sendFinishMessage()
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        client!!.newCall(request).enqueue(this@OKHttpCallBack)
                        failNum++
                    }
                }
            } else if (code == 401) { // 用户认证
                client!!.newBuilder().authenticator { route, response ->
                    val credential = basic("user", "password")
                    response.request.newBuilder().header("Authorization", credential).build()
                }
                val bodyError = Objects.requireNonNull(response.body!!).string()
                callbackListener!!.sendFailureMessage(code, bodyError, null)
                AbLogUtil.e(OhHttpClient::class.java, "$url,$bodyError")
            } else if (code == 404) { // 没有找到接口或页面
                val bodyError = Objects.requireNonNull(response.body!!).string()
                callbackListener!!.sendFailureMessage(code, bodyError, null)
                AbLogUtil.e(OhHttpClient::class.java, "$url,$bodyError")
            } else {
                val bodyError = Objects.requireNonNull(response.body!!).string()
                callbackListener!!.sendFailureMessage(code, bodyError, null)
                AbLogUtil.e(OhHttpClient::class.java, "$url,$bodyError")
            }
            try {
                callbackListener!!.sendFinishMessage()
                //                AbFileUtil.writeAppend("jk", "\n接口:" + request.url().toString() + ",时间:" + (System.currentTimeMillis() - time));
            } catch (e: Exception) {
                e.printStackTrace()
                AbLogUtil.i(javaClass, "$url,错误")
            }
        }
    }

    /**
     * 描述：请求返回
     */
    @SuppressLint("HandlerLeak")
    private inner class ResponderHandler
    /**
     * 响应消息处理.
     */(
        /**
         * 响应消息监听.
         */
        private val responseListener: OhCallBackListener<out Any>
    ) : OhCallBackMessageInterface {
        /**
         * 响应数据.
         */
        private var response: Array<out Any>? = null

        /**
         * 处理消息
         *
         * @param what
         */
        private fun callBack(what: Int) {
            when (what) {
                SUCCESS_MESSAGE -> if (responseListener is OhObjectListener<out Any>) { // 字符串的请求
                    (responseListener as  OhObjectListener<Any>).onSuccess(
                        response!![0])
                } else if (responseListener is OhFileCallBakListener) { // 文件
                    responseListener.onSuccess(
                        response!![0] as String)
                }
                FAILURE_MESSAGE -> if (responseListener is OhObjectListener<out Any>) { // 字符串的请求
                    responseListener .onFailure((response!![0] as Int),
                        response!![1] as String, null)
                } else if (responseListener is OhFileCallBakListener) { // 文件
                    responseListener.onFailure(response!![0].toString() + "",
                        response!![1].toString() + "")
                }
                ERROE_MESSAGE -> if (responseListener is OhObjectListener<out Any>) { // 字符串的请求
                    responseListener.onFailure(-1,
                        "报错",
                        response!![0] as Exception)
                } else if (responseListener is OhFileCallBakListener) { // 文件
                    responseListener.onError(response!![0] as Exception)
                }
                PROGRESS_MESSAGE -> if (response != null && response!!.size >= 2) {
                    (responseListener as OhFileCallBakListener).onRequestProgress((response!![0] as Long),
                        (response!![1] as Long),
                        (response!![2] as Boolean))
                } else {
                    AbLogUtil.e(OhHttpClient::class.java, "PROGRESS_MESSAGE ")
                }
                FINSH_MESSAGE -> if (responseListener is OhObjectListener<*>) { // 字符串的请求
                    responseListener.onFinish()
                } else if (responseListener is OhFileCallBakListener) { // 文件
                    responseListener.onFinish()
                }
                START_MESSAGE -> if (responseListener is OhObjectListener<*>) { // 字符串的请求
                    responseListener.onStart()
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        override fun handedMessage(msg: Message) {
            GlobalScope.launch(Dispatchers.Main) {
                response = msg.obj as Array<out Any>
                callBack(msg.what)
            }
        }
    }

    inner class TLSSocketFactory : SSLSocketFactory() {
        private val delegate: SSLSocketFactory

        init {
            val context = SSLContext.getInstance("TLS")
            context.init(null, null, null)
            delegate = context.socketFactory
        }

        override fun getDefaultCipherSuites(): Array<String> {
            return delegate.defaultCipherSuites
        }

        override fun getSupportedCipherSuites(): Array<String> {
            return delegate.supportedCipherSuites
        }

        @Throws(IOException::class)
        override fun createSocket(): Socket {
            return enableTLSOnSocket(delegate.createSocket())
        }

        @Throws(IOException::class)
        override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
            return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose))
        }

        @Throws(IOException::class, UnknownHostException::class)
        override fun createSocket(host: String, port: Int): Socket {
            return enableTLSOnSocket(delegate.createSocket(host, port))
        }

        @Throws(IOException::class, UnknownHostException::class)
        override fun createSocket(
            host: String,
            port: Int,
            localHost: InetAddress,
            localPort: Int
        ): Socket {
            return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort))
        }

        @Throws(IOException::class)
        override fun createSocket(host: InetAddress, port: Int): Socket {
            return enableTLSOnSocket(delegate.createSocket(host, port))
        }

        @Throws(IOException::class)
        override fun createSocket(
            address: InetAddress,
            port: Int,
            localAddress: InetAddress,
            localPort: Int
        ): Socket {
            return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort))
        }

        private fun enableTLSOnSocket(socket: Socket): Socket {
            if (socket != null && socket is SSLSocket) {
                socket.enabledProtocols = arrayOf("TLSv1.1", "TLSv1.2")
            }
            return socket
        }
    }

    private val trustManager: X509TrustManager = @SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls(0)
        }
    }

    /**
     * 初始化
     */
    init {
        if (client == null) {
            client = OkHttpClient()
            setOkHttpClient()
        }
    }

    companion object {
        /**
         * 超时报错
         */
        const val OKHTTP_TIMEOUT = "OKHTTP_TIMEOUT"

        /**
         * 超时报错
         */
        const val OKHTTP_FAILURE = "OKHTTP_FAILURE"

        /**
         * 成功.
         */
        const val SUCCESS_MESSAGE = 0

        /**
         * 失败.
         */
        const val FAILURE_MESSAGE = 1

        /**
         * 报错
         */
        protected const val ERROE_MESSAGE = 2

        /**
         * 进度消息
         */
        const val PROGRESS_MESSAGE = 3

        /**
         * 完成消息
         */
        const val FINSH_MESSAGE = 4

        /**
         * 开始消息
         */
        const val START_MESSAGE = 5

        /**
         * 连接超时的时间
         */
        var CONNECTTIMEOUT = 30

        /**
         * 写入的超的时间
         */
        var WRITETIMEOUT = 60

        /**
         * 读取的超的时间
         */
        var READTIMEOUT = 60

        /**
         * 缓存的路径
         */
        var CACHEPATH: String? = null

        /**
         * 缓存的大小
         */
        var cacheSize = 100 * 1024

        /**
         * 缓存的请求的失效时间
         */
        var cacheTimeOut = 5

        /**
         * 下载文件的目录
         */
        var DOWNDIR = "/storage/emulated/0/Download"
        private var ohHttpClient: OhHttpClient? = null
        private var headers: Headers? = null

        //json类型传入的参数 自动转
        var JSONTYE = "MediaTypeJson"

        /**
         * 获取实例
         */
        @get:Synchronized
        val init: OhHttpClient
            get() {
                if (ohHttpClient == null) {
                    ohHttpClient = OhHttpClient()
                }
                return ohHttpClient!!
            }

        /**
         * 销毁
         */
        @Synchronized
        fun destroy(): OhHttpClient? {
            if (ohHttpClient != null && ohHttpClient!!.client != null) {
                ohHttpClient!!.client!!.dispatcher.cancelAll()
                ohHttpClient!!.client = null
                ohHttpClient = null
            }
            return ohHttpClient
        }

        /**
         * 使用gzip进行压缩
         */
        fun gzip(primStr: String?): String? {
            if (primStr == null || primStr.isEmpty()) {
                return primStr
            }
            val out = ByteArrayOutputStream()
            var gzip: GZIPOutputStream? = null
            try {
                gzip = GZIPOutputStream(out)
                gzip.write(primStr.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (gzip != null) {
                    try {
                        gzip.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return String(out.toByteArray())
        }

        /**
         * gzip进行解压缩
         */
        fun gunzip(compressedStr: String?): String? {
            if (compressedStr == null) {
                return null
            }
            val out = ByteArrayOutputStream()
            var `in`: ByteArrayInputStream? = null
            var ginzip: GZIPInputStream? = null
            var compressed: ByteArray? = null
            var decompressed: String? = null
            try {
                compressed = compressedStr.toByteArray()
                `in` = ByteArrayInputStream(compressed)
                ginzip = GZIPInputStream(`in`)
                val buffer = ByteArray(1024)
                var offset = -1
                while (ginzip.read(buffer).also { offset = it } != -1) {
                    out.write(buffer, 0, offset)
                }
                decompressed = out.toString()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (ginzip != null) {
                    try {
                        ginzip.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                if (`in` != null) {
                    try {
                        `in`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                try {
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return decompressed
        }
    }
}