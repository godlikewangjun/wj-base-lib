package com.abase.view.weight;

import android.content.Context;
import android.util.Log;

import com.abase.util.AbLogUtil;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

/**
 * qqX%浏览器的操作帮助类
 * @author Administrator
 * @version 1.0
 * @date 2018/11/14/014
 */
public class QqWebHelper {
    public static void X5Init(Context context){
        //设置开启优化方案
//        HashMap<String, Object> map = new HashMap<String, Object>();
//        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
//        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, false);//多线程方案策略配置
//        QbSdk.initTbsSettings(map);


        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                AbLogUtil.d(QqWebHelper.class, " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                AbLogUtil.d(QqWebHelper.class, "X5 is Finished" );
            }
        };
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                System.out.println("下载完成-------------");
            }
            @Override
            public void onInstallFinish(int i) {
                System.out.println("安装完成-------------");
            }
            @Override
            public void onDownloadProgress(int i) {
                System.out.println("进度------------- "+i);
            }
        });
        //x5内核初始化接口
        QbSdk.initX5Environment(context,  cb);
        //设置是否需要在wifi下下载 默认是true
        QbSdk.setDownloadWithoutWifi(true);

    }
}
