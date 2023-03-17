/*
 * Copyright (C) 2012 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wj.util

import java.io.IOException
import java.lang.Exception
import java.lang.StringBuffer
import android.annotation.SuppressLint
import java.lang.StringBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InputStream
import kotlin.jvm.JvmOverloads
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * 字符串处理类.
 * @author wangjun
 * @version 1.0
 * @date 2016/9/30
 */
object AbStrUtil {
    /**
     * 描述：判断一个字符串是否为null或空值.
     *
     * @param str 指定的字符串
     * @return true or false
     */
    fun isEmpty(str: String?): Boolean {
        return str == null || str.trim { it <= ' ' }.isEmpty() || str.trim { it <= ' ' } == "null"
    }

    /**
     * 获取字符串中文字符的长度（每个中文算2个字符）.
     *
     * @param str 指定的字符串
     * @return 中文字符的长度
     */
    fun chineseLength(str: String): Int {
        var valueLength = 0
        val chinese = Regex("[\u0391-\uFFE5]")
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */if (!isEmpty(str)) {
            for (i in str.indices) {
                /* 获取一个字符 */
                val temp = str.substring(i, i + 1)
                /* 判断是否为中文字符 */if (temp.matches(chinese)) {
                    valueLength += 2
                }
            }
        }
        return valueLength
    }

    /**
     * 描述：获取字符串的长度.
     *
     * @param str 指定的字符串
     * @return  字符串的长度（中文字符计2个）
     */
    fun strLength(str: String): Int {
        var valueLength = 0
        val chinese =  Regex("[\u0391-\uFFE5]")
        if (!isEmpty(str)) {
            //获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
            for (i in str.indices) {
                //获取一个字符
                val temp = str.substring(i, i + 1)
                //判断是否为中文字符
                valueLength += if (temp.matches(chinese)) {
                    //中文字符长度为2
                    2
                } else {
                    //其他字符长度为1
                    1
                }
            }
        }
        return valueLength
    }

    /**
     * 描述：获取指定长度的字符所在位置.
     *
     * @param str 指定的字符串
     * @param maxL 要取到的长度（字符长度，中文字符计2个）
     * @return 字符的所在位置
     */
    fun subStringLength(str: String, maxL: Int): Int {
        var currentIndex = 0
        var valueLength = 0
        val chinese =  Regex("[\u0391-\uFFE5]")
        //获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
        for (i in str.indices) {
            //获取一个字符
            val temp = str.substring(i, i + 1)
            //判断是否为中文字符
            valueLength += if (temp.matches(chinese)) {
                //中文字符长度为2
                2
            } else {
                //其他字符长度为1
                1
            }
            if (valueLength >= maxL) {
                currentIndex = i
                break
            }
        }
        return currentIndex
    }

