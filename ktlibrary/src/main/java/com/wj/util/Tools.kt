package com.wj.util

import android.Manifest
import android.graphics.Bitmap
import android.content.Context
import java.io.IOException
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import kotlin.Throws
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import java.security.MessageDigest
import java.lang.StringBuffer
import java.security.NoSuchAlgorithmException
import java.lang.Class
import android.view.View
import android.view.LayoutInflater
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds
import android.net.Uri
import android.database.Cursor
import android.text.TextUtils
import android.graphics.PixelFormat
import android.graphics.Canvas
import com.wj.util.Tools
import android.util.DisplayMetrics
import android.app.Activity
import android.widget.TextView
import android.text.method.LinkMovementMethod
import android.text.Spannable
import android.text.style.URLSpan
import android.text.SpannableStringBuilder
import com.wj.util.Tools.MyURLSpan
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import android.text.style.ClickableSpan
import android.widget.LinearLayout
import android.os.Build
import java.lang.NumberFormatException
import android.annotation.SuppressLint
import android.provider.DocumentsContract
import android.os.Environment
import android.content.ContentUris
import android.provider.MediaStore
import java.lang.StringBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import android.graphics.Bitmap.CompressFormat
import android.os.Build.VERSION
import android.util.Base64
import com.wj.util.AbFileUtil.Companion.deleteFile
import java.lang.reflect.Field
import java.util.*
import java.util.regex.Pattern

/**
 * 操作工具集合
 *
 * @author 王军
 * @date 2014年12月15日
 * @版本 1.0
 */
object Tools {
    var wh  : IntArray?=null // 屏幕的宽和高
    var mBitmap: Bitmap? = null

