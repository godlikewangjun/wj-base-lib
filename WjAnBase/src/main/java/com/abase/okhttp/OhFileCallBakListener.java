package com.abase.okhttp;

/**
 * 文件监听回调
 * @author wangjun
 * @version 2.0
 * @date 2016年2月2日
 */
public abstract class OhFileCallBakListener extends OhCallBackListener implements OhProgressListener{
	 /**
	  * 连接成功
	  */
	 public  abstract void onSuccess(String content);
	 /**
	  * 服务器连接失败
	  */
	 public abstract void onFailure(String code,String content);
	 /**
	 * 连接接错误
	 */
	 public abstract void onError(Exception e);
	 /**
	  * 完成
	  */
	 public  abstract void onFinish();

	/**
	 * 开始
	 */
	public  void onStart(){

	};
}
