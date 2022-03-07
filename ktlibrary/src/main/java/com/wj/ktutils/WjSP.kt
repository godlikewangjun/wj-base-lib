package com.wj.ktutils

import android.content.Context
import android.content.SharedPreferences
import com.abase.util.AbLogUtil

/**
 * 简单的SharedPreferences存储工具
 *
 * @author Admin
 * @version 1.0
 * @date 2018/1/4
 */
class WjSP() {
    constructor(context: Context?) : this() {
        if (wjContext.get() == null) {
            wjContext = SaveContext(context)
        }
        if (default_SP == null) {
            default_SP = context!!.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    /**
     * 切换操作的SharedPreferences的库
     *
     * @param spName 名称
     */
    fun changSpData(spName: String?): WjSP? {
        if (wjContext.get() == null) {
            AbLogUtil.e(WjSP::class.java, "context is null,before init")
            return null
        }
        default_SP = wjContext.get()!!.getSharedPreferences(
            spName,
            Context.MODE_PRIVATE
        )
        return init(wjContext.get())
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    @Synchronized
    fun setValues(key: String?, `object`: Any): WjSP? {
        if (wjContext.get() == null) {
            AbLogUtil.e(WjSP::class.java, "context is null,before init")
            return null
        }
        val type = `object`.javaClass.simpleName
        val editor = default_SP!!.edit()
        when (type) {
            "String" -> {
                editor.putString(key, `object` as String)
            }
            "Integer" -> {
                editor.putInt(key, (`object` as Int))
            }
            "Boolean" -> {
                editor.putBoolean(key, (`object` as Boolean))
            }
            "Float" -> {
                editor.putFloat(key, (`object` as Float))
            }
            "Long" -> {
                editor.putLong(key, (`object` as Long))
            }
        }
        editor.apply()
        return init(wjContext.get())
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
            return default_SP!!.getString(key, null)
        }
        when (defaultObject.javaClass.simpleName) {
            "String" -> {
                return default_SP!!.getString(key, defaultObject as String?)
            }
            "Integer" -> {
                return default_SP!!.getInt(key, (defaultObject as Int?)!!)
            }
            "Boolean" -> {
                return default_SP!!.getBoolean(key, (defaultObject as Boolean?)!!)
            }
            "Float" -> {
                return default_SP!!.getFloat(key, (defaultObject as Float?)!!)
            }
            "Long" -> {
                return default_SP!!.getLong(key, (defaultObject as Long?)!!)
            }
            else -> return null
        }
    }

    /**
     * 清除所有数据
     *
     */
    fun clear(): WjSP? {
        if (wjContext.get() == null) {
            AbLogUtil.e(WjSP::class.java, "context is null,before init")
            return null
        }
        default_SP!!.edit().clear().apply()
        return init(wjContext.get())
    }

    /**
     * 删除key
     *
     * @param key
     */
    fun remove(context: Context?, key: String?): WjSP? {
        if (wjContext.get() == null) {
            AbLogUtil.e(WjSP::class.java, "context is null,before init")
            return null
        }
        val editor = default_SP!!.edit()
        editor.remove(key).apply()
        return init(wjContext.get())
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
         * 保存在手机里面的文件名
         */
        private const val FILE_NAME = "save_cache"

        /**
         * 获取当前的SharedPreferences
         * @return
         */
        var default_SP: SharedPreferences? = null
        private var wjSharedPreferences: WjSP? = null

        /**
         * 缓存context
         */
        @Volatile
        private var wjContext = SaveContext<Context?>(null)

        /**
         * 初始化
         *
         * @param context 最好传ApplicationContext
         */
        @Synchronized
        fun init(context: Context?): WjSP {
            if (wjSharedPreferences == null) {
                wjSharedPreferences = WjSP(context)
            }
            return wjSharedPreferences!!
        }

        /**
         * 获取值
         *
         */
        @Synchronized
        fun getInstance(): WjSP {
            return wjSharedPreferences!!
        }
    }
}
