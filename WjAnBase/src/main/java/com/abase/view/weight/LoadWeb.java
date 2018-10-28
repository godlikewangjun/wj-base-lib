package com.abase.view.weight;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.abase.okhttp.OhFileCallBakListener;
import com.abase.okhttp.OhHttpClient;
import com.abase.util.Tools;
import com.abase.view.weight.web.ObservableWebView;
import com.abase.view.weight.web.SonicJavaScriptInterface;
import com.abase.view.weight.web.SonicRuntimeImpl;
import com.abase.view.weight.web.SonicSessionClientImpl;
import com.tencent.sonic.sdk.SonicCacheInterceptor;
import com.tencent.sonic.sdk.SonicConfig;
import com.tencent.sonic.sdk.SonicConstants;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicSession;
import com.tencent.sonic.sdk.SonicSessionConfig;
import com.tencent.sonic.sdk.SonicSessionConnection;
import com.tencent.sonic.sdk.SonicSessionConnectionInterceptor;
import com.wj.eventbus.WjEventBus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 自定义webview
 *
 * @author 王军
 * @date 2015年1月19日
 * @版本 1.0
 */
public class LoadWeb extends RelativeLayout implements DownloadListener {
    public String url;//加载的地址
    public ObservableWebView mWebView;//网页
    //    private Map<String, String> extraHeaders;//请求头;
    private OnClickListener onClickListener;
    private ProgressDialog alertDialog = null;
    public static String LOADERROE = "webLoadError";
    public static String LOADFINSH = "webLoadFinsh";
    private SonicSession sonicSession;
    private SonicSessionClientImpl sonicSessionClient = null;

    public void setSonicSession(SonicSession sonicSession) {
        this.sonicSession = sonicSession;
    }

    public void setSonicSessionClient(SonicSessionClientImpl sonicSessionClient) {
        this.sonicSessionClient = sonicSessionClient;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    public LoadWeb(Context context) {
        super(context);
        init();
    }

    public LoadWeb(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadWeb(Context context, String url) {
        super(context);
        this.url = url;
        init();
    }

    /*初始化*/
    private void init() {
        mWebView = new ObservableWebView(getContext().getApplicationContext());
        mWebView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        //默认的请求头

        loading();
        webSetting();
    }

    /**
     * 重新加载
     */
    public void reload(){
        if (sonicSession != null) {
            sonicSession.refresh();
        }
        mWebView.reload();
    }



    /**
     * 设置没有加载完成时的界面
     */
    private void loading() {
        mWebView.setVisibility(GONE);//隐藏
    }

    /**
     * web设置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void webSetting() {
        addView(mWebView);

        WebSettings setting = mWebView.getSettings();
        //加速===========
        // add java script interface
        // note:if api level lower than 17(android 4.2), addJavascriptInterface has security
        // issue, please use x5 or see https://developer.android.com/reference/android/webkit/
        // WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)

        // init webview settings
        setting.setAllowContentAccess(true);
        setting.setDatabaseEnabled(true);
        setting.setSavePassword(false);
        setting.setSaveFormData(false);
        //==============
        setting.setJavaScriptEnabled(true); // 设置支持js
        mWebView.setWebViewClient(client);
        mWebView.setWebChromeClient(chromeClient);
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
        //设置支持 本地存储
        setting.setDomStorageEnabled(true);
        setting.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getContext().getCacheDir().getAbsolutePath();
        setting.setAppCachePath(appCachePath);
        setting.setAllowFileAccess(true);

        setting.setAppCacheEnabled(true);
        mWebView.setDownloadListener(this);//设置下载监听

        //提供方法
        defaultSetting();
    }

    @SuppressLint("AddJavascriptInterface")
    private void defaultSetting() {
        mWebView.addJavascriptInterface(new Object() {
            /**错误重新加载*/
            @JavascriptInterface
            public void error() {
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        mWebView.loadUrl(url);
                    }
                });
            }
        }, "javaop");
    }

    //**Chrome*//*
    WebChromeClient chromeClient = new WebChromeClient() {
        public void onProgressChanged(WebView view, int progress) {
            if (progress == 100) {
                mWebView.setVisibility(VISIBLE);//显示
                if (onClickListener != null) {
                    onClickListener.onClick(null);
                }
                WjEventBus.getInit().post(LoadWeb.LOADFINSH, 0);
            } else {
                mWebView.setVisibility(GONE);//显示
            }
        }
    };
    //**client*//*
    WebViewClient client = new WebViewClient() {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (sonicSession != null) {
                sonicSession.getSessionClient().pageFinish(url);
            }
        }
        @TargetApi(21)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInterceptRequest(view, request.getUrl().toString());
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (sonicSession != null) {
                return (WebResourceResponse) sonicSession.getSessionClient().requestResource(url);
            }
            return null;
        }
        
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
            WjEventBus.getInit().post(LoadWeb.LOADERROE, "");
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
            WjEventBus.getInit().post(LoadWeb.LOADERROE, "");
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
            WjEventBus.getInit().post(LoadWeb.LOADERROE, "");
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    };

    //**设置提供web调用的事件*//*
    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    public void addWebEvent(Object object) {
        mWebView.addJavascriptInterface(object, "javaop");
    }


    /**
     * 重新加载url
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
        if (url.indexOf("http") < 0) {
            url = "file:///android_asset/" + url;
        } else if(url.indexOf("http")==0 || url.indexOf("https")==0) {
            // webview is ready now, just tell session client to bind
            if (sonicSessionClient != null) {
                sonicSessionClient.bindWebView(mWebView);
                sonicSessionClient.clientReady();
            } else { // default mode
                mWebView.loadUrl(url);
            }
        }else{
            mWebView.loadData(url, "text/html; charset=UTF-8", null);//这种写法可以正确解码
        }

    }

    /**
     * 设置js调用java的接口
     */
    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    public void setJavaToJs(Object obj, String interfaceName) {
        mWebView.addJavascriptInterface(obj, interfaceName);
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String
            mimetype, long contentLength) {
        Activity activity = (Activity) getContext();
        if (alertDialog == null) {
            alertDialog = new ProgressDialog(getContext());
            alertDialog.setMessage("正在下载" + contentDisposition);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            OhHttpClient.getInit().downFile(getContext(), url, new OhFileCallBakListener() {
                @Override
                public void onSuccess(String content) {

                }

                @Override
                public void onFailure(String code, String content) {
                    alertDialog.cancel();
                }

                @Override
                public void onError(Exception e) {
                    alertDialog.cancel();
                }

                @Override
                public void onFinish() {
                    alertDialog.cancel();
                }

                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    alertDialog.setMessage("正在下载" + (int) (bytesWritten / contentLength) + "%");
                }
            });
        }
    }
}
