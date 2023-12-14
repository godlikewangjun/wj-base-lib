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

import okio.appendingSink
import okio.buffer
import okio.source
import okio.sink
import okhttp3.internal.closeQuietly
import android.graphics.Bitmap
import android.content.Context
import java.io.IOException
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import java.lang.StringBuffer
import java.util.HashMap
import android.os.Environment
import java.lang.StringBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import android.content.Intent
import android.os.Build
import java.io.InputStream
import java.lang.Thread
import java.util.Comparator
import java.io.FileInputStream
import java.io.OutputStream
import java.io.DataInputStream
import java.io.ByteArrayInputStream
import okio.BufferedSink
import okio.BufferedSource
import java.io.FileNotFoundException
import java.net.URL
import java.net.HttpURLConnection
import java.util.Locale
import android.os.StatFs
import java.nio.charset.Charset
import androidx.core.content.FileProvider
import okhttp3.internal.and
import java.util.regex.Pattern

/**
 */
class AbFileUtil {
    /**
     * 根据文件的最后修改时间进行排序.
     */
    class FileLastModifSort : Comparator<File> {
        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        override fun compare(arg0: File, arg1: File): Int {
            return if (arg0.lastModified() > arg1.lastModified()) {
                1
            } else if (arg0.lastModified() == arg1.lastModified()) {
                0
            } else {
                -1
            }
        }
    }

