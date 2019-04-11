package com.abase.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;

import com.abase.global.AbAppConfig;
import com.abase.okhttp.Interceptor.DownInterceptor;
import com.abase.okhttp.Interceptor.GzipRequestInterceptor;
import com.abase.okhttp.body.FileRequestBody;
import com.abase.okhttp.body.MultipartBodyRbody;
import com.abase.okhttp.cookies.PersistentCookieStore;
import com.abase.okhttp.db.SQLTools;
import com.abase.okhttp.log.HttpLoggingInterceptor;
import com.abase.okhttp.util.DownLoad;
import com.abase.task.AbThreadFactory;
import com.abase.util.AbFileUtil;
import com.abase.util.AbLogUtil;
import com.abase.util.GsonUtil;
import com.abase.util.Tools;
import com.wj.eventbus.WjEventBus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.FormBody.Builder;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;
import okio.Buffer;

/**
 * okhttp封装类的客户端
 *
 * @author wangjun
 * @version 2.0
 * @date 2016年2月1日
 */
public class OhHttpClient {
    /**
     * 超时报错
     */
    public static final String OKHTTP_TIMEOUT = "OKHTTP_TIMEOUT";
    /**
     * 成功.
     */
    protected static final int SUCCESS_MESSAGE = 0;
    /**
     * 失败.
     */
    protected static final int FAILURE_MESSAGE = 1;
    /**
     * 报错
     */
    protected static final int ERROE_MESSAGE = 2;
    /**
     * 进度消息
     */
    protected static final int PROGRESS_MESSAGE = 3;
    /**
     * 完成消息
     */
    protected static final int FINSH_MESSAGE = 4;
    /**
     * 开始消息
     */
    protected static final int START_MESSAGE = 5;

    /**
     * 连接超时的时间
     */
    public static int CONNECTTIMEOUT = 30;
    /**
     * 写入的超的时间
     */
    public static int WRITETIMEOUT = 60;
    /**
     * 读取的超的时间
     */
    public static int READTIMEOUT = 60;
    /**
     * 缓存的路径
     */
    public static String CACHEPATH = null;
    /**
     * 缓存的大小
     */
    public static int cacheSize = 100 * 1024;
    /**
     * 缓存的请求的失效时间
     */
    public static int cacheTimeOut = 5;
    /**
     * 下载文件的目录
     */
    public static String DOWNDIR = "/storage/emulated/0/Download";
    /**
     * 是否格式化请求成功的json字符串
     */
    private boolean isJsonFromMat = true;
    /**
     * 限制上传下载的速度即是时间间隔
     */
    public int dowmUpTime = -1;
    /**
     * 销毁的请求url集合 最大缓存10条
     */
    private ArrayList<String> destoryUrls = new ArrayList<>();

    private static OhHttpClient ohHttpClient;
    private OkHttpClient client;
    private static Headers headers = null;
    private PersistentCookieStore cookieStore;//cookies
    public Handler handler = new Handler(Looper.getMainLooper());
    private HttpLoggingInterceptor logging;//打印日志
    private  Charset UTF8 = Charset.forName("UTF-8");


    public HttpLoggingInterceptor getLogging() {
        return logging;
    }

    public OhHttpClient setLogging(HttpLoggingInterceptor logging) {
        this.logging = logging;
        return this;
    }

    /**
     * 获取销毁的urls 存量 最大缓存10条
     *
     * @return 销毁的请求url集合
     */
    public ArrayList<String> getDestoryUrls() {
        return destoryUrls;
    }


