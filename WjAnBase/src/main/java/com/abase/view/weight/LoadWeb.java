package com.abase.view.weight;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;

import androidx.annotation.RequiresApi;

import android.util.AttributeSet;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
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
import com.abase.view.weight.web.ObservableWebView;
import com.abase.view.weight.web.WebMethodsListener;
import com.wj.eventbus.WjEventBus;


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
    private ProgressDialog alertDialog = null;
    public static String LOADERROE = "webLoadError";
    public static String LOADFINSH = "webLoadFinsh";
    public WebMethodsListener webMethodsListener;


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
    public void reload() {
        mWebView.reload();
    }


    /**
     * 设置没有加载完成时的界面
     */
    private void loading() {
//        mWebView.setVisibility(GONE);//隐藏
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
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);
        setting.setAllowFileAccess(true);
        setting.setGeolocationEnabled(true);//允许地理位置可用

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
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (webMethodsListener != null) {
                webMethodsListener.onProgressChanged(view, progress);
            }

        }

        //处理定位权限
        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            if (webMethodsListener != null) {
                if (webMethodsListener.onGeolocationPermissionsShowPrompt(origin, callback)) {
                    return;
                }
            }
            final boolean remember = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("位置信息");
            builder.setMessage(origin + "允许获取您的地理位置信息吗？").setCancelable(true).setPositiveButton("允许", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.invoke(origin, true, remember);
                }
            }).setNegativeButton("不允许", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.invoke(origin, false, remember);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
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
            WjEventBus.getInit().post(LoadWeb.LOADFINSH, 0);
        }

        @TargetApi(21)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInterceptRequest(view, request.getUrl().toString());
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
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
        } else if (url.indexOf("http") == 0 || url.indexOf("https") == 0) {
            // webview is ready now, just tell session client to bind
            mWebView.loadUrl(url);
        }
    }

    /**
     * 加载html
     *
     * @param html
     */
    public void loadHtml(String html) {
        mWebView.loadData(html, "text/html; charset=UTF-8", null);//这种写法可以正确解码
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
