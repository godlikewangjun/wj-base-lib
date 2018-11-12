package com.abase.okhttp;

import android.util.Log;

import com.abase.util.AbStrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数请求
 * 
 * @author wangjun
 * @version 2.0
 * @date 2016年2月16日
 */
public class OhHttpParams {
	private ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<String, Object>();
	private ArrayList<String> keys = new ArrayList<String>();

    public ConcurrentHashMap<String, Object> getParams() {
        return params;
    }

    /** 添加值 */
	public OhHttpParams put(String key, Object value) {
		if (key != null && value != null) {
			if(!keys.contains(key)){
				keys.add(key);
				params.put(key, value);
			}else{
				params.remove(key);
				params.put(key, value);
			}
			
		}
		return this;
	}

	/** 获取keys */
	public ArrayList<String> getKeys() {
		return keys;
	}
	/** 删除键值对 */
	public boolean remove(String key) {
		if(keys.contains(key)){
			params.remove(key);
			keys.remove(key);
			return true;
		}
		
		return false;
	}

	/**
	 * get value
	 */
	public <T> Object get(T key) {
		return params.get(key);
	}
	/**获取拼接的字符串*/
	public String getParamString(){
		StringBuilder str= new StringBuilder("?");
		for (int i = 0; i < keys.size(); i++) {
			if(i!=keys.size()-1){
				str.append(keys.get(i)).append("=").append(params.get(keys.get(i))).append("&");
			}else{
				str.append(keys.get(i)).append("=").append(params.get(keys.get(i)));
			}
		}
		return str.toString();
	}
    /**获取拼接的字符串*/
    public String getJSONString(){
        JsonObject jsonObject=new JsonObject();
        for (int i = 0; i < keys.size(); i++) {
        	Object value=params.get(keys.get(i));
        	if(value instanceof String){
				jsonObject.addProperty(keys.get(i),(String) value);
			}else if(value instanceof Number){
				jsonObject.addProperty(keys.get(i),(Number) value);
			}
        }
        return jsonObject.toString();
    }
	@Override
	public String toString() {
		return params.toString();
	}

}
