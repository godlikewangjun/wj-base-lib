package com.abase.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Iterator;

/**
 * 简单的SharedPreferences存储工具
 *
 * @author Admin
 * @version 1.0
 * @date 2018/1/4
 */

public class WjSharedPreferences {
    /**
     * 保存在手机里面的文件名
     */
    private static final String FILE_NAME = "save_canch";
    private static SharedPreferences default_SP;
    private static WjSharedPreferences wjSharedPreferences;

    /**
     * 缓存context
     */
    private volatile static SaveContext<Context> wjcontext=new SaveContext<Context>(null);

    public WjSharedPreferences(Context context) {
        if(wjcontext.get()==null){
            wjcontext=new SaveContext<Context>(context.getApplicationContext());
        }
        if (default_SP == null) {
            default_SP = context.getSharedPreferences(FILE_NAME,
                    Context.MODE_PRIVATE);
        }
    }

    /**
     * 获取当前的SharedPreferences
     * @return
     */
    public static SharedPreferences getDefault_SP() {
        return default_SP;
    }

    /**
     * 初始化
     *
     * @param context 最好传ApplicationContext
     */
    public synchronized static WjSharedPreferences init(Context context) {
        if (wjSharedPreferences == null) {
            wjSharedPreferences = new WjSharedPreferences(context);
        }
        return wjSharedPreferences;
    }

    /**
     * 切换操作的SharedPreferences的库
     *
     * @param context 最好传ApplicationContext
     * @param spName 名称
     */
    public WjSharedPreferences changSpData(String spName) {
        if(wjcontext.get()==null){
            AbLogUtil.e(WjSharedPreferences.class,"context is null,befor init");
            return null;
        }
        default_SP = wjcontext.get().getSharedPreferences(spName,
                Context.MODE_PRIVATE);
        return init(wjcontext.get());
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context 最好传ApplicationContext
     * @param key
     * @param object
     */
    public WjSharedPreferences setValues(String key, Object object) {
        if(wjcontext.get()==null){
            AbLogUtil.e(WjSharedPreferences.class,"context is null,befor init");
            return null;
        }
        String type = object.getClass().getSimpleName();
        SharedPreferences.Editor editor = default_SP.edit();

        if ("String".equals(type)) {
            editor.putString(key, (String) object);
        } else if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) object);
        }

        editor.apply();
        return init(wjcontext.get());
    }


    /**
     * 泛型转换
     *
     * @param context       最好传ApplicationContext
     * @param key
     * @param defaultObject
     * @return
     */
    public <V> V getValues(String key, Object defaultObject) {
        return (V)get(key,defaultObject);
    }
    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context       最好传ApplicationContext
     * @param key
     * @param defaultObject
     * @return
     */
    private Object get(String key, Object defaultObject) {
        if(defaultObject==null){
            return default_SP.getString(key,null);
        }
        String type="String";
        if(defaultObject!=null){
            type = defaultObject.getClass().getSimpleName();
        }
        if ("String".equals(type)) {
            return default_SP.getString(key, (String) defaultObject);
        } else if ("Integer".equals(type)) {
            return default_SP.getInt(key, (Integer) defaultObject);
        } else if ("Boolean".equals(type)) {
            return default_SP.getBoolean(key, (Boolean) defaultObject);
        } else if ("Float".equals(type)) {
            return default_SP.getFloat(key, (Float) defaultObject);
        } else if ("Long".equals(type)) {
            return default_SP.getLong(key, (Long) defaultObject);
        }

        return null;
    }

    /**
     * 清除所有数据
     *
     * @param context 最好传ApplicationContext
     */
    public WjSharedPreferences clear() {
        if(wjcontext.get()==null){
            AbLogUtil.e(WjSharedPreferences.class,"context is null,befor init");
            return null;
        }
        default_SP.edit().clear().apply();
        return init(wjcontext.get());
    }

    /**
     * 删除key
     *
     * @param context 最好传ApplicationContext
     * @param key
     */
    public WjSharedPreferences remove(Context context, String key) {
        if(wjcontext.get()==null){
            AbLogUtil.e(WjSharedPreferences.class,"context is null,befor init");
            return null;
        }
        SharedPreferences.Editor editor = default_SP.edit();
        editor.remove(key).apply();
        return init(wjcontext.get());
    }

    /**
     * 防止内存泄露
     * @param <T>
     */
    public static class SaveContext<T>{
        T t;
        public SaveContext(T t) {
            this.t=t;
        }

        public T get(){
            return t;
        }
    }
}