    /**
     * 读取properties
     */
    fun getPropertiesValue(context: Context?): Properties {
        val properties = Properties()
        if (context != null && !context.isRestricted) {
            try {
                properties.load(context.assets.open("peizi.properties"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return properties
    }

    /**
     * 保存配置文件
     *
     * @param context
     * @param file
     * @param properties
     * @return
     */
    fun saveProperty(
        context: Context?, file: String?,
        properties: Properties
    ): Boolean {
        try {
            val fil = File(file)
            if (!fil.exists()) fil.createNewFile()
            val s = FileOutputStream(fil)
            properties.store(s, "")
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 获取签名
     */
    fun getSign(context: Context): String? {
        val pm = context.packageManager
        val apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES)
        val iter: Iterator<PackageInfo> = apps.iterator()
        while (iter.hasNext()) {
            val packageinfo = iter.next()
            val packageName = packageinfo.packageName
            if (packageinfo.packageName == packageName) {
            }
            return packageinfo.signatures[0].toCharsString()
        }
        return null
    }

    /**
     * Java文件操作 获取文件扩展名
     */
    fun getExtensionName(filename: String?): String? {
        if (filename != null && filename.length > 0) {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length - 1) {
                return filename.substring(dot + 1)
            }
        }
        return filename
    }

    /**
     * Java文件操作 获取不带扩展名的文件名
     */
    fun getFileNameNoEx(filename: String?): String? {
        if (filename != null && filename.length > 0) {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length) {
                return filename.substring(0, dot)
            }
        }
        return filename
    }

    /**
     *
     * 将base64字符保存文本文件
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    @Throws(Exception::class)
    fun toFile(base64Code: String, targetPath: String?) {
        val buffer = base64Code.toByteArray()
        val out = FileOutputStream(targetPath)
        out.write(buffer)
        out.close()
    }

    /**
     * 　　* 将base64转换成bitmap图片
     * 　　*
     * 　　* @param string base64字符串
     * 　　* @return bitmap
     *
     */
    fun base64ToBitmap(base64String: String?): Bitmap? {
        var bitmap: Bitmap? = null
        val bitmapArray: ByteArray
        try {
            bitmapArray = Base64.decode(base64String, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                bitmapArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 判断网络连接是否已开
     *
     * @param context
     * @return true网络打开
     */
    fun getNetWorkStates(context: Context): Boolean {
        val cManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cManager.activeNetworkInfo
        return info!!.isAvailable
    }

    /**
     * 获取资源颜色
     *
     * @param context
     * @param color
     * @return
     */
    fun getColor(context: Context, color: Int): Int {
        return context.resources.getColor(color)
    }

    /**
     * 获取draw
     *
     * @param context
     * @return
     */
    fun getDrawable(context: Context, drawable: Int): Drawable {
        return context.resources.getDrawable(drawable)
    }

    /**
     *
     * 将文件转成base64 字符串
     * 文件路径
     *
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encodeBase64File(file: File): String {
        return Base64.encodeToString(AbImageUtil.bitmap2Bytes(AbImageUtil.getBitmap(file),
            CompressFormat.JPEG,
            true), Base64.DEFAULT)
    }

    /**
     *
     * 将base64字符解码保存文件
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decoderBase64File(base64Code: String?, targetPath: String?) {
        val buffer = Base64.decode(base64Code, Base64.DEFAULT)
        val out = FileOutputStream(targetPath)
        out.write(buffer)
        out.close()
    }

    /**
     * 显示toast
     *
     * @param context activity
     * @param text    显示的内容
     */
    fun showToast(context: Context?, text: String?) {
        val mToast = Toast.makeText(context, text, Toast.LENGTH_LONG)
        mToast.setText(text)
        mToast.show()
    }

    /**
     * 显示toast
     *
     * @param context activity
     * @param text    显示的内容
     */
    fun showTip(
        context: Context?, text: String?,
        gravity: Int
    ) {
        val mToast = Toast.makeText(context, text, Toast.LENGTH_LONG)
        mToast.setText(text)
        mToast.setGravity(gravity, 0, 0)
        mToast.show()
    }

    fun showTip(
        context: Context?, text: String?,
        gravity: Int, x: Int, y: Int
    ) {
        val mToast = Toast.makeText(context, text, Toast.LENGTH_LONG)
        mToast.setText(text)
        mToast.setGravity(gravity, x, y)
        mToast.show()
    }

    /**
     * @param source
     * @return true 表示空
     */
    fun isEmpty(source: String?): Boolean {
        if (source == null || "" == source || "null" == source) return true
        for (i in 0 until source.length) {
            val c = source[i]
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false
            }
        }
        return true
    }

    /**
     * 处理电话号码
     *
     * @param phonenum
     * @return
     */
    fun hintPhoneNum(phonenum: String?): String? {
        if (phonenum == null) {
            return phonenum
        } else if (phonenum.length != 11) {
            return phonenum
        }
        return phonenum.substring(0, 3) + "*****" + phonenum.substring(8)
    }

    /**
     * 字符串加密
     */
    fun setMD5(str: String?): String {
        if (str == null || str == "") {
            return ""
        }
        try {
            val bmd5 = MessageDigest.getInstance("MD5")
            bmd5.update(str.toByteArray())
            var i: Int
            val buf = StringBuffer()
            val b = bmd5.digest()
            for (offset in b.indices) {
                i = b[offset].toInt()
                if (i < 0) i += 256
                if (i < 16) buf.append("0")
                buf.append(Integer.toHexString(i))
            }
            return buf.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return str
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    fun getAppVersionCode(context: Context): Int {
        var versioncode = 0
        try {
            // ---get the package info---
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versioncode = pi.versionCode
            if (versioncode!=0) {
                return 0
            }
        } catch (_: Exception) {
        }
        return versioncode
    }

    /**
     * 获取版本名称
     *
     * @param context
     * @return
     */
    fun getAppVersionName(context: Context): String {
        var versionName = ""
        try {
            // ---get the package info---
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
            if (versionName.isEmpty()) {
                return ""
            }
        } catch (_: Exception) {
        }
        return versionName
    }

    /**
     * 获取通知栏的高度
     *
     * @param context
     * @return
     */
    @SuppressLint("InternalInsetResource", "PrivateApi", "DiscouragedApi")
    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        var c: Class<*>? = null
        var obj: Any? = null
        var field: Field? = null
        var x = 0
        var statusBarHeight = 0
        try {
            c = Class.forName("com.android.internal.R\$dimen")
            obj = c.newInstance()
            field = c.getField("status_bar_height")
            x = field[obj]?.toString()!!.toInt()
            statusBarHeight = context.resources.getDimensionPixelSize(x)
        } catch (e1: Exception) {
            val resources = context.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    /**
     * 获取view
     *
     * @param context 要获取view的content
     * @param id      获取view的资源id
     * @return 获取到的view
     */
    fun getContentView(context: Context?, id: Int): View {
        return LayoutInflater.from(context).inflate(id, null)
    }

    /**
     * 随机生成一个多少位的大小写字母加数字组成的字串
     *
     * @return 生成的字符串
     */
    fun randompwd(count: Int): String {
        val r = Random()
        var i = 0
        var str = ""
        var s: String? = null
        while (i < count) { // 这个地方的30控制产生几位随机数，这里是产生30位随机数
            when (r.nextInt(63)) {
                0 -> s = "0"
                1 -> s = "1"
                2 -> s = "2"
                3 -> s = "3"
                4 -> s = "4"
                5 -> s = "5"
                6 -> s = "6"
                7 -> s = "7"
                8 -> s = "8"
                9 -> s = "9"
                10 -> s = "a"
                11 -> s = "b"
                12 -> s = "c"
                13 -> s = "d"
                14 -> s = "e"
                15 -> s = "f"
                16 -> s = "g"
                17 -> s = "h"
                18 -> s = "i"
                19 -> s = "j"
                20 -> s = "k"
                21 -> s = "m"
                23 -> s = "n"
                24 -> s = "o"
                25 -> s = "p"
                26 -> s = "q"
                27 -> s = "r"
                28 -> s = "s"
                29 -> s = "t"
                30 -> s = "u"
                31 -> s = "v"
                32 -> s = "w"
                33 -> s = "l"
                34 -> s = "x"
                35 -> s = "y"
                36 -> s = "z"
                37 -> s = "A"
                38 -> s = "B"
                39 -> s = "C"
                40 -> s = "D"
                41 -> s = "E"
                42 -> s = "F"
                43 -> s = "G"
                44 -> s = "H"
                45 -> s = "I"
                46 -> s = "L"
                47 -> s = "J"
                48 -> s = "K"
                49 -> s = "M"
                50 -> s = "N"
                51 -> s = "O"
                52 -> s = "P"
                53 -> s = "Q"
                54 -> s = "R"
                55 -> s = "S"
                56 -> s = "T"
                57 -> s = "U"
                58 -> s = "V"
                59 -> s = "W"
                60 -> s = "X"
                61 -> s = "Y"
                62 -> s = "Z"
            }
            i++
            str = s + str
        }
        return str
    }

    /**
     * 获取本机的号码信息
     *
     * @param context
     * @return 号码和运营商信息
     */
    @SuppressLint("HardwareIds")
    fun getSelfPhoneInfo(context: Context): Map<String, String?>? {
        val telephonyManager = context
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var ProvidersName: String? = null
        // 返回唯一的用户ID;就是这张卡的编号神马的
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null
        }
        val IMSI = telephonyManager.subscriberId
        // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
            ProvidersName = "中国移动"
        } else if (IMSI.startsWith("46001")) {
            ProvidersName = "中国联通"
        } else if (IMSI.startsWith("46003")) {
            ProvidersName = "中国电信"
        }
        // 返回
        val map: MutableMap<String, String?> = HashMap()
        map["phonenum"] = telephonyManager.line1Number // //号码
        map["type"] = ProvidersName // 运营商
        return map
    }

    /**
     * 读取通讯录 获取 电话号码和名称
     *
     * @param context
     * @return
     */
    fun getPhoneAddressBook(
        context: Context
    ): ArrayList<Map<String, String>> {
        /** 获取库Phon表字段  */
        val PHONES_PROJECTION = arrayOf(Phone.DISPLAY_NAME,
            Phone.NUMBER, CommonDataKinds.Photo.PHOTO_ID, Phone.CONTACT_ID)
        /** 联系人显示名称  */
        val PHONES_DISPLAY_NAME_INDEX = 0
        /** 电话号码  */
        val PHONES_NUMBER_INDEX = 1
        /** 联系人信息列  */
        val mContacts = ArrayList<Map<String, String>>()
        var phoneNumber: String // 电话号码
        var contactName: String // 名字
        val resolver = context.contentResolver // 初始数据连接
        // 获取Sims卡联系人
        val uri = Uri.parse("content://icc/adn")
        var phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
            null)
        var map: MutableMap<String, String> // 字段集
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                map = HashMap()
                // 得到手机号码
                phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX)
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber)) {
                    map["phonenum"] = phoneNumber
                    continue
                }
                // 得到联系人名称
                contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX)
                map["name"] = contactName
                // Sim卡中没有联系人头像
                mContacts.add(map)
            }
        }
        // 获取手机联系人
        phoneCursor = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION,
            null, null, null)
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                map = HashMap()
                // 得到手机号码
                phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX)
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber)) {
                    map["phonenum"] = phoneNumber
                    continue
                }
                // 得到联系人名称
                contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX)
                map["name"] = contactName
            }
            phoneCursor.close() // 关闭数据连接
        }
        return mContacts
    }

    /**
     * 把drawable转成bitmap
     *
     * @param drawable
     * @return bitmap
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val width = drawable.intrinsicWidth // 取drawable的长宽
        val height = drawable.intrinsicHeight
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565 // 取drawable的颜色格式
        val bitmap = Bitmap.createBitmap(width, height, config) // 建立对应bitmap
        val canvas = Canvas(bitmap) // 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas) // 把drawable内容画到画布中
        return bitmap
    }

    /**
     * 获取屏幕大小
     *
     *
     * 1是宽 2是高
     */
    fun getScreenWH(context: Context): IntArray? {
        if (wh != null && wh!![0] != 0 && wh!![1] != 0) {
            return wh
        }
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay
            .getMetrics(displayMetrics)
        var width = 0
        var height = 0
        width = displayMetrics.widthPixels
        height = displayMetrics.heightPixels - getStatusBarHeight(context) // 去掉通知栏的高度
        val `is` = intArrayOf(width, height)
        wh = `is`
        return `is`
    }

    /**
     * 打开网站服务条款
     */
    fun openWebText(activity: Context?, textView: TextView, onClickListener: View.OnClickListener) {
        textView.movementMethod = LinkMovementMethod.getInstance()
        val text = textView.text
        if (text is Spannable) {
            val end = text.length
            val sp = textView.text as Spannable
            val urls = sp.getSpans(0, end, URLSpan::class.java)
            val style = SpannableStringBuilder(text)
            style.clearSpans() // should clear old spans
            for (url in urls) {
                val myURLSpan = MyURLSpan(url.url, activity,
                    null, onClickListener)
                style.setSpan(myURLSpan, sp.getSpanStart(url),
                    sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val redSpan = ForegroundColorSpan(Color.RED)
                style.setSpan(redSpan, sp.getSpanStart(url),
                    sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = style
        }
    }

    /**
     * 打开网站服务条款
     */
    fun openWebText(
        activity: Context?, textView: TextView,
        title: String?, onClickListener: View.OnClickListener
    ) {
        textView.movementMethod = LinkMovementMethod.getInstance()
        val text = textView.text
        if (text is Spannable) {
            val end = text.length
            val sp = textView.text as Spannable
            val urls = sp.getSpans(0, end, URLSpan::class.java)
            val style = SpannableStringBuilder(text)
            style.clearSpans() // should clear old spans
            for (url in urls) {
                val myURLSpan = MyURLSpan(url.url, activity,
                    title, onClickListener)
                style.setSpan(myURLSpan, sp.getSpanStart(url),
                    sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val redSpan = ForegroundColorSpan(Color.RED)
                style.setSpan(redSpan, sp.getSpanStart(url),
                    sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = style
        }
    }

    /**
     * 打开文本html链接的处理
     */
    fun openWebText(
        activity: Context?, textView: TextView,
        mSpan: ClickableSpan?
    ) {
        textView.movementMethod = LinkMovementMethod.getInstance()
        val text = textView.text
        if (text is Spannable) {
            val end = text.length
            val sp = textView.text as Spannable
            val urls = sp.getSpans(0, end, URLSpan::class.java)
            val style = SpannableStringBuilder(text)
            style.clearSpans() // should clear old spans
            for (url in urls) {
                style.setSpan(mSpan, sp.getSpanStart(url), sp.getSpanEnd(url),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val redSpan = ForegroundColorSpan(Color.RED)
                style.setSpan(redSpan, sp.getSpanStart(url),
                    sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = style
        }
    }

    /**
     * 修改textview的字体颜色
     *
     * @param start 开始位置
     * @param end   结束位置
     * @param color 颜色
     */
    fun changTextViewColor(
        textView: TextView, start: Int,
        end: Int, color: Int
    ) {
        val builder = SpannableStringBuilder(textView
            .text.toString())
        // ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
        val redSpan = ForegroundColorSpan(color)
        builder.setSpan(redSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = builder
    }

    /**
     * 根据屏幕缩放
     *
     * @param activity
     * @param view     操作的view
     * @param sw       按屏幕大小缩放的宽
     * @param sh       按屏幕大小缩放的高
     */
    fun zoomView(activity: Activity, view: View, sw: Int, sh: Int) {
        view.layoutParams = LinearLayout.LayoutParams(
            getScreenWH(activity)!![0] / sw, getScreenWH(activity)!![1] / sh)
    }

    /**
     * 判断手机号是否正确
     *
     * @param mobiles
     * @return
     */
    fun isMobileNum(mobiles: String): Boolean {
        val p = Pattern
            .compile("^((\\+86)|(86))?1(3[0-9]|7[0-9]|8[0-9]|47|5[0-3]|5[5-9])\\d{8}$")
        val m = p.matcher(mobiles)
        return m.matches()
    }

    /**
     * 判断密码是否符合标准
     *
     * @param mobiles
     * @return
     */
    fun isPwdNum(mobiles: String): Boolean {
        val p = Pattern
            .compile("^(?:([a-z])|([A-Z])|([0-9])|(.)){6,}|(.)+$")
        val m = p.matcher(mobiles)
        return m.matches()
    }

    /**
     * 判断是否全是数字
     * mobiles
     *
     * @return
     */
    fun isNum(str: String): Boolean {
        val p = Pattern
            .compile("^[0-9]*$")
        val m = p.matcher(str)
        return m.matches()
    }

    /**
     * 判断是否全是字母
     * mobiles
     *
     * @return
     */
    fun isAbc(str: String): Boolean {
        val p = Pattern
            .compile("[a-zA-Z]")
        val m = p.matcher(str)
        return m.matches()
    }

    /**
     * 是否是email格式
     *
     * @param mobiles
     * @return
     */
    fun isEmailNum(mobiles: String): Boolean {
        val p = Pattern
            .compile("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$")
        val m = p.matcher(mobiles)
        return m.matches()
    }

    /**
     * 隐藏字符串
     *
     * @param content
     * @return
     */
    fun hintStr(content: String): String? {
        if (content == "" || content == null) {
            return content
        }
        var str: String? = null
        str = if (content.length in 4..6) {
            "***" + content.substring(3)
        } else if (content.length <= 3) {
            "*" + content.substring(1)
        } else if (content.length in 7..10) {
            "******" + content.substring(6)
        } else  {
            "**********" + content.substring(9)
        }
        return str
    }

    /**
     * 如果缓存的图片过大久删除缓存文件
     */
    fun cleanCancle(context: Context) {
        try {
//			AbLogUtil.d(context, getFolderSize(new File(AbFileUtil.getCacheDownloadDir(context)))+"当前缓存的容量");
            AbFileUtil.deleteFile(AbFileUtil.getCacheDownloadDir(context)
                ?.let { File(it) })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long 单位为M
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getFolderSize(file: File): Long {
        var size: Long = 0
        val fileList = file.listFiles()
        if (fileList != null) {
            for (i in fileList.indices) {
                size = if (fileList[i].isDirectory) {
                    size + getFolderSize(fileList[i])
                } else {
                    size + fileList[i].length()
                }
            }
        }
        return size / (1024 * 1024)
    }

    /**
     * 计算出该TextView中文字的长度(像素)
     */
    fun getTextViewLength(textView: TextView, text: String): Float {
        val paint = textView.paint
        // 得到使用该paint写上text的时候,像素为多少
        return paint.measureText(text)
    }

    /**
     * 获取sdk的版本号
     */
    val SDKCode: Int
        get() {
            val osVersion: Int = try {
                Integer.valueOf(VERSION.SDK)
            } catch (e: NumberFormatException) {
                0
            }
            return osVersion
        }

    /**
     * 从相册得到的url转换为SD卡中图片路径 兼容4.4
     */
    @SuppressLint("NewApi")
    fun getPath(uri: Uri, context: Context): String? {
        val isKitKat = VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                null)
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * asset 转 string
     */
    fun assetToString(context: Context, assetName: String?): String {
        val stringBuilder = StringBuilder()
        try {
            val assetManager = context.assets
            val bf = BufferedReader(InputStreamReader(
                assetManager.open(assetName!!)))
            var line: String?
            while (bf.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    /**
     * 处理textview点击超文本的方法
     *
     * @author 王军
     * @date 2014年12月26日
     * @版本 1.0
     */
    class MyURLSpan internal constructor(
        private val mUrl: String, private val activity: Context?, // 标题
        private val title: String?, private val onClickListener: View.OnClickListener
    ) : ClickableSpan() {
        override fun onClick(widget: View) {
            onClickListener.onClick(widget)
        }
    }
}