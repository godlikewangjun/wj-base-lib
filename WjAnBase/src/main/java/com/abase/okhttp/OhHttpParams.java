package com.abase.okhttp;

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
	private ConcurrentHashMap<String, String> params = new ConcurrentHashMap<String, String>();
	private ArrayList<String> keys = new ArrayList<String>();

    public ConcurrentHashMap<String, String> getParams() {
        return params;
    }

    /** 添加值 */
	public OhHttpParams put(String key, String value) {
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
	public <T> String get(T key) {
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
            jsonObject.addProperty(keys.get(i),params.get(keys.get(i)));
        }
        return jsonObject.toString();
    }
	@Override
	public String toString() {
		return params.toString();
	}

}
