package com.abase.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Gson的单例
 *
 * @author Admin
 * @version 1.0
 * @date 2018/5/31
 */
public class GsonUtil {
    private static Gson gson;

    public static synchronized Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    /**
     * 对象转成json
     *
     * @param object
     * @return
     */
    public static String gson2String(Object object) {
        return getGson().toJson(object);
    }

    /**
     * JSON转成对象
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> T gson2Object(String gsonStr, Class<T> cls) {
        return getGson().fromJson(gsonStr, cls);
    }

    /**
     * JSON转成list集合
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> List<T> Gson2List(String gsonStr, Class<T> cls) {
        return getGson().fromJson(gsonStr, new TypeToken<List<T>>() {
        }.getType());
    }

    /**
     * JSON转成list集合 有可能直接转换会报错 需要指定类型
     * @param s
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> Gson2ArryList(String s, Class<T[]> clazz) {
        T[] arr = GsonUtil.getGson().fromJson(s, clazz);
        ArrayList<T> data = new ArrayList<>(Arrays.asList(arr));
        return data; //or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
    }

    /**
     * json转成map的
     *
     * @param gsonString
     * @return
     */
    public static <T> Map<String, T> GsonToMap(String gsonStr) {
        return getGson().fromJson(gsonStr, new TypeToken<Map<String, T>>() {
        }.getType());
    }

    /**
     * JSON转成含有map的list集合
     *
     * @param gsonString
     * @return
     */
    public static <T> List<Map<String, T>> GsonToListMap(String gsonStr) {
        return getGson().fromJson(gsonStr,
                new TypeToken<List<Map<String, T>>>() {
                }.getType());
    }

}
