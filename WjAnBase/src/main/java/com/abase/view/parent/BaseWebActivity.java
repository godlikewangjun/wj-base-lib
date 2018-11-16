package com.abase.view.parent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.WindowManager;

import com.abase.util.Tools;
import com.abase.view.weight.LoadWeb;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网页浏览器
 * @author Administrator
 * @version 1.0
 * @date 2018/10/28/028
 */
public class BaseWebActivity extends BaseActivity{
    //加载速度
    private SonicSession sonicSession;
    private SonicSessionClientImpl sonicSessionClient = null;

    @Override
    public void before() {
        super.before();
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public int setContentView() {
        return 0;
    }

    @Override
    public void afertOp() {

    }


    /**
     * 加载速度提升,必须设置加载布局（setcontentview）之前
     * MODE_DEFAULT = 0;
     * MODE_SONIC = 1;
     * MODE_SONIC_WITH_OFFLINE_CACHE = 2;
     * PERMISSION_REQUEST_CODE_STORAGE = 1;
     */
    public  void sonicFast(final Activity activity, int type, String url) {
        (activity).getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        // init sonic engine if necessary, or maybe u can do this when application created
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(new SonicRuntimeImpl(activity), new SonicConfig.Builder().build());
        }

        // if it's sonic mode , startup sonic session at first time
        if (type == 0) { // sonic mode
            SonicSessionConfig.Builder sessionConfigBuilder = new SonicSessionConfig.Builder();
            sessionConfigBuilder.setSupportLocalServer(true);

            // if it's offline pkg mode, we need to intercept the session connection
            if (type == 2) {
                sessionConfigBuilder.setCacheInterceptor(new SonicCacheInterceptor(null) {
                    @Override
                    public String getCacheData(SonicSession session) {
                        return null; // offline pkg does not need cache
                    }
                });

                sessionConfigBuilder.setConnectionInterceptor(new SonicSessionConnectionInterceptor() {
                    @Override
                    public SonicSessionConnection getConnection(SonicSession session, Intent intent) {
                        return new OfflinePkgSessionConnection(activity, session, intent);
                    }
                });

                // create sonic session and run sonic flow
                sonicSession = SonicEngine.getInstance().createSession(url, sessionConfigBuilder.build());
                if (null != sonicSession) {
                    sonicSession.bindClient(sonicSessionClient = new SonicSessionClientImpl());
                } else {
                    // this only happen when a same sonic session is already running,
                    // u can comment following codes to feedback as a default mode.
                    // throw new UnknownError("create session fail!");
                    Tools.showToast(activity, "create sonic session fail!");
                }
            }

        }
    }

    /**
     * 绑定webview,在加载网页之前
     */
    @SuppressLint("AddJavascriptInterface")
    public void bindWebView(LoadWeb loadWeb){
        loadWeb.mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        Intent intent=new Intent();
        intent.putExtra(SonicJavaScriptInterface.PARAM_LOAD_URL_TIME, System.currentTimeMillis());
        loadWeb.mWebView.addJavascriptInterface(new SonicJavaScriptInterface(sonicSessionClient, intent), "sonic");
        loadWeb.setSonicSession(sonicSession);
        loadWeb.setSonicSessionClient(sonicSessionClient);
    }
    /**
     * 监听连接
     */
    private  class OfflinePkgSessionConnection extends SonicSessionConnection {

        private final WeakReference<Context> context;

        public OfflinePkgSessionConnection(Context context, SonicSession session, Intent intent) {
            super(session, intent);
            this.context = new WeakReference<Context>(context);
        }

        @Override
        protected int internalConnect() {
            Context ctx = context.get();
            if (null != ctx) {
                try {
                    InputStream offlineHtmlInputStream = ctx.getAssets().open("sonic-demo-index.html");
                    responseStream = new BufferedInputStream(offlineHtmlInputStream);
                    return SonicConstants.ERROR_CODE_SUCCESS;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return SonicConstants.ERROR_CODE_UNKNOWN;
        }

        @Override
        protected BufferedInputStream internalGetResponseStream() {
            return responseStream;
        }

        @Override
        public void disconnect() {
            if (null != responseStream) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getResponseCode() {
            return 200;
        }

        @Override
        public Map<String, List<String>> getResponseHeaderFields() {
            return new HashMap<>(0);
        }

        @Override
        public String getResponseHeaderField(String key) {
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != sonicSession) {
            sonicSession.destroy();
            sonicSession = null;
        }
    }
}