    /**
     * 描述：手机号格式验证.
     *
     * @param str 指定的手机号码字符串
     * @return 是否为手机号码格式:是为true，否则false
     */
    fun isMobileNo(str: String?): Boolean {
        var isMobileNo = false
        try {
            val p = Pattern
                .compile("^((\\+86)|(86))?1([3-9][0-9])\\d{8}$")
            val m = p.matcher(str)
            isMobileNo = m.matches()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isMobileNo
    }

    /**
     * 描述：是否只是字母和数字.
     *
     * @param str 指定的字符串
     * @return 是否只是字母和数字:是为true，否则false
     */
    fun isNumberLetter(str: String): Boolean {
        var isNoLetter = false
        val expr =  Regex("^[A-Za-z0-9]+$")
        if (str.matches(expr)) {
            isNoLetter = true
        }
        return isNoLetter
    }

    /**
     * 描述：是否只是数字.
     *
     * @param str 指定的字符串
     * @return 是否只是数字:是为true，否则false
     */
    fun isNumber(str: String): Boolean {
        var isNumber = false
        val expr =  Regex("^[0-9]+$")
        if (str.matches(expr)) {
            isNumber = true
        }
        return isNumber
    }

    /**
     * 描述：是否是邮箱.
     *
     * @param str 指定的字符串
     * @return 是否是邮箱:是为true，否则false
     */
    fun isEmail(str: String): Boolean {
        var isEmail = false
        val expr =
            Regex("^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$")
        if (str.matches(expr)) {
            isEmail = true
        }
        return isEmail
    }

    /**
     * 描述：是否是中文.
     *
     * @param str 指定的字符串
     * @return  是否是中文:是为true，否则false
     */
    fun isChinese(str: String): Boolean {
        var isChinese = true
        val chinese =  Regex("[\u0391-\uFFE5]")
        if (!isEmpty(str)) {
            //获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
            for (i in str.indices) {
                //获取一个字符
                val temp = str.substring(i, i + 1)
                //判断是否为中文字符
                if (temp.matches(chinese)) {
                } else {
                    isChinese = false
                }
            }
        }
        return isChinese
    }

    /**
     * 描述：是否包含中文.
     *
     * @param str 指定的字符串
     * @return  是否包含中文:是为true，否则false
     */
    fun isContainChinese(str: String): Boolean {
        var isChinese = false
        val chinese =  Regex("[\u0391-\uFFE5]")
        if (!isEmpty(str)) {
            //获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
            for (i in str.indices) {
                //获取一个字符
                val temp = str.substring(i, i + 1)
                //判断是否为中文字符
                if (temp.matches(chinese)) {
                    isChinese = true
                } else {
                }
            }
        }
        return isChinese
    }

    /**
     * 描述：从输入流中获得String.
     *
     * @param is 输入流
     * @return 获得的String
     */
    fun convertStreamToString(`is`: InputStream): String {
        val reader = BufferedReader(InputStreamReader(`is`))
        val sb = StringBuilder()
        var line: String? = null
        try {
            while (reader.readLine().also { line = it } != null) {
                sb.append("""
    $line
    
    """.trimIndent())
            }

            //最后一个\n删除
            if (sb.indexOf("\n") != -1 && sb.lastIndexOf("\n") == sb.length - 1) {
                sb.delete(sb.lastIndexOf("\n"), sb.lastIndexOf("\n") + 1)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }

    /**
     * 描述：标准化日期时间类型的数据，不足两位的补0.
     *
     * @param dateTime 预格式的时间字符串，如:12154545
     * @return String 格式化好的时间字符串，如:2012-03-20 12:02:20
     */
    fun dateTimeFormat(date: Date?): String {
        @SuppressLint("SimpleDateFormat") val simpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.format(date)
    }

    /**
     * 描述：不足2个字符的在前面补“0”.
     *
     * @param str 指定的字符串
     * @return 至少2个字符的字符串
     */
    fun strFormat2(str: String): String {
        var str = str
        try {
            if (str.length <= 1) {
                str = "0$str"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return str
    }
    /**
     * 描述：截取字符串到指定字节长度.
     *
     * @param str 文本
     * @param length 字节长度
     * @param dot 省略符号
     * @return 截取后的字符串
     */
    /**
     * 描述：截取字符串到指定字节长度.
     *
     * @param str the str
     * @param length 指定字节长度
     * @return 截取后的字符串
     */
    @JvmOverloads
    fun cutString(str: String, length: Int, dot: String? = ""): String {
        val strBLen = strlen(str, "GBK")
        if (strBLen <= length) {
            return str
        }
        var temp = 0
        val sb = StringBuffer(length)
        val ch = str.toCharArray()
        for (c in ch) {
            sb.append(c)
            temp += if (c.code > 256) {
                2
            } else {
                1
            }
            if (temp >= length) {
                if (dot != null) {
                    sb.append(dot)
                }
                break
            }
        }
        return sb.toString()
    }

    /**
     * 描述：截取字符串从第一个指定字符.
     *
     * @param str1 原文本
     * @param str2 指定字符
     * @param offset 偏移的索引
     * @return 截取后的字符串
     */
    fun cutStringFromChar(str1: String, str2: String?, offset: Int): String {
        if (isEmpty(str1)) {
            return ""
        }
        val start = str1.indexOf(str2!!)
        if (start != -1) {
            if (str1.length > start + offset) {
                return str1.substring(start + offset)
            }
        }
        return ""
    }

    /**
     * 描述：获取字节长度.
     *
     * @param str 文本
     * @param charset 字符集（GBK）
     * @return the int
     */
    fun strlen(str: String?, charset: String?): Int {
        if (str == null || str.length == 0) {
            return 0
        }
        var length = 0
        try {
            length = str.toByteArray(charset(charset!!)).size
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return length
    }

    /**
     * 获取大小的描述.
     *
     * @param size 字节个数
     * @return  大小的描述
     */
    fun getSizeDesc(size: Long): String {
        var size = size
        var suffix = "B"
        if (size >= 1024) {
            suffix = "K"
            size = size shr 10
            if (size >= 1024) {
                suffix = "M"
                //size /= 1024;
                size = size shr 10
                if (size >= 1024) {
                    suffix = "G"
                    size = size shr 10
                    //size /= 1024;
                }
            }
        }
        return size.toString() + suffix
    }

    /**
     * 描述：ip地址转换为10进制数.
     *
     * @param ip the ip
     * @return the long
     */
    fun ip2int(ip: String): Long {
        var ip = ip
        ip = ip.replace(".", ",")
        val items = ip.split(",").toTypedArray()
        return java.lang.Long.valueOf(items[0]) shl 24 or (java.lang.Long.valueOf(items[1]) shl 16) or (java.lang.Long.valueOf(
            items[2]) shl 8) or java.lang.Long.valueOf(items[3])
    }

    /**
     * 对json字符串格式化输出
     * @param jsonStr
     * @return
     */
    fun formatJson(jsonStr: String?): String {
        if (null == jsonStr || "" == jsonStr) return ""
        val sb = StringBuilder()
        var last = '\u0000'
        var current = '\u0000'
        var indent = 0
        for (i in 0 until jsonStr.length) {
            last = current
            current = jsonStr[i]
            when (current) {
                '{', '[' -> {
                    sb.append(current)
                    sb.append('\n')
                    indent++
                    addIndentBlank(sb, indent)
                }
                '}', ']' -> {
                    sb.append('\n')
                    indent--
                    addIndentBlank(sb, indent)
                    sb.append(current)
                }
                ',' -> {
                    sb.append(current)
                    if (last != '\\') {
                        sb.append('\n')
                        addIndentBlank(sb, indent)
                    }
                }
                else -> sb.append(current)
            }
        }
        return sb.toString()
    }

    /**
     * 添加space
     * @param sb
     * @param indent
     */
    private fun addIndentBlank(sb: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            sb.append('\t')
        }
    }
}