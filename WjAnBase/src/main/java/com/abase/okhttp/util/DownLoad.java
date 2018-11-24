package com.abase.okhttp.util;

import android.content.Context;

import com.abase.okhttp.OhCallBackListener;
import com.abase.okhttp.OhHttpClient;
import com.abase.okhttp.db.SQLTools;
import com.abase.util.AbFileUtil;
import com.abase.util.AbLogUtil;
import com.abase.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * @author Admin
 * @version 1.0
 * @date 2017/7/6
 */

public class DownLoad {
    //下载速度
    private static int bufferSize = 100*1024;
    //下载延迟时间
    public static int time = 5;
    private boolean isPause = false;
    private long len = 0;
    private int sum = 0;
    private Context context;
    private String id,dir;

    public DownLoad(Context context) {
        this.context = context;
    }

    /**
     * 保存文件
     */
    public void saveFile(Response response, OhCallBackListener callbackListener, String destFileDir,
                         String destFileName) {
        BufferedSink bufferedSink = null;
        BufferedSource source = null;
        Buffer buffer = null;
        this.dir=destFileDir;
        //打印成功返回的日志
        try {
            String url = response.request().url().toString();
            id = Tools.setMD5(url);

            source = response.body().source();
            long total = response.body().contentLength();
            AbLogUtil.i(OhHttpClient.class, response.request().url() + ",\n 下载文件地址：" + destFileDir + "/" + destFileName + ";大小:" + total);

            if(!SQLTools.init(context).selectDownLoad(id).has("id")){
                SQLTools.init(context).saveDowmloadInfo(id,total+"");
            }
            if (total == 0) {
                AbFileUtil.deleteFile(new File(destFileDir, ".temp"));
            }
            JSONObject jsonObject = SQLTools.init(context).selectDownLoad(id);
            if (jsonObject != null && jsonObject.has("id")) {
                try {
                    total = jsonObject.getLong("totallength");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);
            if (!file.exists()) {
                file.createNewFile();
                bufferedSink = Okio.buffer(Okio.sink(file));
            } else {
                sum += file.length();
                bufferedSink = Okio.buffer(Okio.appendingSink(file));
            }

            buffer = bufferedSink.buffer();

            ArrayList<String> list = OhHttpClient.getInit().getDestoryUrls();

            while (!isPause) {
                if (isPause) {
                    break;
                }
                len = (int) source.read(buffer, bufferSize);
                if (len == -1) {
                    break;
                }
                for (int i = list.size() - 1; i > -1; i--) {
                    if (list.get(i).equals(url)) {
                        list.remove(list.get(i));
                        AbLogUtil.i(OhHttpClient.class, url + ",销毁了");
                        break;
                    }
                }
                bufferedSink.emit();
                sum += len;
                Thread.sleep(time);
                callbackListener.sendProgressMessage(sum, total, false);
                if (file.length() >= total) {
                    File newFile=new File(dir, destFileName.replace(".temp", "."+AbFileUtil.getFileType(file)));
                    file.renameTo(newFile);//重新命名
                    callbackListener.sendProgressMessage(sum, total, true);
                    callbackListener.sendSucessMessage(newFile.getAbsolutePath());
                    SQLTools.init(context).delDownLoad(id);
                    SQLTools.init(context).onDestory();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackListener.sendFailureMessage(405, "write apk erro", e);
        } finally {
            if (bufferedSink != null) {
                try {
//                    bufferedSink.flush();
                    buffer.clear();
                    source.timeout().clearDeadline();
                    bufferedSink.timeout().clearDeadline();

                    buffer.close();
                    source.close();
                    bufferedSink.close();
                    callbackListener.sendFinshMessage();
                    System.gc();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 取消
     */
    public void cancle() {
        isPause = true;
    }
    /**
     * 取消
     */
    public void stop() {
        isPause = true;
        AbFileUtil.deleteFile(new File(dir, id+".temp"));
    }
}