    public boolean isJsonFromMat() {
        return isJsonFromMat;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    //json类型传入的参数 自动转
    public static String JSONTYE = "MediaTypeJson";

    /**
     * 设置格式化请求成功的json
     */
    public OhHttpClient setJsonFromMat(boolean jsonFromMat) {
        isJsonFromMat = jsonFromMat;
        return ohHttpClient;
    }

    /**
     * 初始化
     */
    public OhHttpClient() {
        if (client == null) {
            client = new OkHttpClient();
            setOkHttpClient();
        }
    }


    /**
     * 获取实例
     */
    public static synchronized OhHttpClient getInit() {
        if (ohHttpClient == null) {
            ohHttpClient = new OhHttpClient();
        }
        return ohHttpClient;
    }

    /**
     * 销毁
     */
    public static synchronized OhHttpClient destory() {
        if (ohHttpClient != null && ohHttpClient.client != null) {
            ohHttpClient.client.dispatcher().cancelAll();
            ohHttpClient.client = null;
            ohHttpClient = null;
        }
        return ohHttpClient;
    }

    /**
     * 关闭自己的日志
     */
    public void closeLog() {

    }

    /**
     * 添加头
     */
    public OhHttpClient setHeaders(Headers headers) {
        OhHttpClient.headers = headers;
        return ohHttpClient;
    }

    /**
     * 获取头
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * 设置Cookies
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public OhHttpClient setCookies(Context context) {
        if (cookieStore == null) {
            cookieStore = new PersistentCookieStore(context);
        }
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client.newBuilder().cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                if (cookies != null && cookies.size() > 0) {
                    for (Cookie item : cookies) {
                        cookieStore.add(url, item);
                    }
                }
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url);
                return cookies;
            }
        }).build();
        return ohHttpClient;
    }

    /**
     * 设置读取缓存的头
     */
    public OhHttpClient setCanchHeader(Context context) {
        if (headers != null) {
            headers.newBuilder().add("Cache-Control", "max-stale=3600");
        } else {
            headers = new Headers.Builder().add("Cache-Control", "max-stale=3600").build();
        }
        File file;
        if (CACHEPATH == null) {
            file = context.getCacheDir();
        } else {
            file = new File(CACHEPATH);
        }

        Cache cache = new Cache(file, cacheSize);
        client.newBuilder().cache(cache).build();
        return ohHttpClient;
    }

    /**
     * 销毁请求的url
     */
    public void destoryUrl(final String url) {
        if (destoryUrls.size() > 10) {
            destoryUrls.remove(0);
        }
        for (int i = 0; i < destoryUrls.size(); i++) {
            if (destoryUrls.get(i).equals(url)) {
                destoryUrls.remove(i);
                break;
            }
        }
        destoryUrls.add(url);
        if (client != null && url != null) {
//            if(queue==null){
//                queue=new LinkedList<String>();
//            }
//            queue.offer(url);
            AbThreadFactory.getExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (client.dispatcher().getClass()) {
                        for (Call call : client.dispatcher().queuedCalls()) {
                            if (url.equals(call.request().tag())) {
                                call.cancel();
                            }
                        }
                        for (Call call : client.dispatcher().runningCalls()) {
                            if (url.equals(call.request().tag())) call.cancel();
                        }
                    }
                }
            });
        }
    }

    /**
     * 销毁请求的url
     */
    public boolean isHaveUrl(final String url) {
        if (client != null && url != null) {
            synchronized (client.dispatcher().getClass()) {
                for (Call call : client.dispatcher().queuedCalls()) {
                    if (url.equals(call.request().tag())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 销毁所有url
     */
    public void destoryAll() {
        if (client != null) {
            client.dispatcher().cancelAll();
            ohHttpClient = null;
        }
    }

    /**
     * 配置http
     */
    private void setOkHttpClient() {
        okhttp3.OkHttpClient.Builder builder = client.newBuilder();
        builder.connectTimeout(CONNECTTIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(WRITETIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(READTIMEOUT, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);//错误重连
        //默认就设置日志打印
        if (logging != null) {
            logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addNetworkInterceptor(logging);
        }
        client = builder.build();
    }

    /**
     * 设置是否gzip压缩请求body
     */
    public void setGzip(boolean isGzip) {
        if (isGzip) {
            client = client.newBuilder().addInterceptor(new GzipRequestInterceptor()).build();// 加入拦截器
        } else {
            client.interceptors().remove(new GzipRequestInterceptor());
        }
    }

    /**
     * 设置ssl签名 需要证书访问网站的时候用 ,context.getAssets().open("zhy_server.cer") 输入流 或者使用setCertificates 写入字符串
     */
    public void setCertificates(InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            client = client.newBuilder().sslSocketFactory(sslContext.getSocketFactory()).build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置ssl签名 需要证书访问网站的时候用
     */
    public void setCertificates(String certificates) {
        InputStream certificate = new Buffer().writeUtf8(certificates).inputStream();
        setCertificates(certificate);
    }

    /**
     * get请求
     */
    public void get(String url, OhObjectListener callbackListener) {
        haveNoBody(url, callbackListener, 0);
    }

    /**
     * delete请求
     */
    public void delete(String url, OhCallBackListener callbackListener) {
        haveNoBody(url, callbackListener, 1);
    }

    /**
     * 不携带参数的返回string的统一方法 0 是get 1是delete
     */
    private void haveNoBody(String url, OhCallBackListener callbackListener, int type) {
        okhttp3.Request.Builder builder = new Request.Builder().url(url).tag(url);// 设置tag;
        switch (type) {
            case 0:// get
                builder.get();
                break;
            case 1:// delete
                builder.delete();
                break;
        }
        if (headers != null) {
            builder.headers(headers);
        }
        Request request = builder.build();
        client.newCall(request).enqueue(new OKHttpCallBack(request, callbackListener));
    }

    /**
     * post请求
     */
    public void post(String url, OhHttpParams requestParams,
                     OhObjectListener callbackListener) {
        haveBody(url, requestParams, callbackListener, 0);
    }

    /**
     * put请求
     */
    public void put(String url, OhHttpParams requestParams,
                    OhObjectListener callbackListener) {
        haveBody(url, requestParams, callbackListener, 1);
    }

    /**
     * patch请求
     */
    public void patch(String url, OhHttpParams requestParams,
                      OhObjectListener callbackListener) {
        haveBody(url, requestParams, callbackListener, 2);
    }

    /**
     * dedelete请求
     */
    public void delete(String url, OhHttpParams requestParams,
                       OhCallBackListener callbackListener) {
        haveBody(url, requestParams, callbackListener, 3);
    }

    /**
     * 携带参数的返回string的统一方法 0 是post 1是put 2是patch 3是delete
     */
    private void haveBody(String url, OhHttpParams requestParams,
                          OhCallBackListener callbackListener, int type) {
        RequestBody body;
        if (requestParams != null && requestParams.getKeys().contains(JSONTYE)) {
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (String) requestParams.get(JSONTYE));
        } else {
            Builder requestBody = new Builder();
            if (requestParams != null) {
                ArrayList<String> keys = requestParams.getKeys();
                for (int i = 0; i < keys.size(); i++) {
                    requestBody.add(keys.get(i), requestParams.get(keys.get(i)).toString());
                }
            }
            body = requestBody.build();
        }

        okhttp3.Request.Builder builder = new Request.Builder().url(url).tag(url);// 设置tag
        switch (type) {
            case 0:// post
                builder.post(body);
                break;
            case 1:// put
                builder.put(body);
                break;
            case 2:// patch
                builder.patch(body);
                break;
            case 3:// delete
                builder.delete(body);
                break;
        }
        if (headers != null) {
            builder.headers(headers);
        }
        Request request = builder.build();
        client.newCall(request).enqueue(new OKHttpCallBack(request, callbackListener));
    }

    /**
     * 上传文件
     * 模拟表单提交，因为头不能有中文,所有文件名要进行编码
     */
    public void upFile(String url, OhHttpParams requestParams, File file,
                       OhFileCallBakListener callbackListener) {
        // 写入文件流 用于单个文件上传
        // text/x-markdown; charset=utf-8
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);

        // 调用多个文件上传的body 封装进入
        okhttp3.MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("files", getFileEndCode(file.getName()), requestBody);
        // 添加post参数
        if (requestParams != null) {
            String key;
            Iterator<String> iterator = requestParams.getParams().keySet().iterator();
            while (iterator.hasNext()) {
                key = iterator.next();
                multipartBody.addFormDataPart(key, requestParams.get(key).toString());
            }
        }
        // 封装请求
        okhttp3.Request.Builder builder = new Request.Builder().url(url).tag(url);// 设置tag
        if (headers != null) {
            builder.headers(headers);
        }
        builder.post(multipartBody.build());
        Request request = builder.build();
        callbackListener.ohtype = 0;// 设置类型是上传
        client.newCall(request).enqueue(new OKHttpCallBack(request, callbackListener));
    }

    /**
     * 上传文件
     * 模拟表单提交，因为头不能有中文,所有文件名要进行编码
     */
    public void upFiles(String url, OhHttpParams requestParams, List<File> files,
                        OhFileCallBakListener callbackListener) {
        // 调用多个文件上传的body 封装进入
        okhttp3.MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        // 添加多个文件
        for (int i = 0; i < files.size(); i++) {
            multipartBody.addFormDataPart("file", getFileEndCode(files.get(i).getName()), RequestBody.create(MediaType.parse("application/octet-stream"), files.get(i)));
        }
        // 添加post参数
        if (requestParams != null) {
            String key;
            Iterator<String> iterator = requestParams.getParams().keySet().iterator();
            while (iterator.hasNext()) {
                key = iterator.next();
                multipartBody.addFormDataPart(key, requestParams.get(key).toString());
            }
        }
        // 封装请求
        okhttp3.Request.Builder builder = new Request.Builder().url(url).tag(url);// 设置tag
        if (headers != null) {
            builder.headers(headers);
        }
        builder.post(new MultipartBodyRbody(multipartBody.build(), callbackListener));
        Request request = builder.build();
        callbackListener.ohtype = 0;// 设置类型是上传
        client.newCall(request).enqueue(new OKHttpCallBack(request, callbackListener));
    }

    /**
     * 编码文件名，防止okHttp3在header中出现中文报错
     *
     * @param fileName
     * @return
     */
    private String getFileEndCode(String fileName) {
        try {
            return URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Tools.setMD5(fileName);
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
    public FileRequestBody upFileStream(String url, OhHttpParams requestParams, File file, String type,
                                        OhFileCallBakListener callbackListener) {
        if (type == null) {
            type = "application/octet-stream";
        }
        // 写入文件流 用于单个文件上传
        // text/x-markdown; charset=utf-8
        FileRequestBody requestBody = new FileRequestBody(MediaType.parse(type), file, 0, callbackListener);
        // 调用多个文件上传的body 封装进入
        MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(AbFileUtil.getFileType(file), file.getName(), requestBody);
        // 添加post参数
        if (requestParams != null) {
            String key;
            Iterator<String> iterator = requestParams.getParams().keySet().iterator();
            while (iterator.hasNext()) {
                key = iterator.next();
                multipartBody.addFormDataPart(key, requestParams.get(key).toString());
            }
        }
        // 封装请求
        Request.Builder builder = new Request.Builder().url(url).tag(url);// 设置tag
        if (headers != null) {
            builder.headers(headers);
        }
        builder.post(multipartBody.build());
        Request request = builder.build();
        callbackListener.ohtype = 0;// 设置类型是上传
        client.newCall(request).enqueue(new OKHttpCallBack(request, callbackListener));
        return requestBody;
    }


    /**
     * 下载文件
     *
     * @param url
     * @param isBreakpoint     是否支持断点下载
     * @param callbackListener
     */
    public DownLoad downFile(Context context, String url, final OhFileCallBakListener callbackListener) {
        boolean isLoading = false;
        synchronized (client.dispatcher().getClass()) { //当前任务在下载队列就返回
            for (Call call : client.dispatcher().queuedCalls()) {
                if (url.equals(call.request().tag())) {
                    isLoading = true;
                    return null;
                }
            }
            for (Call call : client.dispatcher().runningCalls()) {
                if (url.equals(call.request().tag())) call.cancel();
            }
        }
        if (isLoading) {
            return null;
        }
        okhttp3.Request.Builder builder = new Request.Builder().url(url).tag(url).get();// 设置tag
        if (headers != null) {
            builder.headers(headers);
        }
        String id = Tools.setMD5(url);
        File file = new File(DOWNDIR, id + ".temp");
        if (file.exists()) {
            String total = "";
            JSONObject jsonObject = SQLTools.init(context).selectDownLoad(id);
            if (jsonObject != null && jsonObject.has("id")) {
                try {
                    total = jsonObject.getLong("totallength") + "";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            builder.header("range", "bytes=" + file.length() + "-" + total);//断点续传要用到的，指示下载的区间
           AbLogUtil.d(OhHttpClient.class,"bytes=" + file.length() + "-" + total + "  本地存的文件长度：" + jsonObject.toString());
        }

        Request request = builder.build();
        List<Interceptor> interceptors=client.networkInterceptors();
        for (int i=0;i<interceptors.size();i++){
            if(interceptors.get(i) instanceof DownInterceptor){
                break;
            }else if(i==interceptors.size()-1){
                client=client.newBuilder().addNetworkInterceptor(new DownInterceptor(callbackListener)).build();
            }
        }
        //清理请求的
        destoryUrls.clear();
        DownLoad downLoad = new DownLoad(context);
        callbackListener.ohtype = 1;// 设置类型是下载
        client.newCall(request).enqueue(new OKHttpCallBack(request, downLoad, callbackListener));
        return downLoad;
    }

    /**
     * 统一的处理okhttp的返回结果的格式
     */
    public class OKHttpCallBack implements Callback {
        private OhCallBackListener callbackListener;
        private Request request;
        private int failNum = 0;// 失败次数
        public long time;//起始时间
        private DownLoad downLoad;

        public OKHttpCallBack(Request request, OhCallBackListener callbackListener) {
            this.request = request;
            init(callbackListener);
        }

        public OKHttpCallBack(Request request, DownLoad downLoad, OhCallBackListener callbackListener) {
            this.request = request;
            this.downLoad = downLoad;
            init(callbackListener);
        }

        private void init(OhCallBackListener callbackListener) {
            if (callbackListener == null) {
                callbackListener = new OhObjectListener<String>() {

                    @Override
                    public void onSuccess(String content) {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onFailure(int code, String content, Throwable error) {

                    }
                };
            }
            this.callbackListener = callbackListener;
            this.callbackListener.setHandler(new ResponderHandler(callbackListener));
            // 设置hander
            this.callbackListener.sendStartMessage();//开始
        }


        @Override
        public void onFailure(Call call, IOException e) {
            if (SocketTimeoutException.class.equals(e.getCause())) {
                AbLogUtil.e(OhHttpClient.class, request.url().toString() + ", 请检查网络,连接超时");

                //网络不好重试
                AbLogUtil.d(OhHttpClient.class, "连接不到:" + request.url().toString() + ",重试" + failNum + "次");
                if (failNum > 2) {//失败消息为0
                    callbackListener.sendFailureMessage(0,
                            "连接不到:" + request.url().toString() + ",重试超过最大的次数" + failNum, null);
                    callbackListener.sendFinshMessage();

                    //打印错误和发送通知方便全局处理
                    e.printStackTrace();
                    if (Looper.myLooper() == null) Looper.prepare();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            WjEventBus.getInit().post(OKHTTP_TIMEOUT, 0);
                        }
                    });
                } else {
                    if (Looper.myLooper() == null) Looper.prepare();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            client.newCall(request).enqueue(OKHttpCallBack.this);
                        }
                    });
                    failNum++;
                }

            } else {//code==-1 不是超时错误
                AbLogUtil.e(OhHttpClient.class, request.url().toString() + "," + e.getMessage());
                e.printStackTrace();
                callbackListener.sendFailureMessage(-1, e.getMessage(), e);
            }
        }

        @Override
        public void onResponse(final Call call, final Response response) throws IOException {
            final String url = request.url().toString();
            int code = response.code();
            // 存在服务器返回压缩就解压缩 okhttp自动解压就不做处理
//			if (response.header("Content-Encoding") != null && response.header("Content-Encoding").equals("gzip")) {
//				body = gunzip(body);
//			}
//            WjEventBus.getInit().post(response.headers());
            if (code == 200 || code == 206) {// 服务器响应成功 206是断点
                if (callbackListener instanceof OhObjectListener) {// 请求sring的监听
                    ResponseBody responseBody = response.body();
                    Charset charset = UTF8;
                    MediaType contentType = responseBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }
                    String body = responseBody.source().readString(charset);
                    if (!String.class.equals(((OhObjectListener) callbackListener).classname)) {
                        try {
                            callbackListener.sendSucessMessage(GsonUtil.getGson().fromJson(body, ((OhObjectListener) callbackListener).classname));
                        } catch (Exception e) {
                            e.printStackTrace();
                            AbLogUtil.e(OhHttpClient.class, ((OhObjectListener) callbackListener).classname + ";" + url + ",返回json格式化错误" + body);
                            if (failNum == 3) {
                                ((OhObjectListener) callbackListener).onFailure(400, "类格式化错误", e);
                            }
                            return;
                        }
                    } else
                        callbackListener.sendSucessMessage(body);

                } else if (callbackListener instanceof OhFileCallBakListener) {// 请求文件的监听
                    if (callbackListener.ohtype == 0) {// 上传
                        String body = response.body().string();
                        AbLogUtil.i(OhHttpClient.class, url + "," + body);
                        callbackListener.sendSucessMessage(body);
                    } else if (callbackListener.ohtype == 1) {// 下载
                        final String name = Tools.setMD5(url);
                        downLoad.saveFile(response, callbackListener, DOWNDIR, name + ".temp");
                        downLoad = null;
                        return;
                    }
                }
            } else if (code == 301 || code == 302) {// 重试
                AbLogUtil.d(OhHttpClient.class, url + ",重试" + failNum + "次");
                if (failNum > 2) {
                    callbackListener.sendFailureMessage(response.code(),
                            request.url().toString() + ",重试超过最大的次数" + failNum, null);
                    callbackListener.sendFinshMessage();
                } else {
                    if (Looper.myLooper() == null) Looper.prepare();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            client.newCall(request).enqueue(OKHttpCallBack.this);
                        }
                    });

                    failNum++;
                }

            } else if (code == 401) {// 用户认证
                client.newBuilder().authenticator(new Authenticator() {

                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic("user", "password");
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }
                });
                callbackListener.sendFailureMessage(code, url + "," + AbAppConfig.NOT_FOUND_USER, null);
                AbLogUtil.e(OhHttpClient.class, url + "," + response.body().string());
            } else if (code == 404) {// 没有找到接口或页面
                callbackListener.sendFailureMessage(code, url + "," + AbAppConfig.NOT_FOUND_EXCEPTION, null);
                AbLogUtil.e(OhHttpClient.class, url + "," + response.body().string());
            } else {
                String body = response.body().string();
                callbackListener.sendFailureMessage(code, url + "," + body, null);
                AbLogUtil.e(OhHttpClient.class, url + "," + body);
            }


            try {
                callbackListener.sendFinshMessage();
//                AbFileUtil.writeAppend("jk", "\n接口:" + request.url().toString() + ",时间:" + (System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                AbLogUtil.i(getClass(), url + ",错误");
            }
        }
    }


    /**
     * 描述：请求返回
     */
    @SuppressLint("HandlerLeak")
    private class ResponderHandler implements OhCallBackMessageInterface {

        /**
         * 响应数据.
         */
        private Object[] response;

        /**
         * 响应消息监听.
         */
        private OhCallBackListener responseListener;

        /**
         * 响应消息处理.
         */
        public ResponderHandler(OhCallBackListener responseListener) {
            this.responseListener = responseListener;
        }

        /**
         * 处理消息
         *
         * @param what
         * @param response
         */
        private void callBack(int what) {
            switch (what) {
                case SUCCESS_MESSAGE:// 成功
                    if (responseListener instanceof OhObjectListener) {// 字符串的请求
                        ((OhObjectListener) responseListener).onSuccess(
                                response[0]);
                    } else if (responseListener instanceof OhFileCallBakListener) {// 文件
                        ((OhFileCallBakListener) responseListener).onSuccess(
                                (String) response[0]);
                    }
                    break;
                case FAILURE_MESSAGE:// 失败
                    if (responseListener instanceof OhObjectListener) {// 字符串的请求
                        ((OhObjectListener) responseListener).onFailure((Integer) response[0],
                                (String) response[1], null);
                    } else if (responseListener instanceof OhFileCallBakListener) {// 文件
                        ((OhFileCallBakListener) responseListener).onFailure(response[0] + "",
                                response[1]+"");
                    }
                    break;
                case ERROE_MESSAGE:// 报错
                    if (responseListener instanceof OhObjectListener) {// 字符串的请求
                        ((OhObjectListener) responseListener).onFailure(-1, "报错", (Exception) response[0]);
                    } else if (responseListener instanceof OhFileCallBakListener) {// 文件
                        ((OhFileCallBakListener) responseListener).onError((Exception) response[0]);
                    }
                    break;
                case PROGRESS_MESSAGE:// 进度消息
                    if (response != null && response.length >= 2) {
                        ((OhFileCallBakListener) responseListener).onRequestProgress((Long) response[0], (Long) response[1],
                                (Boolean) response[2]);
                    } else {
                        AbLogUtil.e(OhHttpClient.class, "PROGRESS_MESSAGE " + AbAppConfig.MISSING_PARAMETERS);
                    }
                    break;
                case FINSH_MESSAGE:// 完成
                    if (responseListener instanceof OhObjectListener) {// 字符串的请求
                        ((OhObjectListener) responseListener).onFinish();
                    } else if (responseListener instanceof OhFileCallBakListener) {// 文件
                        ((OhFileCallBakListener) responseListener).onFinish();
                    }
                    break;
                case START_MESSAGE://开始
                    if (responseListener instanceof OhObjectListener) {// 字符串的请求
                        ((OhObjectListener) responseListener).onStart();
                    }
                    break;
            }
        }

        @Override
        public void handedMessage(final Message msg) {
            if (Looper.myLooper() == null) Looper.prepare();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    response = (Object[]) msg.obj;
                    callBack(msg.what);
                }
            });

        }
    }

    /**
     * 使用gzip进行压缩
     */
    public static String gzip(String primStr) {
        if (primStr == null || primStr.length() == 0) {
            return primStr;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(primStr.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new String(out.toByteArray());
    }

    /**
     * gzip进行解压缩
     */
    public static String gunzip(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;
        try {
            compressed = compressedStr.getBytes();
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);

            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }

        return decompressed;
    }

}
