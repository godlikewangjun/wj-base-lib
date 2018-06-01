package com.abase.util;

import com.google.gson.Gson;

/**
 * Gson的单例
 * @author Admin
 * @version 1.0
 * @date 2018/5/31
 */
public class GsonUtil {
    private static Gson gson;

    public static synchronized Gson getGson() {
        if(gson==null){
            gson=new Gson();
        }
        return gson;
    }
}
