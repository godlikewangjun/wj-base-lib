package com.wj.util

import java.lang.Class
import java.util.ArrayList
import com.google.gson.Gson
import kotlin.jvm.Synchronized
import com.google.gson.reflect.TypeToken
import java.util.Arrays
import com.google.gson.JsonObject
/**
 * Gson的单例
 *
 * @author Admin
 * @version 1.0
 * @date 2018/5/31
 */
object GsonUtil {
    @get:Synchronized
    val gson: Gson by lazy { Gson() }

    /**
     * 对象转成json
     *
     * @param object
     * @return
     */
    fun gson2String(`object`: Any?): String {
        return gson.toJson(`object`)
    }

    /**
     * JSON转成对象
     *
     * @param gsonString
     * @param cls
     * @return
     */
    fun <T> gson2Object(gsonStr: String?, cls: Class<T>?): T {
        return gson.fromJson(gsonStr, cls)
    }

    /**
     * JSON转成list集合
     *
     * @param gsonString
     * @param cls
     * @return
     */
    fun <T> Gson2List(gsonStr: String?, cls: Class<T>?): List<T> {
        return gson.fromJson(gsonStr, object : TypeToken<List<T>?>() {}.type)
    }

    /**
     * JSON转成list集合 有可能直接转换会报错 需要指定类型
     *
     * @param s
     * @param clazz
     * @param <T>
     * @return
    </T> */
    fun <T> Gson2ArryList(s: String?, clazz: Class<Array<T>>?): List<T> {
        val arr = gson.fromJson(s, clazz)
        return ArrayList(listOf(*arr)) //or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
    }

    /**
     * json转成map的
     *
     * @param gsonString
     * @return
     */
    fun <T> GsonToMap(gsonStr: String?): Map<String, T> {
        return gson.fromJson(gsonStr, object : TypeToken<Map<String?, T>?>() {}.type)
    }

    /**
     * JSON转成含有map的list集合
     *
     * @param gsonString
     * @return
     */
    fun <T> GsonToListMap(gsonStr: String?): List<Map<String, T>> {
        return gson.fromJson(gsonStr,
            object : TypeToken<List<Map<String?, T>?>?>() {}.type)
    }

    /**
     * 解析json纯数组
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
    </T> */
    fun <T> jsonToArrayList(json: String?, clazz: Class<T>?): ArrayList<T> {
        val type = object : TypeToken<ArrayList<JsonObject?>?>() {}.type
        val jsonObjects = Gson().fromJson<ArrayList<JsonObject>>(json, type)
        val arrayList = ArrayList<T>()
        for (jsonObject in jsonObjects) {
            arrayList.add(Gson().fromJson(jsonObject, clazz))
        }
        return arrayList
    }
}