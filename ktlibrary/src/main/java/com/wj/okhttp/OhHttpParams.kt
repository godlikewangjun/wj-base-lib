package com.wj.okhttp

import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap
import java.lang.StringBuilder
import com.google.gson.JsonObject

/**
 * 参数请求
 *
 * @author wangjun
 * @version 2.0
 * @date 2016年2月16日
 */
class OhHttpParams {
    val params = ConcurrentHashMap<String, Any>()
    /** 获取keys  */
    val keys = ArrayList<String>()

    /** 添加值  */
    fun put(key: String, value: Any?): OhHttpParams {
        if (value != null) {
            if (!keys.contains(key)) {
                keys.add(key)
                params[key] = value
            } else {
                params.remove(key)
                params[key] = value
            }
        }
        return this
    }

    /** 删除键值对  */
    fun remove(key: String?): Boolean {
        if (keys.contains(key)) {
            params.remove(key)
            keys.remove(key)
            return true
        }
        return false
    }

    /**
     * get value
     */
    operator fun get(key: String): Any? {
        return params[key]
    }

    /**获取拼接的字符串 */
    val paramString: String
        get() {
            val str = StringBuilder("?")
            for (i in keys.indices) {
                if (i != keys.size - 1) {
                    str.append(keys[i]).append("=").append(params[keys[i]]).append("&")
                } else {
                    str.append(keys[i]).append("=").append(params[keys[i]])
                }
            }
            return str.toString()
        }

    /**获取拼接的字符串 */
    val jSONString: String
        get() {
            val jsonObject = JsonObject()
            for (i in keys.indices) {
                val value = params[keys[i]]
                if (value is String) {
                    jsonObject.addProperty(keys[i], value as String?)
                } else if (value is Number) {
                    jsonObject.addProperty(keys[i], value as Number?)
                }
            }
            return jsonObject.toString()
        }

    override fun toString(): String {
        return params.toString()
    }
}