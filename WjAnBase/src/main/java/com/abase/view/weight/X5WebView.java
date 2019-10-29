package com.abase.view.weight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import androidx.annotation.RequiresPermission;

import com.abase.okhttp.OhFileCallBakListener;
import com.abase.okhttp.OhHttpClient;
import com.abase.util.AbFileUtil;
import com.abase.view.weight.web.WebMethodsListener;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.wj.eventbus.WjEventBus;

public class X5WebView extends WebView implements DownloadListener {
    public String url;//加载的地址
    public WebMethodsListener webMethodsListener;
    private ProgressDialog alertDialog = null;
    public WebViewClient client = new WebViewClient() {
        /**
         * 防止加载网页时调起系统浏览器
         */
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            WjEventBus.getInit().post(LoadWeb.LOADFINSH, "");
        }

        @Override
        public void onReceivedError(WebView webView, int i, String s, String s1) {
            super.onReceivedError(webView, i, s, s1);
            WjEventBus.getInit().post(LoadWeb.LOADERROE, "");
        }

        @Override
        public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
            super.onReceivedError(webView, webResourceRequest, webResourceError);
            WjEventBus.getInit().post(LoadWeb.LOADERROE, "");
        }

        @Override
        public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
            super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }
    };

    public X5WebView(Context arg0) {
        super(arg0);
        this.setWebViewClient(client);
        // this.setWebChromeClient(chromeClient);
        // WebStorage webStorage = WebStorage.getInstance();
        initWebViewSettings();
        this.getView().setClickable(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public X5WebView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        this.setWebViewClient(client);
        // this.setWebChromeClient(chromeClient);
        // WebStorage webStorage = WebStorage.getInstance();
        initWebViewSettings();
        this.getView().setClickable(true);
    }

    private void initWebViewSettings() {
        WebSettings webSetting = getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        setWebChromeClient(chromeClient);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSetting.setGeolocationEnabled(true);//允许地理位置可用
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(AbFileUtil.getCacheDownloadDir(getContext()));
        webSetting.setDatabasePath(getContext().getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(getContext().getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);

        CookieSyncManager.createInstance(getContext());
        CookieSyncManager.getInstance().sync();

        setDownloadListener(this);
    }

    //**Chrome*//*
    com.tencent.smtt.sdk.WebChromeClient chromeClient = new com.tencent.smtt.sdk.WebChromeClient() {
        @Override
        public void onProgressChanged(WebView webView, int i) {
            if (webMethodsListener != null) {
                webMethodsListener.onX5ProgressChanged(webView, i);
            }
        }

        //处理定位权限
        @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissionsCallback callback) {
            if (webMethodsListener != null) {
                if (webMethodsListener.onX5GeolocationPermissionsShowPrompt(origin, callback)) {
                    return;
                }
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("位置信息");
            builder.setMessage(origin + "允许获取您的地理位置信息吗？").setCancelable(true).setPositiveButton("允许", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.invoke(origin, true, true);
                }
            }).setNegativeButton("不允许", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.invoke(origin, false, false);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

    };

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
            loadUrl(url);
        }
    }

    /**
     * 加载html
     *
     * @param html
     */
    public void loadHtml(String html) {
        loadData(html, "text/html; charset=UTF-8", null);//这种写法可以正确解码
    }

    /**
     * 设置js调用java的接口
     */
    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    public void setJavaToJs(Object obj, String interfaceName) {
        addJavascriptInterface(obj, interfaceName);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
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
