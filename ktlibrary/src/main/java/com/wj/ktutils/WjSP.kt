package com.wj.ktutils

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * 简单的SharedPreferences存储工具
 *
 * @author Admin
 * @version 1.0
 * @date 2018/1/4
 */
class WjSP() {

    constructor(context: Context?,spName: String) : this() {
        init(context,spName)
    }
    /**
     * 切换操作的SharedPreferences的库
     *
     * @param spName 名称
     */
    fun changSpData(spName: String?): WjSP? {
        mmkv=MMKV.mmkvWithID(spName)
        return getInstance()
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    @Synchronized
    fun setValues(key: String?, `object`: Any): WjSP? {
        when (`object`.javaClass.simpleName) {
            "String" -> {
                mmkv?.encode(key, `object` as String)
            }
            "Integer" -> {
                mmkv?.encode(key, (`object` as Int))
            }
            "Boolean" -> {
                mmkv?.encode(key, (`object` as Boolean))
            }
            "Float" -> {
                mmkv?.encode(key, (`object` as Float))
            }
            "Long" -> {
                mmkv?.encode(key, (`object` as Long))
            }
            "ByteArray" -> {
                mmkv?.encode(key, (`object` as ByteArray))
            }
        }
        return getInstance()
    }

    /**
     * 泛型转换
     *
     * @param key
     * @param defaultObject
     * @return
     */
    @Synchronized
    fun <V> getValues(key: String, defaultObject: V?): V {
        return get(key, defaultObject) as V
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @param defaultObject
     * @return
     */
    private operator fun get(key: String, defaultObject: Any?): Any? {
        if (defaultObject == null) {
            return mmkv!!.decodeString(key, null)
        }
        when (defaultObject.javaClass.simpleName) {
            "String" -> {
                return mmkv!!.decodeString(key, defaultObject as String?)
            }
            "Integer" -> {
                return mmkv!!.decodeInt(key, (defaultObject as Int?)!!)
            }
            "Boolean" -> {
                return mmkv!!.decodeBool(key, (defaultObject as Boolean?)!!)
            }
            "Float" -> {
                return mmkv!!.decodeFloat(key, (defaultObject as Float?)!!)
            }
            "Long" -> {
                return mmkv!!.decodeLong(key, (defaultObject as Long?)!!)
            }
            "ByteArray" -> {
                return mmkv!!.decodeBytes(key, (defaultObject as ByteArray?)!!)
            }
            else -> return null
        }
    }

    /**
     * 清除所有数据
     *
     */
    fun clear(): WjSP? {
        mmkv!!.clearAll()
        return getInstance()
    }

    /**
     * 删除key
     *
     * @param key
     */
    fun remove(key: String?): WjSP? {
        mmkv!!.removeValueForKey(key)
        return getInstance()
    }


    /**
     * 防止内存泄露
     * @param <T>
    </T> */
    class SaveContext<T>(var t: T) {
        fun get(): T {
            return t
        }
    }

    companion object {
        /**
         * 获取当前的SharedPreferences
         * @return
         */
        private var wjSharedPreferences: WjSP? = null

        var mmkv:MMKV?=null
        /**
         * 初始化
         *
         * @param context 最好传ApplicationContext
         */
        fun init(context: Context?,spName: String?=null): WjSP {
            MMKV.initialize(context)
            mmkv = if (spName==null)
                MMKV.defaultMMKV()
            else MMKV.mmkvWithID(spName)
            if (wjSharedPreferences == null) {
                wjSharedPreferences = WjSP()
            }
            return wjSharedPreferences!!
        }

        /**
         * 获取值
         *
         */
        fun getInstance(): WjSP? {
            return wjSharedPreferences
        }
    }
}
