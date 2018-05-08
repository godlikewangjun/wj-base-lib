package com.abase.view.weight;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
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
    public WebView mWebView;//网页
    //    private Map<String, String> extraHeaders;//请求头;
    private OnClickListener onClickListener;
    private boolean isSuccess=true;
    private ProgressDialog alertDialog=null;
    private boolean isTask=false;
    public static String LOADERROE="webLoadError";

    public void setTask(boolean task) {
        isTask = task;
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
        mWebView = new WebView(getContext().getApplicationContext());
        mWebView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        //默认的请求头

        loading();
        webSetting();
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
    private void webSetting() {
        addView(mWebView);

        WebSettings setting = mWebView.getSettings();
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
                if(onClickListener!=null){
                    onClickListener.onClick(null);
                }
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
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
            WjEventBus.getInit().post(LoadWeb.LOADERROE,"");
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
            WjEventBus.getInit().post(LoadWeb.LOADERROE,"");
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
//            mWebView.loadUrl("file:///android_asset/load_fail.html");
            WjEventBus.getInit().post(LoadWeb.LOADERROE,"");
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    };

    //**设置提供web调用的事件*//*
    @SuppressLint("JavascriptInterface")
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
        } else {
            mWebView.loadUrl(url);
        }

    }

    /**
     * 设置js调用java的接口
     */
    @SuppressLint("JavascriptInterface")
    public void setJavaToJs(Object obj, String interfaceName) {
        mWebView.addJavascriptInterface(obj, interfaceName);
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//        Uri uri = Uri.parse(url);
//        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//        getContext().startActivity(intent);
        if(!isSuccess){
            return;
        }
        isSuccess=false;
        Activity activity=(Activity)getContext();
        if(alertDialog==null){
            alertDialog=new ProgressDialog(getContext());
            alertDialog.setMessage("正在下载应用");
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            alertDialog.setMessage("正在下载0%");
        }
    }

}
