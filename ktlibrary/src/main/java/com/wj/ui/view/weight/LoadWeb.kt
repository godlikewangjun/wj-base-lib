package com.wj.ui.view.weight

import android.widget.RelativeLayout
import android.app.ProgressDialog
import android.content.Context
import android.util.AttributeSet
import android.annotation.SuppressLint
import android.app.Activity
import com.wj.eventbus.WjEventBus
import android.annotation.TargetApi
import android.app.AlertDialog
import android.net.http.SslError
import android.webkit.*
import com.wj.okhttp.OhFileCallBakListener
import com.wj.okhttp.OhHttpClient
import java.lang.Exception

/**
 * 自定义webview
 *
 * @author 王军
 * @date 2015年1月19日
 * @版本 1.0
 */
class LoadWeb : RelativeLayout, DownloadListener {
    var url : String? = null //加载的地址
    var mWebView : WebView? = null //网页

    //    private Map<String, String> extraHeaders;//请求头;
    private var alertDialog: ProgressDialog? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, url: String?) : super(context) {
        this.url = url
        init()
    }

    /*初始化*/
    private fun init() {
        mWebView = WebView(context.applicationContext)
        mWebView!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT)
        //默认的请求头
        loading()
        webSetting()
    }

    /**
     * 重新加载
     */
    fun reload() {
        mWebView!!.reload()
    }

    /**
     * 设置没有加载完成时的界面
     */
    private fun loading() {
//        mWebView.setVisibility(GONE);//隐藏
    }

    /**
     * web设置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun webSetting() {
        addView(mWebView)
        val setting = mWebView!!.settings
        //加速===========
        // add java script interface
        // note:if api level lower than 17(android 4.2), addJavascriptInterface has security
        // issue, please use x5 or see https://developer.android.com/reference/android/webkit/
        // WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)

        // init webview settings
        setting.allowContentAccess = true
        setting.databaseEnabled = true
        setting.savePassword = false
        setting.saveFormData = false
        //==============
        setting.javaScriptEnabled = true // 设置支持js
        mWebView!!.webViewClient = client
        mWebView!!.webChromeClient = chromeClient
        setting.useWideViewPort = true
        setting.loadWithOverviewMode = true
        //设置支持 本地存储
        setting.domStorageEnabled = true
        setting.cacheMode = WebSettings.LOAD_DEFAULT
        setting.allowFileAccess = true
        setting.setGeolocationEnabled(true) //允许地理位置可用
        mWebView!!.setDownloadListener(this) //设置下载监听

        //提供方法
        defaultSetting()
    }

    @SuppressLint("AddJavascriptInterface")
    private fun defaultSetting() {
        mWebView!!.addJavascriptInterface(object : Any() {
            /**错误重新加载 */
            @JavascriptInterface
            fun error() {
                (context as Activity).runOnUiThread { mWebView!!.loadUrl(url!!) }
            }
        }, "javaop")
    }

    //**Chrome*//*
    var chromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, progress: Int) {}

        //处理定位权限
        override fun onGeolocationPermissionsShowPrompt(
            origin: String,
            callback: GeolocationPermissions.Callback
        ) {
            val remember = true
            val builder = AlertDialog.Builder(context)
            builder.setTitle("位置信息")
            builder.setMessage(origin + "允许获取您的地理位置信息吗？").setCancelable(true)
                .setPositiveButton("允许") { dialog, which ->
                    callback.invoke(origin,
                        true,
                        remember)
                }
                .setNegativeButton("不允许") { dialog, which ->
                    callback.invoke(origin,
                        false,
                        remember)
                }
            val alert = builder.create()
            alert.show()
        }
    }

    //**client*//*
    var client: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            WjEventBus.getInit().post(LOADFINSH, 0)
        }

        @TargetApi(21)
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return shouldInterceptRequest(view, request.url.toString())
        }

        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            return null
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
            WjEventBus.getInit().post(LOADERROE, "")
        }

        override fun onReceivedHttpAuthRequest(
            view: WebView,
            handler: HttpAuthHandler,
            host: String,
            realm: String
        ) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
            WjEventBus.getInit().post(LOADERROE, "")
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            val builder = AlertDialog.Builder(view.context)
            builder.setMessage("SSL认证失败，是否继续访问？")
            builder.setPositiveButton("确定") { dialog, which -> handler.proceed() }
            builder.setNegativeButton("取消") { dialog, which -> handler.cancel() }
            val dialog = builder.create()
            dialog.show()
        }
    }

    //**设置提供web调用的事件*//*
    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    fun addWebEvent(`object`: Any?) {
        mWebView!!.addJavascriptInterface(`object`!!, "javaop")
    }

    /**
     * 重新加载url
     *
     * @param url
     */
    fun setLoadUrl(url: String) {
        var url = url
        this.url = url
        if (url.indexOf("http") < 0) {
            url = "file:///android_asset/$url"
        } else if (url.indexOf("http") == 0 || url.indexOf("https") == 0) {
            // webview is ready now, just tell session client to bind
            mWebView!!.loadUrl(url)
        }
    }

    /**
     * 加载html
     *
     * @param html
     */
    fun loadHtml(html: String?) {
        mWebView!!.loadData(html!!, "text/html; charset=UTF-8", null) //这种写法可以正确解码
    }

    /**
     * 设置js调用java的接口
     */
    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    fun setJavaToJs(obj: Any?, interfaceName: String?) {
        mWebView!!.addJavascriptInterface(obj!!, interfaceName!!)
    }

    override fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long
    ) {
        if (alertDialog == null) {
            alertDialog = ProgressDialog(context)
            alertDialog!!.setMessage("正在下载$contentDisposition")
            alertDialog!!.setCanceledOnTouchOutside(false)
            alertDialog!!.show()
            OhHttpClient.init.downFile(context, url, object : OhFileCallBakListener() {

                override fun onSuccess(content: String) {
                }

                override fun onFailure(code: String, content: String) {
                    alertDialog!!.cancel()
                }

                override fun onError(e: Exception) {
                    alertDialog!!.cancel()
                }

                override fun onFinish() {
                    alertDialog!!.cancel()
                }

                override fun onRequestProgress(
                    bytesWritten: Long,
                    contentLength: Long,
                    done: Boolean
                ) {
                    alertDialog!!.setMessage("正在下载" + (bytesWritten / contentLength).toInt() + "%")
                }
            })
        }
    }

    companion object {
        var LOADERROE = "webLoadError"
        var LOADFINSH = "webLoadFinsh"
    }
}