    /**
     * 拷贝文件
     */
    fun copyFile(fromFile: String?, toFile: String?): Int {
        return try {
            val inputStream: InputStream = FileInputStream(fromFile)
            val outputStream: OutputStream = FileOutputStream(toFile)
            val bt = ByteArray(1024)
            var d: Int
            while (inputStream.read(bt).also { d = it } > 0) {
                outputStream.write(bt, 0, d)
            }
            inputStream.close()
            outputStream.close()
            0
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * 复制文件夹
     * @param fromFile
     * @param toFile
     * @return
     */
    fun copyFolder(fromFile: String?, toFile: String): Int {
        //要复制的文件目录
        val fromList: Array<File>
        val file = fromFile?.let { File(it) }
        //判断文件是否存在
        if (!file!!.exists()) {
            return -1
        }
        //如果存在则获取当前目录下的所有文件，填充数组
        fromList = file.listFiles()!!
        //目标目录
        val toList = File(toFile)
        //创建目录
        if (!toList.exists()) {
            toList.mkdirs()
        }
        //遍历要复制的全部文件
        for (i in fromList.indices) {
            if (fromList[i].isDirectory) { //如果当前项为子目录，进行递归
                copyFolder(fromList[i].path + "/", toFile + "/ " + fromList[i].name + "/")
            } else { //如果当前项为文件则进行拷贝
                copyFile(fromList[i].path, toFile + fromList[i].name)
            }
        }
        return 0
    }

    companion object {
        /**
         * 默认APP根目录.
         */
        private var downloadRootDir: String? = null

        /**
         * 默认下载图片文件目录.
         */
        private var imageDownloadDir: String? = null

        /**
         * 默认下载文件目录.
         */
        private var fileDownloadDir: String? = null

        /**
         * 默认缓存目录.
         */
        private var cacheDownloadDir: String? = null

        /**
         * 默认下载数据库文件的目录.
         */
        private var dbDownloadDir: String? = null

        /**
         * 默认下载数据库文件的目录.
         */
        private var logloadDir: String? = null
        /**
         * Gets the free sd space needed to cache.
         *
         * @return the free sd space needed to cache
         */
        /**
         * 剩余空间大于200M才使用SD缓存.
         */
        const val freeSdSpaceNeededToCache = 200 * 1024 * 1024

        /**
         * 写入文件默认的文件夹
         *
         * @return
         */
        fun writeData(filename1: String, data: String?): Boolean {
            var filename = filename1
            filename += ".txt"
            var fos: FileOutputStream? = null
            var dis: DataInputStream? = null
            var bis: ByteArrayInputStream? = null
            var file: File?
            try {
                if (data != null) {
                    file = File(cacheDownloadDir + "/" + filename)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    fos = FileOutputStream(file)
                    var readLength: Int
                    bis = ByteArrayInputStream(data.toByteArray())
                    dis = DataInputStream(bis)
                    val buffer = ByteArray(1024)
                    while (dis.read(buffer).also { readLength = it } != -1) {
                        fos.write(buffer, 0, readLength)
                    }
                    fos.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (dis != null) {
                    try {
                        dis.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (bis != null) {
                    try {
                        bis.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return true
        }

        /**
         * 写入文件默认的文件夹
         *
         * @return
         */
        fun writeAppend(filename1: String, data: String?): Boolean {
            var filename = filename1
            filename += ".txt"
            var file: File?
            var bufferedSink: BufferedSink? = null
            val bufferedSource: BufferedSource? = null
            try {
                if (data != null) {
                    file = File(logloadDir + "/" + filename)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    bufferedSink = file.appendingSink().buffer()
                    bufferedSink.writeUtf8(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    bufferedSink?.close()
                    bufferedSource?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return true
        }

        /**
         * 读取文件
         */
        fun redData(filename: String): String? {
            val file = File(cacheDownloadDir + filename)
            var reader: InputStreamReader? = null
            try {
                // SD卡是否存在
                if (!isCanUseSD) {
                    return null
                }
                // 文件是否存在
                if (!file.exists()) {
                    return null
                }
                // 文件存在
                val fileInputStream = FileInputStream(file)
                // 一次读多个字符
                val tempchars = CharArray(30)
                var charread = 0
                reader = InputStreamReader(fileInputStream)
                // 读入多个字符到字符数组中，charread为一次读取字符数
                while (reader.read(tempchars).also { charread = it } != -1) {
                    // 同样屏蔽掉\r不显示
                    if (charread != tempchars.size
                        && tempchars[tempchars.size - 1] == '\r'
                    ) {
                        for (i in 0 until charread) {
                            if (tempchars[i] == '\r') {
                                continue
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return filename
        }

        /**
         * 获取文件大小
         *
         * @return 返回kb
         */
        fun getFileSize(file: File?): Long {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
                return fis.channel.size() / 1024
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return -1
        }

        /**
         * 描述：通过文件的本地地址从SD卡读取图片.
         *
         * @param file          the file
         * @param type          图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类） 如果设置为原图，则后边参数无效，得到原图
         * @param desiredWidth  新图片的宽
         * @param desiredHeight 新图片的高
         * @return Bitmap 新图片
         */
        fun getBitmapFromSD(
            file: File, type: Int, desiredWidth: Int,
            desiredHeight: Int
        ): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                // SD卡是否存在
                if (!isCanUseSD) {
                    return null
                }

                // 文件是否存在
                if (!file.exists()) {
                    return null
                }

                // 文件存在
                bitmap = when (type) {
                    AbImageUtil.CUTIMG -> {
                        AbImageUtil.getCutBitmap(
                            file, desiredWidth,
                            desiredHeight
                        )
                    }
                    AbImageUtil.SCALEIMG -> {
                        AbImageUtil.getScaleBitmap(
                            file, desiredWidth,
                            desiredHeight
                        )
                    }
                    else -> {
                        AbImageUtil.getBitmap(file)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        /**
         * 描述：通过文件的本地地址从SD卡读取图片.
         *
         * @param file the file
         * 图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类） 如果设置为原图，则后边参数无效，得到原图
         * 新图片的宽
         * 新图片的高
         * @return Bitmap 新图片
         */
        fun getBitmapFromSD(file: File, scale: Float): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                // SD卡是否存在
                if (!isCanUseSD) {
                    return null
                }

                // 文件是否存在
                if (!file.exists()) {
                    return null
                }

                // 文件存在
                bitmap = AbImageUtil.getScaleBitmap(file, scale)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        /**
         * 描述：通过文件的本地地址从SD卡读取图片.
         *
         * @param file the file
         * @return Bitmap 图片
         */
        fun getBitmapFromSD(file: File): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                // SD卡是否存在
                if (!isCanUseSD) {
                    return null
                }
                // 文件是否存在
                if (!file.exists()) {
                    return null
                }
                // 文件存在
                bitmap = AbImageUtil.getBitmap(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        /**
         * 描述：将图片的byte[]写入本地文件.
         *
         * @param imgByte       图片的byte[]形势
         * @param fileName      文件名称，需要包含后缀，如.jpg
         * @param type          图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
         * @param desiredWidth  新图片的宽
         * @param desiredHeight 新图片的高
         * @return Bitmap 新图片
         */
        fun getBitmapFromByte(
            imgByte: ByteArray?, fileName: String,
            type: Int, desiredWidth: Int, desiredHeight: Int
        ): Bitmap? {
            var fos: FileOutputStream? = null
            var dis: DataInputStream? = null
            var bis: ByteArrayInputStream? = null
            var bitmap: Bitmap? = null
            val file: File?
            try {
                if (imgByte != null) {
                    file = File(imageDownloadDir + fileName)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    fos = FileOutputStream(file)
                    var readLength: Int
                    bis = ByteArrayInputStream(imgByte)
                    dis = DataInputStream(bis)
                    val buffer = ByteArray(1024)
                    while (dis.read(buffer).also { readLength = it } != -1) {
                        fos.write(buffer, 0, readLength)
                        try {
                            Thread.sleep(500)
                        } catch (e: Exception) {
                        }
                    }
                    fos.flush()
                    bitmap = getBitmapFromSD(
                        file, type, desiredWidth,
                        desiredHeight
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (dis != null) {
                    try {
                        dis.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (bis != null) {
                    try {
                        bis.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return bitmap
        }

        /**
         * 描述：获取src中的图片资源.
         *
         * @param src 图片的src路径，如（“image/arrow.png”）
         * @return Bitmap 图片
         */
        fun getBitmapFromSrc(src: String?): Bitmap? {
            var bit: Bitmap? = null
            try {
                bit = BitmapFactory.decodeStream(
                    src?.let {
                        AbFileUtil::class.java
                            .getResourceAsStream(it)
                    }
                )
            } catch (e: Exception) {
                AbLogUtil.d(AbFileUtil::class.java, "获取图片异常：" + e.message)
            }
            return bit
        }

        /**
         * 描述：获取Asset中的图片资源.
         *
         * @param context  the context
         * @param fileName the file name
         * @return Bitmap 图片
         */
        fun getBitmapFromAsset(context: Context, fileName: String?): Bitmap? {
            var bit: Bitmap? = null
            try {
                val assetManager = context.assets
                val `is` = assetManager.open(fileName!!)
                bit = BitmapFactory.decodeStream(`is`)
            } catch (e: Exception) {
                AbLogUtil.d(AbFileUtil::class.java, "获取图片异常：" + e.message)
            }
            return bit
        }

        /**
         * 描述：获取Asset中的图片资源.
         *
         * @param context  the context
         * @param fileName the file name
         * @return Drawable 图片
         */
        fun getDrawableFromAsset(context: Context, fileName: String?): Drawable? {
            var drawable: Drawable? = null
            try {
                val assetManager = context.assets
                val `is` = assetManager.open(fileName!!)
                drawable = Drawable.createFromStream(`is`, null)
            } catch (e: Exception) {
                AbLogUtil.d(AbFileUtil::class.java, "获取图片异常：" + e.message)
            }
            return drawable
        }

        /**
         * 描述：获取网络文件的大小.
         *
         * @param Url 图片的网络路径
         * @return int 网络文件的大小
         */
        fun getContentLengthFromUrl(Url: String?): Int {
            var mContentLength = 0
            try {
                val url = URL(Url)
                val mHttpURLConnection = url
                    .openConnection() as HttpURLConnection
                mHttpURLConnection.connectTimeout = 5 * 1000
                mHttpURLConnection.requestMethod = "GET"
                mHttpURLConnection
                    .setRequestProperty(
                        "Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*"
                    )
                mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN")
                mHttpURLConnection.setRequestProperty("Referer", Url)
                mHttpURLConnection.setRequestProperty("Charset", "UTF-8")
                mHttpURLConnection
                    .setRequestProperty(
                        "User-Agent",
                        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"
                    )
                mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive")
                mHttpURLConnection.connect()
                if (mHttpURLConnection.responseCode == 200) {
                    // 根据响应获取文件大小
                    mContentLength = mHttpURLConnection.contentLength
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AbLogUtil.d(AbFileUtil::class.java, "获取长度异常：" + e.message)
            }
            return mContentLength
        }

        /**
         * 获取文件名，通过网络获取.
         *
         * @param url 文件地址
         * @return 文件名
         */
        fun getRealFileNameFromUrl(url: String?): String? {
            val name: String? = null
            try {
                if (AbStrUtil.isEmpty(url)) {
                    return name
                }
                val mUrl = URL(url)
                val mHttpURLConnection = mUrl
                    .openConnection() as HttpURLConnection
                mHttpURLConnection.connectTimeout = 5 * 1000
                mHttpURLConnection.requestMethod = "GET"
                mHttpURLConnection
                    .setRequestProperty(
                        "Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*"
                    )
                mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN")
                mHttpURLConnection.setRequestProperty("Referer", url)
                mHttpURLConnection.setRequestProperty("Charset", "UTF-8")
                mHttpURLConnection.setRequestProperty("User-Agent", "")
                mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive")
                mHttpURLConnection.connect()
                if (mHttpURLConnection.responseCode == 200) {
                    var i = 0
                    while (true) {
                        val mine = mHttpURLConnection.getHeaderField(i) ?: break
                        if ("content-disposition" == mHttpURLConnection
                                .getHeaderFieldKey(i).lowercase(Locale.getDefault())
                        ) {
                            val m = Pattern.compile(".*filename=(.*)").matcher(
                                mine.lowercase(Locale.getDefault())
                            )
                            if (m.find()) return m.group(1).replace("\"", "")
                        }
                        i++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AbLogUtil.e(AbFileUtil::class.java, "网络上获取文件名失败")
            }
            return name
        }

        /**
         * Java文件操作 获取文件扩展名
         * @param filename
         * @return
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
         * @param filename
         * @return
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

        // 缓存文件头信息-文件头信息
        val mFileTypes = HashMap<String?, String>()

        init {
            // images
            mFileTypes["FFD8FF"] = "jpg"
            mFileTypes["FFD8FFE0"] = "jpg"
            mFileTypes["89504E47"] = "png"
            mFileTypes["47494638"] = "gif"
            mFileTypes["49492A00"] = "tif"
            mFileTypes["424D"] = "bmp"
            //
            mFileTypes["504B0304"] = "apk" // apk
            mFileTypes["41433130"] = "dwg" // CAD
            mFileTypes["38425053"] = "psd"
            mFileTypes["7B5C727466"] = "rtf" // 日记本
            mFileTypes["3C3F786D6C"] = "xml"
            mFileTypes["68746D6C3E"] = "html"
            mFileTypes["44656C69766572792D646174653A"] = "eml" // 邮件
            mFileTypes["D0CF11E0"] = "doc"
            mFileTypes["D0CF11E0"] = "xls" //excel2003版本文件
            mFileTypes["5374616E64617264204A"] = "mdb"
            mFileTypes["252150532D41646F6265"] = "ps"
            mFileTypes["255044462D312E"] = "pdf"
            mFileTypes["52617221"] = "rar"
            mFileTypes["57415645"] = "wav"
            mFileTypes["41564920"] = "avi"
            mFileTypes["2E524D46"] = "rm"
            mFileTypes["000001BA"] = "mpg"
            mFileTypes["000001B3"] = "mpg"
            mFileTypes["6D6F6F76"] = "mov"
            mFileTypes["3026B2758E66CF11"] = "asf"
            mFileTypes["4D546864"] = "mid"
            mFileTypes["1F8B08"] = "gz"
        }

        /**
         * @author guoxk
         *
         * 方法描述：根据文件路径获取文件头信息
         * @param filePath 文件路径
         * @return 文件头信息
         */
        fun getFileType(filePath: String?): String? {
            //获取文件的头信息
            var `is`: FileInputStream? = null
            var value: String? = null
            try {
                `is` = FileInputStream(filePath)
                val b = ByteArray(4)
                /*
             * int read() 从此输入流中读取一个数据字节。int read(byte[] b) 从此输入流中将最多 b.length
             * 个字节的数据读入一个 byte 数组中。 int read(byte[] b, int off, int len)
             * 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
             */`is`.read(b, 0, b.size)
                value = bytesToHexString(b)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (null != `is`) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return mFileTypes[value]
        }

        /**
         * @author guoxk
         *
         * 方法描述：根据文件路径获取文件头信息
         * @return 文件头信息
         */
        fun getFileType(file: File?): String? {
            //获取文件的头信息
            var `is`: FileInputStream? = null
            var value: String? = null
            try {
                `is` = FileInputStream(file)
                val b = ByteArray(4)
                /*
             * int read() 从此输入流中读取一个数据字节。int read(byte[] b) 从此输入流中将最多 b.length
             * 个字节的数据读入一个 byte 数组中。 int read(byte[] b, int off, int len)
             * 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
             */`is`.read(b, 0, b.size)
                value = bytesToHexString(b)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (null != `is`) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return mFileTypes[value]
        }

        /**
         * 利用系统文件管理器打开
         * @param context
         */
        fun openLocalDir(context: Context, file: File?) {
            //调用系统文件管理器打开指定路径目录
            //获取到指定文件夹，这里为：/storage/emulated/0/Android/data/你的包	名/files/Download
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            val uri =
                FileProvider.getUriForFile(context, context.packageName + ".fileProvider", file!!)
            intent.data = uri
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        /**
         * @author guoxk
         *
         * 方法描述：将要读取文件头信息的文件的byte数组转换成string类型表示
         * @param src 要读取文件头信息的文件的byte数组
         * @return   文件头信息
         */
        private fun bytesToHexString(src: ByteArray?): String? {
            val builder = StringBuilder()
            if (src == null || src.size <= 0) {
                return null
            }
            var hv: String
            for (i in src.indices) {
                // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
                hv = Integer.toHexString(src[i] and 0xFF).uppercase(Locale.getDefault())
                if (hv.length < 2) {
                    builder.append(0)
                }
                builder.append(hv)
            }
            return builder.toString()
        }

        /**
         * 描述：SD卡是否能用.
         *
         * @return true 可用,false不可用
         */
        val isCanUseSD: Boolean
            get() {
                try {
                    return Environment.getExternalStorageState() ==
                            Environment.MEDIA_MOUNTED
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }

        /**
         * 描述：初始化存储目录.
         *
         * @param context the context
         */
        fun initFileDir(context: Context) {


            // 默认下载文件根目录.
            val cacheDir = (File.separator
                    + context.packageName + File.separator)
            // 默认下载图片文件目录.
            val imageDownloadPath = (cacheDir
                    + AbAppConfig.DOWNLOAD_IMAGE_DIR + File.separator)

            // 默认下载文件目录.
            val fileDownloadPath = (cacheDir
                    + AbAppConfig.DOWNLOAD_FILE_DIR + File.separator)

            // 默认缓存目录.
            val cacheDownloadPath = (cacheDir + AbAppConfig.CACHE_DIR
                    + File.separator)

            // 默认DB目录.
            val dbDownloadPath = (cacheDir + AbAppConfig.DB_DIR
                    + File.separator)

            // 默认Log目录.
            val logDownloadPath = (cacheDir + AbAppConfig.Log_DIR
                    + File.separator)
            try {
                if (!isCanUseSD) {
                    return
                } else {
                    val root =
                        Environment.getExternalStorageDirectory() //获取外置sdcasrd的路径
                    val downloadDir = File(
                        root.absolutePath
                                + cacheDir
                    )
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs()
                    }
                    downloadRootDir = downloadDir.path
                    val cacheDownloadDirFile = File(
                        root.absolutePath
                                + cacheDownloadPath
                    )
                    if (!cacheDownloadDirFile.exists()) {
                        cacheDownloadDirFile.mkdirs()
                    }
                    cacheDownloadDir = cacheDownloadDirFile.path
                    val imageDownloadDirFile = File(
                        root.absolutePath
                                + imageDownloadPath
                    )
                    if (!imageDownloadDirFile.exists()) {
                        imageDownloadDirFile.mkdirs()
                    }
                    imageDownloadDir = imageDownloadDirFile.path
                    val fileDownloadDirFile = File(
                        root.absolutePath
                                + fileDownloadPath
                    )
                    if (!fileDownloadDirFile.exists()) {
                        fileDownloadDirFile.mkdirs()
                    }
                    fileDownloadDir = fileDownloadDirFile.path
                    val dbDownloadDirFile = File(
                        root.absolutePath
                                + dbDownloadPath
                    )
                    if (!dbDownloadDirFile.exists()) {
                        dbDownloadDirFile.mkdirs()
                    }
                    dbDownloadDir = dbDownloadDirFile.path
                    val logDownloadPathDirFile = File(
                        root.absolutePath
                                + logDownloadPath
                    )
                    if (!logDownloadPathDirFile.exists()) {
                        logDownloadPathDirFile.mkdirs()
                    }
                    logloadDir = logDownloadPathDirFile.path
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 计算sdcard上的剩余空间.
         *
         * @return the int
         */
        fun freeSpaceOnSD(): Int {
            val stat = StatFs(
                Environment.getExternalStorageDirectory()
                    .path
            )
            val sdFreeMB = stat.availableBlocks.toDouble() * stat
                .blockSize.toDouble() / 1024 * 1024
            return sdFreeMB.toInt()
        }

        /**
         * 删除所有缓存文件.
         *
         * @return true, if successful
         */
        fun clearDownloadFile(): Boolean {
            try {
                val fileDirectory = downloadRootDir?.let { File(it) }
                deleteFile(fileDirectory)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        /**
         * 删除文件.
         *
         * @return true, if successful
         */
        fun deleteFile(file: File?): Boolean {
            try {
                if (!isCanUseSD) {
                    return false
                }
                if (file == null) {
                    return true
                }
                if (file.isDirectory) {
                    val files = file.listFiles()
                    if (files != null) {
                        for (i in files.indices) {
                            deleteFile(files[i])
                        }
                    }
                } else {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        /**
         * 描述：读取Assets目录的文件内容.
         *
         * @param context  the context
         * @param name     the name
         * @param encoding the encoding
         * @return the string
         */
        fun readAssetsByName(
            context: Context, name: String?,
            encoding: String?
        ): String? {
            var text: String? = null
            var inputReader: InputStreamReader? = null
            var bufReader: BufferedReader? = null
            try {
                inputReader = InputStreamReader(context.assets.open(name!!))
                bufReader = BufferedReader(inputReader)
                var line: String?
                val buffer = StringBuffer()
                while (bufReader.readLine().also { line = it } != null) {
                    buffer.append(line)
                }
                text = String(buffer.toString().toByteArray(), Charset.forName(encoding))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    bufReader?.close()
                    inputReader?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return text
        }

        /**
         * 描述：读取Raw目录的文件内容.
         *
         * @param context  the context
         * @param id       the id
         * @param encoding the encoding
         * @return the string
         */
        fun readRawByName(context: Context, id: Int, encoding: String?): String? {
            var text: String? = null
            var inputReader: InputStreamReader? = null
            var bufReader: BufferedReader? = null
            try {
                inputReader = InputStreamReader(
                    context.resources
                        .openRawResource(id)
                )
                bufReader = BufferedReader(inputReader)
                var line: String?
                val buffer = StringBuffer()
                while (bufReader.readLine().also { line = it } != null) {
                    buffer.append(line)
                }
                text = String(buffer.toString().toByteArray(), Charset.forName(encoding))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    bufReader?.close()
                    inputReader?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return text
        }

        /**
         * Gets the download root dir.
         *
         * @param context the context
         * @return the download root dir
         */
        fun getDownloadRootDir(context: Context): String? {
            if (downloadRootDir == null) {
                initFileDir(context)
            }
            return downloadRootDir
        }

        /**
         * Gets the image download dir.
         *
         * @param context the context
         * @return the image download dir
         */
        fun getImageDownloadDir(context: Context): String? {
            if (downloadRootDir == null) {
                initFileDir(context)
            }
            return imageDownloadDir
        }

        /**
         * Gets the file download dir.
         *
         * @param context the context
         * @return the file download dir
         */
        fun getFileDownloadDir(context: Context): String? {
            if (downloadRootDir == null) {
                initFileDir(context)
            }
            return fileDownloadDir
        }

        /**
         * Gets the cache download dir.
         *
         * @param context the context
         * @return the cache download dir
         */
        fun getCacheDownloadDir(context: Context): String? {
            if (downloadRootDir == null) {
                initFileDir(context)
            }
            return cacheDownloadDir
        }

        /**
         * Gets the db download dir.
         *
         * @param context the context
         * @return the db download dir
         */
        fun getDbDownloadDir(context: Context): String? {
            if (downloadRootDir == null) {
                initFileDir(context)
            }
            return dbDownloadDir
        }

        /**
         * Gets the download root dir.
         *
         * @param context the context
         * @return the download root dir
         */
        fun getLogRootDir(context: Context): String? {
            if (downloadRootDir == null) {
                initFileDir(context)
            }
            return logloadDir
        }

        /**
         * 读取 okio
         *
         * @return
         */
        fun okReadFile(file: File): String {
            var bufferedSource: BufferedSource? = null
            try {
                bufferedSource = file.source().buffer()
                return bufferedSource.readString(Charset.forName("UTF-8"))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    bufferedSource?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return ""
        }

        /**
         * 读取 okio
         *
         * @return
         */
        fun okReadFile(file: InputStream): String {
            var bufferedSource: BufferedSource? = null
            try {
                bufferedSource = file.source().buffer()
                return bufferedSource.readString(Charset.forName("UTF-8"))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    bufferedSource?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return ""
        }

        /**
         * 写入文件 okio
         *
         * @return
         */
        fun okWriteFile(file: File, content: String?) {
            if (!file.exists()) {
                try {
                    if (file.isFile) {
                        file.createNewFile()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            var bufferedSink: BufferedSink? = null
            try {
                bufferedSink = file.sink().buffer()
                bufferedSink.writeUtf8(content!!)
                bufferedSink.flush()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                bufferedSink?.closeQuietly()
            }
        }
    }
}