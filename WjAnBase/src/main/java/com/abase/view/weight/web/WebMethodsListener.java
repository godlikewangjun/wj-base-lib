package com.abase.view.weight.web;

import android.webkit.GeolocationPermissions;
import android.webkit.WebView;

import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;

/**
 * 网页加载进度的回调
 * @author Administrator
 * @version 1.0
 * @date 2018/12/4
 */
public abstract class WebMethodsListener {
    /**
     * 网页加载进度的回调
     * @param view
     * @param progress
     */
    public void onProgressChanged(WebView view, int progress){

    }

    /**
     * 定位权限处理
     * @param origin
     * @param callback
     * @return 返回true是拦截 false是使用默认的
     */
    public boolean onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback){
        return false;
    }

    /**
     * 网页加载进度的回调
     * @param view
     * @param progress
     */
    public void onProgressChanged(com.tencent.smtt.sdk.WebView view, int progress){

    }

    /**
     * 定位权限处理
     * @param origin
     * @param callback
     * @return 返回true是拦截 false是使用默认的
     */
    public boolean onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissionsCallback callback){
        return false;
    }

}
