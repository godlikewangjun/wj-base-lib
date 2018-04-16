package com.abase.okhttp;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *okhttp 的 返回调用接口
 * @author wangjun
 * @version 2.0
 * @date 2016年2月1日
 */
public abstract class OhObjectListener<T extends Object> extends OhCallBackListener{
	public Class<T> classname=null;

	public OhObjectListener() {
		Type className=this.getClass().getGenericSuperclass();
		if(className!=null && className instanceof ParameterizedType){
			ParameterizedType pt = (ParameterizedType)this.getClass().getGenericSuperclass();
			classname = (Class<T>) pt.getActualTypeArguments()[0];
		}
	}


	/**
	  * 服务器连接失败
	  */
	 public abstract void onFailure(int code,String content,Throwable error);
	 /**
	  * 连接成功
	  */
	 public  abstract void onSuccess(T content);
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
