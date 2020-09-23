package com.abase.okhttp.util;

import android.content.Context;
import android.os.Handler;

import com.abase.okhttp.OhFileCallBakListener;
import com.abase.okhttp.OhHttpClient;
import com.abase.okhttp.OhHttpParams;
import com.abase.okhttp.OhProgressListener;
import com.abase.util.AbLogUtil;
import com.abase.util.AbStrUtil;
import com.abase.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 断点上传
 *
 * @author Admin
 * @version 1.0
 * @date 2017/7/5
 */

public class UpLoad {
    private Handler handler = new Handler();
    private UpLoadRunable upLoadRunable;
    private boolean isPause = false;
    private OhProgressListener ohProgressListener;
    private Context context;
    private int maxSize = 1024 * 100;

    /**
     * 分段上传
     *
     * @param url
     * @param requestParams
     * @param file
     * @param lastLength
     * @param callbackListener
     */
    public UpLoad upload(Context context, String url, final File file, final long lastLength,
                              OhProgressListener ohProgressListener) {
        if (upLoadRunable != null) {
            handler.removeCallbacks(upLoadRunable);
        }
        isPause = false;
        this.context = context;
        this.ohProgressListener = ohProgressListener;
        upLoadRunable = new UpLoadRunable(file, null, url);
        handler.post(upLoadRunable);

        return this;
    }

    /**
     * 分段上传
     *
     * @param url
     * @param requestParams
     * @param file
     * @param lastLength
     * @param callbackListener
     */
    public UpLoad upload(Context context, String url, final File file,OhHttpParams ohHttpParams, OhProgressListener ohProgressListener) {
        if (upLoadRunable != null) {
            handler.removeCallbacks(upLoadRunable);
        }
        isPause = false;
        this.context = context;
        this.ohProgressListener = ohProgressListener;
        upLoadRunable = new UpLoadRunable(file, ohHttpParams, url);
        handler.post(upLoadRunable);

        return this;
    }

    /**
     * 暂停取消请求
     */
    public void onPause() {
        if (handler != null && upLoadRunable != null) {
            isPause = true;
            handler.removeCallbacks(upLoadRunable);
        }
    }

    /**
     * 重新开始
     */
    public boolean onReStart() {
        if (handler != null && upLoadRunable != null && !upLoadRunable.isFinsh) {
            isPause = false;
            handler.post(upLoadRunable);
            return true;
        }
        return false;
    }

    /**
     * 销毁
     */
    public void onDestory() {
        if (handler != null && upLoadRunable != null) {
            isPause = true;
            handler.removeCallbacks(upLoadRunable);
            handler = null;
            upLoadRunable = null;
        }
    }

    /**
     * 线程执行类
     */
    class UpLoadRunable implements Runnable {
        long lastSize;
        FileInputStream fileInputStream;
        byte[] canch = new byte[maxSize];
        long total, remaining;
        OhHttpParams requestParams;
        String url;
        File file;
        RandomAccessFile accessFile;
        String sourceid = "";
        boolean isFinsh = false;

        public UpLoadRunable(File file, OhHttpParams requestParams, String url) {
            this.fileInputStream = fileInputStream;
            this.total = file.length();
            this.requestParams = requestParams;
            this.url = url;
            this.file = file;

            try {
                stream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 读取流
         */
        private void stream() throws IOException {
            if (fileInputStream != null) {
                remaining = fileInputStream.available();
            }
            if (accessFile == null || remaining > maxSize) {
                try {
                    if (accessFile == null) {
                        accessFile = new RandomAccessFile(file, "r");
                    }
                    accessFile.seek(lastSize);// 仅上传未完成的文件内容
                    fileInputStream = new FileInputStream(accessFile.getFD());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                accessFile.seek(total - total % maxSize);// 仅上传未完成的剩余文件内容
                fileInputStream = new FileInputStream(accessFile.getFD());
                canch = new byte[(int) (total % maxSize)];
                isFinsh = true;
            }
        }

        @Override
        public void run() {
            try {
                stream();

                AbLogUtil.d(UpLoad.class, "正在上传：" + url + "; 地址：" + file.getAbsolutePath());
                fileInputStream.read(canch);
                OhHttpClient.getInit().upFile(url,"files", requestParams, file, new OhFileCallBakListener() {

                    @Override
                    public void onSuccess(String content) {
                        if (lastSize + maxSize <= total) {
                            lastSize += maxSize;
                        } else {
                            lastSize = total;
                        }
                        ohProgressListener.onRequestProgress(lastSize, total, total == lastSize);
                        if (total != lastSize) {
                        } else {
                        }
                        if (!isPause && !isFinsh) {
                            handler.postDelayed(upLoadRunable, 200);
                        }
                    }

                    @Override
                    public void onFailure(String code, String content) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {

                    }
                });
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
