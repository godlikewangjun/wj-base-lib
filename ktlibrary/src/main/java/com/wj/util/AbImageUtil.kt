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

import android.content.Context
import java.io.File
import java.lang.Exception
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import android.os.Build
import java.io.InputStream
import kotlin.jvm.JvmOverloads
import java.net.URL
import android.view.View.MeasureSpec
import java.net.URLConnection
import android.widget.ImageView
import android.graphics.drawable.BitmapDrawable
import android.annotation.TargetApi
import android.renderscript.RenderScript
import android.renderscript.RenderScript.RSMessageHandler
import android.renderscript.Allocation
import android.renderscript.ScriptIntrinsicBlur
import android.graphics.Bitmap.CompressFormat
import java.io.ByteArrayOutputStream
import android.graphics.Shader.TileMode
import android.os.Build.VERSION
import android.graphics.*
import android.renderscript.Element

// TODO: Auto-generated Javadoc
/**
 * © 2012 amsoft.cn
 * 名称：AbImageUtil.java
 * 描述：图片处理类.
 *
 * @author 还如一梦中
 * @version v1.0
 * @date：2013-01-17 下午11:52:13
 */
object AbImageUtil {
    /** 图片处理：裁剪.  */
    const val CUTIMG = 0

    /** 图片处理：缩放.  */
    const val SCALEIMG = 1

    /** 图片处理：不处理.  */
    const val ORIGINALIMG = 2

    /** 图片最大宽度.  */
    const val MAX_WIDTH = 4096 / 2

    /** 图片最大高度.  */
    const val MAX_HEIGHT = 4096 / 2

    /**
     * 直接获取互联网上的图片.
     *
     * @param url
     * 要下载文件的网络地址
     * @param type
     * 图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
     * @param desiredWidth
     * 新图片的宽
     * @param desiredHeight
     * 新图片的高
     * @return Bitmap 新图片
     */
    fun getBitmap(
        url: String?, type: Int,
        desiredWidth: Int, desiredHeight: Int
    ): Bitmap? {
        var bm: Bitmap? = null
        var con: URLConnection? = null
        var `is`: InputStream? = null
        try {
            val imageURL = URL(url)
            con = imageURL.openConnection()
            con.doInput = true
            con.connect()
            `is` = con.getInputStream()
            // 获取资源图片
            val wholeBm = BitmapFactory.decodeStream(`is`, null, null)
            bm = if (type == CUTIMG) {
                getCutBitmap(wholeBm, desiredWidth, desiredHeight)
            } else if (type == SCALEIMG) {
                getScaleBitmap(wholeBm, desiredWidth, desiredHeight)
            } else {
                wholeBm
            }
        } catch (e: Exception) {
            AbLogUtil.d(AbImageUtil::class.java, "" + e.message)
        } finally {
            try {
                `is`?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return bm
    }

    /**回收 ImageView的Bitmap */
    fun releaseImageViewResouce(imageView: ImageView?) {
        if (imageView == null) return
        val drawable = imageView.drawable
        if (drawable != null && drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }
    /**
     * 转为圆形图片
     *
     * @param src     源图片
     * @param recycle 是否回收
     * @return 圆形图片
     */
    /**
     * 转为圆形图片
     *
     * @param src 源图片
     * @return 圆形图片
     */
    @JvmOverloads
    fun toRound(src: Bitmap, recycle: Boolean = false): Bitmap {
        val width = src.width
        val height = src.height
        val radius = Math.min(width, height) shr 1
        val ret = Bitmap.createBitmap(width, height, src.config)
        val paint = Paint()
        val canvas = Canvas(ret)
        val rect = Rect(0, 0, width, height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle((width shr 1).toFloat(),
            (height shr 1).toFloat(),
            radius.toFloat(),
            paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, rect, rect, paint)
        if (recycle && !src.isRecycled) src.recycle()
        return ret
    }
    /**
     * 转为圆角图片
     *
     * @param src     源图片
     * @param radius  圆角的度数
     * @param recycle 是否回收
     * @return 圆角图片
     */
    /**
     * 转为圆角图片
     *
     * @param src    源图片
     * @param radius 圆角的度数
     * @return 圆角图片
     */
    @JvmOverloads
    fun toRoundCorner(src: Bitmap?, radius: Float, recycle: Boolean = false): Bitmap? {
        if (null == src) return null
        val width = src.width
        val height = src.height
        val ret = Bitmap.createBitmap(width, height, src.config)
        val paint = Paint()
        val canvas = Canvas(ret)
        val rect = Rect(0, 0, width, height)
        paint.isAntiAlias = true
        canvas.drawRoundRect(RectF(rect), radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, rect, rect, paint)
        if (recycle && !src.isRecycled) src.recycle()
        return ret
    }

    /**
     * renderScript模糊图片
     *
     * API大于17
     *
     * @param context 上下文
     * @param src     源图片
     * @param radius  模糊度(1...25)
     * @return 模糊后的图片
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun renderScriptBlur(context: Context?, src: Bitmap, radius: Float): Bitmap {
        var radius = radius
        var rs: RenderScript? = null
        try {
            rs = RenderScript.create(context)
            rs.messageHandler = RSMessageHandler()
            val input = Allocation.createFromBitmap(rs,
                src,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT)
            val output = Allocation.createTyped(rs, input.type)
            val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            if (radius > 25) {
                radius = 25.0f
            } else if (radius <= 0) {
                radius = 1.0f
            }
            blurScript.setInput(input)
            blurScript.setRadius(radius)
            blurScript.forEach(output)
            output.copyTo(src)
        } finally {
            rs?.destroy()
        }
        return src
    }

    /**
     * 描述：获取原图.
     *
     * @param file
     * File对象
     * @return Bitmap 图片
     */
    fun getBitmap(file: File): Bitmap? {
        var resizeBmp: Bitmap? = null
        try {
            resizeBmp = BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resizeBmp
    }

    /**
     * 描述：缩放图片.压缩
     *
     * @param file
     * File对象
     * @param desiredWidth
     * 新图片的宽
     * @param desiredHeight
     * 新图片的高
     * @return Bitmap 新图片
     */
    fun getScaleBitmap(file: File, desiredWidth: Int, desiredHeight: Int): Bitmap? {
        var desiredWidth = desiredWidth
        var desiredHeight = desiredHeight
        var resizeBmp: Bitmap? = null
        val opts = BitmapFactory.Options()
        // 设置为true,decodeFile先不创建内存 只获取一些解码边界信息即图片大小信息
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.path, opts)

        // 获取图片的原始宽度高度
        val srcWidth = opts.outWidth
        val srcHeight = opts.outHeight
        val size = resizeToMaxSize(srcWidth, srcHeight, desiredWidth, desiredHeight)
        desiredWidth = size[0]
        desiredHeight = size[1]

        // 缩放的比例
        val scale = getMinScale(srcWidth, srcHeight, desiredWidth, desiredHeight)
        var destWidth = srcWidth
        var destHeight = srcHeight
        if (scale != 0f) {
            destWidth = (srcWidth * scale).toInt()
            destHeight = (srcHeight * scale).toInt()
        }

        // 默认为ARGB_8888.
        opts.inPreferredConfig = Bitmap.Config.RGB_565
        // 以下两个字段需一起使用：
        // 产生的位图将得到像素空间，如果系统gc，那么将被清空。当像素再次被访问，如果Bitmap已经decode，那么将被自动重新解码
        opts.inPurgeable = true
        // 位图可以共享一个参考输入数据(inputstream、阵列等)
        opts.inInputShareable = true

        // inSampleSize=2 表示图片宽高都为原来的二分之一，即图片为原来的四分之一
        // 缩放的比例，SDK中建议其值是2的指数值，通过inSampleSize来进行缩放，其值表明缩放的倍数
        if (scale < 0.25) {
            // 缩小到4分之一
            opts.inSampleSize = 2
        } else if (scale < 0.125) {
            // 缩小到8分之一
            opts.inSampleSize = 4
        } else {
            opts.inSampleSize = 1
        }

        // 设置大小
        opts.outWidth = destWidth
        opts.outHeight = destHeight

        // 创建内存
        opts.inJustDecodeBounds = false
        // 使图片不抖动
        opts.inDither = false
        resizeBmp = BitmapFactory.decodeFile(file.path, opts)
        // 缩小或者放大
        resizeBmp = getScaleBitmap(resizeBmp, scale)
        return resizeBmp
    }

    /**
     * 描述：缩放图片.压缩
     *
     * @param file
     * File对象
     * 新图片的宽
     * 新图片的高
     * @return Bitmap 新图片
     */
    fun getScaleBitmap(file: File, scalef: Float): Bitmap? {
        var resizeBmp: Bitmap? = null
        val opts = BitmapFactory.Options()
        // 设置为true,decodeFile先不创建内存 只获取一些解码边界信息即图片大小信息
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.path, opts)

        // 获取图片的原始宽度高度
        val srcWidth = opts.outWidth
        val srcHeight = opts.outHeight
        val size = resizeToMaxSize(srcWidth,
            srcHeight,
            (srcWidth * scalef).toInt(),
            (srcHeight * scalef).toInt())

        // 缩放的比例
        val scale = getMinScale(srcWidth, srcHeight, size[0], size[1])
        var destWidth = srcWidth
        var destHeight = srcHeight
        if (scale != 0f) {
            destWidth = (srcWidth * scale).toInt()
            destHeight = (srcHeight * scale).toInt()
        }

        // 默认为ARGB_8888.
        opts.inPreferredConfig = Bitmap.Config.RGB_565
        // 以下两个字段需一起使用：
        // 产生的位图将得到像素空间，如果系统gc，那么将被清空。当像素再次被访问，如果Bitmap已经decode，那么将被自动重新解码
        opts.inPurgeable = true
        // 位图可以共享一个参考输入数据(inputstream、阵列等)
        opts.inInputShareable = true

        // inSampleSize=2 表示图片宽高都为原来的二分之一，即图片为原来的四分之一
        // 缩放的比例，SDK中建议其值是2的指数值，通过inSampleSize来进行缩放，其值表明缩放的倍数
        if (scale < 0.25) {
            // 缩小到4分之一
            opts.inSampleSize = 2
        } else if (scale < 0.125) {
            // 缩小到8分之一
            opts.inSampleSize = 4
        } else {
            opts.inSampleSize = 1
        }

        // 设置大小
        opts.outWidth = destWidth
        opts.outHeight = destHeight

        // 创建内存
        opts.inJustDecodeBounds = false
        // 使图片不抖动
        opts.inDither = false
        resizeBmp = BitmapFactory.decodeFile(file.path, opts)
        // 缩小或者放大
        resizeBmp = getScaleBitmap(resizeBmp, scale)
        return resizeBmp
    }

    /**
     * 描述：缩放图片,不压缩的缩放.
     *
     * @param bitmap
     * the bitmap
     * @param desiredWidth
     * 新图片的宽
     * @param desiredHeight
     * 新图片的高
     * @return Bitmap 新图片
     */
    fun getScaleBitmap(bitmap: Bitmap?, desiredWidth: Int, desiredHeight: Int): Bitmap? {
        var desiredWidth = desiredWidth
        var desiredHeight = desiredHeight
        var resizeBmp: Bitmap? = null
        try {
            if (!checkBitmap(bitmap)) {
                return null
            }

            // 获得图片的宽高
            val srcWidth = bitmap!!.width
            val srcHeight = bitmap.height
            val size = resizeToMaxSize(srcWidth, srcHeight, desiredWidth, desiredHeight)
            desiredWidth = size[0]
            desiredHeight = size[1]
            val scale = getMinScale(srcWidth, srcHeight, desiredWidth, desiredHeight)
            resizeBmp = getScaleBitmap(bitmap, scale)
            //超出的裁掉
//			if (resizeBmp.getWidth() > desiredWidth || resizeBmp.getHeight() > desiredHeight) {
//				resizeBmp  = getCutBitmap(resizeBmp,desiredWidth,desiredHeight);
//			}
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resizeBmp
    }

    /**
     * 描述：根据等比例缩放图片.
     *
     * @param bitmap
     * the bitmap
     * @param scale
     * 比例
     * @return Bitmap 新图片
     */
    fun getScaleBitmap(bitmap: Bitmap?, scale: Float): Bitmap? {
        if (!checkBitmap(bitmap)) {
            return null
        }
        if (scale == 1f) {
            return bitmap
        }
        var resizeBmp: Bitmap? = null
        try {
            // 获取Bitmap资源的宽和高
            val bmpW = bitmap!!.width
            val bmpH = bitmap.height

            // 注意这个Matirx是android.graphics底下的那个
            val matrix = Matrix()
            // 设置缩放系数，分别为原来的0.8和0.8
            matrix.postScale(scale, scale)
            resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bmpW, bmpH, matrix, true)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (resizeBmp != bitmap) {
                bitmap!!.recycle()
            }
        }
        return resizeBmp
    }

    /**
     * 描述：裁剪图片.
     *
     * @param file
     * File对象
     * @param desiredWidth
     * 新图片的宽
     * @param desiredHeight
     * 新图片的高
     * @return Bitmap 新图片
     */
    fun getCutBitmap(file: File, desiredWidth: Int, desiredHeight: Int): Bitmap? {
        var desiredWidth = desiredWidth
        var desiredHeight = desiredHeight
        var resizeBmp: Bitmap? = null
        val opts = BitmapFactory.Options()
        // 设置为true,decodeFile先不创建内存 只获取一些解码边界信息即图片大小信息
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.path, opts)

        // 获取图片的原始宽度
        val srcWidth = opts.outWidth
        // 获取图片原始高度
        val srcHeight = opts.outHeight
        val size = resizeToMaxSize(srcWidth, srcHeight, desiredWidth, desiredHeight)
        desiredWidth = size[0]
        desiredHeight = size[1]

        // 缩放的比例
        val scale = getMinScale(srcWidth, srcHeight, desiredWidth, desiredHeight)
        var destWidth = srcWidth
        var destHeight = srcHeight
        if (scale != 1f) {
            destWidth = (srcWidth * scale).toInt()
            destHeight = (srcHeight * scale).toInt()
        }

        // 默认为ARGB_8888.
        opts.inPreferredConfig = Bitmap.Config.RGB_565
        // 以下两个字段需一起使用：
        // 产生的位图将得到像素空间，如果系统gc，那么将被清空。当像素再次被访问，如果Bitmap已经decode，那么将被自动重新解码
        opts.inPurgeable = true
        // 位图可以共享一个参考输入数据(inputstream、阵列等)
        opts.inInputShareable = true
        // 缩放的比例，缩放是很难按准备的比例进行缩放的，通过inSampleSize来进行缩放，其值表明缩放的倍数，SDK中建议其值是2的指数值
        if (scale < 0.25) {
            // 缩小到4分之一
            opts.inSampleSize = 2
        } else if (scale < 0.125) {
            // 缩小
            opts.inSampleSize = 4
        } else {
            opts.inSampleSize = 1
        }
        // 设置大小
        opts.outHeight = destHeight
        opts.outWidth = destWidth
        // 创建内存
        opts.inJustDecodeBounds = false
        // 使图片不抖动
        opts.inDither = false
        val bitmap = BitmapFactory.decodeFile(file.path, opts)
        if (bitmap != null) {
            resizeBmp = getCutBitmap(bitmap, desiredWidth, desiredHeight)
        }
        return resizeBmp
    }

    /**
     * 描述：裁剪图片.
     *
     * @param bitmap
     * the bitmap
     * @param desiredWidth
     * 新图片的宽
     * @param desiredHeight
     * 新图片的高
     * @return Bitmap 新图片
     */
    fun getCutBitmap(bitmap: Bitmap?, desiredWidth: Int, desiredHeight: Int): Bitmap? {
        var desiredWidth = desiredWidth
        var desiredHeight = desiredHeight
        if (!checkBitmap(bitmap)) {
            return null
        }
        if (!checkSize(desiredWidth, desiredHeight)) {
            return null
        }
        var resizeBmp: Bitmap? = null
        try {
            val width = bitmap!!.width
            val height = bitmap.height
            var offsetX = 0
            var offsetY = 0
            if (width > desiredWidth) {
                offsetX = (width - desiredWidth) / 2
            } else {
                desiredWidth = width
            }
            if (height > desiredHeight) {
                offsetY = (height - desiredHeight) / 2
            } else {
                desiredHeight = height
            }
            resizeBmp = Bitmap.createBitmap(bitmap, offsetX, offsetY, desiredWidth, desiredHeight)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (resizeBmp != bitmap) {
                bitmap!!.recycle()
            }
        }
        return resizeBmp
    }

    /**
     * 描述：获取图片尺寸
     *
     * @param file File对象
     * @return Bitmap 新图片
     */
    fun getBitmapSize(file: File): FloatArray {
        val size = FloatArray(2)
        val opts = BitmapFactory.Options()
        // 设置为true,decodeFile先不创建内存 只获取一些解码边界信息即图片大小信息
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.path, opts)
        // 获取图片的原始宽度高度
        size[0] = opts.outWidth.toFloat()
        size[1] = opts.outHeight.toFloat()
        return size
    }

    private fun getMinScale(
        srcWidth: Int, srcHeight: Int, desiredWidth: Int,
        desiredHeight: Int
    ): Float {
        // 缩放的比例
        var scale = 0f
        // 计算缩放比例，宽高的最小比例
        val scaleWidth = desiredWidth.toFloat() / srcWidth
        val scaleHeight = desiredHeight.toFloat() / srcHeight
        scale = if (scaleWidth > scaleHeight) {
            scaleWidth
        } else {
            scaleHeight
        }
        return scale
    }

    private fun resizeToMaxSize(
        srcWidth: Int, srcHeight: Int,
        desiredWidth: Int, desiredHeight: Int
    ): IntArray {
        var desiredWidth = desiredWidth
        var desiredHeight = desiredHeight
        val size = IntArray(2)
        if (desiredWidth <= 0) {
            desiredWidth = srcWidth
        }
        if (desiredHeight <= 0) {
            desiredHeight = srcHeight
        }
        if (desiredWidth > MAX_WIDTH) {
            // 重新计算大小
            desiredWidth = MAX_WIDTH
            val scaleWidth = desiredWidth.toFloat() / srcWidth
            desiredHeight = (desiredHeight * scaleWidth).toInt()
        }
        if (desiredHeight > MAX_HEIGHT) {
            // 重新计算大小
            desiredHeight = MAX_HEIGHT
            val scaleHeight = desiredHeight.toFloat() / srcHeight
            desiredWidth = (desiredWidth * scaleHeight).toInt()
        }
        size[0] = desiredWidth
        size[1] = desiredHeight
        return size
    }

    private fun checkBitmap(bitmap: Bitmap?): Boolean {
        if (bitmap == null) {
            AbLogUtil.e(AbImageUtil::class.java, "原图Bitmap为空了")
            return false
        }
        if (bitmap.width <= 0 || bitmap.height <= 0) {
            AbLogUtil.e(AbImageUtil::class.java, "原图Bitmap大小为0")
            return false
        }
        return true
    }

    private fun checkSize(desiredWidth: Int, desiredHeight: Int): Boolean {
        if (desiredWidth <= 0 || desiredHeight <= 0) {
            AbLogUtil.e(AbImageUtil::class.java, "请求Bitmap的宽高参数必须大于0")
            return false
        }
        return true
    }

    /**
     * Drawable转Bitmap.
     *
     * @param drawable
     * 要转化的Drawable
     * @return Bitmap
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = Bitmap
                .createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth,
                drawable.intrinsicHeight)
            drawable.draw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * Bitmap对象转换Drawable对象.
     *
     * @param bitmap
     * 要转化的Bitmap对象
     * @return Drawable 转化完成的Drawable对象
     */
    fun bitmapToDrawable(bitmap: Bitmap?): Drawable? {
        var mBitmapDrawable: BitmapDrawable? = null
        try {
            if (bitmap == null) {
                return null
            }
            mBitmapDrawable = BitmapDrawable(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mBitmapDrawable
    }

    /**
     * 将Bitmap转换为byte[].
     *
     * @param bitmap
     * the bitmap
     * @param mCompressFormat
     * 图片格式 Bitmap.CompressFormat.JPEG,CompressFormat.PNG
     * @param needRecycle
     * 是否需要回收
     * @return byte[] 图片的byte[]
     */
    fun bitmap2Bytes(
        bitmap: Bitmap?,
        mCompressFormat: CompressFormat?, needRecycle: Boolean
    ): ByteArray? {
        var result: ByteArray? = null
        var output: ByteArrayOutputStream? = null
        try {
            output = ByteArrayOutputStream()
            bitmap!!.compress(mCompressFormat, 100, output)
            result = output.toByteArray()
            if (needRecycle) {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (output != null) {
                try {
                    output.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    /**
     * 获取Bitmap大小.
     *
     * @param bitmap
     * the bitmap
     * @param mCompressFormat
     * 图片格式 Bitmap.CompressFormat.JPEG,CompressFormat.PNG
     * @return 图片的大小
     */
    fun getByteCount(
        bitmap: Bitmap,
        mCompressFormat: CompressFormat?
    ): Int {
        var size = 0
        var output: ByteArrayOutputStream? = null
        try {
            output = ByteArrayOutputStream()
            bitmap.compress(mCompressFormat, 100, output)
            var result = output.toByteArray()
            size = result.size
            result = null
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (output != null) {
                try {
                    output.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return size
    }

    /**
     * 描述：将byte[]转换为Bitmap.
     *
     * @param b
     * 图片格式的byte[]数组
     * @return bitmap 得到的Bitmap
     */
    fun bytes2Bimap(b: ByteArray): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            if (b.size != 0) {
                bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 将ImageView转换为Bitmap.
     *
     * @param view
     * 要转换为bitmap的View
     * @return byte[] 图片的byte[]
     */
    fun imageView2Bitmap(view: ImageView): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 将View转换为Drawable.需要最上层布局为Linearlayout
     *
     * @param view
     * 要转换为Drawable的View
     * @return BitmapDrawable Drawable
     */
    fun view2Drawable(view: View?): Drawable? {
        var mBitmapDrawable: BitmapDrawable? = null
        try {
            val newbmp = view2Bitmap(view)
            if (newbmp != null) {
                mBitmapDrawable = BitmapDrawable(newbmp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mBitmapDrawable
    }

    /**
     * 将View转换为Bitmap.需要最上层布局为Linearlayout
     *
     * @param view
     * 要转换为bitmap的View
     * @return byte[] 图片的byte[]
     */
    fun view2Bitmap(view: View?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            if (view != null) {
                view.isDrawingCacheEnabled = true
                view.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                view.layout(0, 0, view.measuredWidth,
                    view.measuredHeight)
                view.buildDrawingCache()
                bitmap = view.drawingCache
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 将View转换为byte[].
     *
     * @param view
     * 要转换为byte[]的View
     * @param compressFormat
     * the compress format
     * @return byte[] View图片的byte[]
     */
    fun view2Bytes(
        view: View?,
        compressFormat: CompressFormat?
    ): ByteArray? {
        var b: ByteArray? = null
        try {
            val bitmap = view2Bitmap(view)
            b = bitmap2Bytes(bitmap, compressFormat, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return b
    }

    /**
     * 描述：旋转Bitmap为一定的角度.
     *
     * @param bitmap
     * the bitmap
     * @param degrees
     * the degrees
     * @return the bitmap
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap? {
        var mBitmap: Bitmap? = null
        try {
            val m = Matrix()
            m.setRotate(degrees % 360)
            mBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width,
                bitmap.height, m, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mBitmap
    }

    /**
     * 描述：旋转Bitmap为一定的角度并四周暗化处理.
     *
     * @param bitmap
     * the bitmap
     * @param degrees
     * the degrees
     * @return the bitmap
     */
    fun rotateBitmapTranslate(bitmap: Bitmap, degrees: Float): Bitmap? {
        val mBitmap: Bitmap? = null
        val width: Int
        val height: Int
        try {
            val matrix = Matrix()
            if (degrees / 90 % 2 != 0f) {
                width = bitmap.width
                height = bitmap.height
            } else {
                width = bitmap.height
                height = bitmap.width
            }
            val cx = width / 2
            val cy = height / 2
            matrix.preTranslate(-cx.toFloat(), -cy.toFloat())
            matrix.postRotate(degrees)
            matrix.postTranslate(cx.toFloat(), cy.toFloat())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mBitmap
    }

    /**
     * 转换图片转换成圆形.
     *
     * @param bitmap
     * 传入Bitmap对象
     * @return the bitmap
     */
    fun toRoundBitmap(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) {
            return null
        }
        var width = bitmap.width
        var height = bitmap.height
        val roundPx: Float
        val left: Float
        val top: Float
        val right: Float
        val bottom: Float
        val dst_left: Float
        val dst_top: Float
        val dst_right: Float
        val dst_bottom: Float
        if (width <= height) {
            roundPx = (width / 2).toFloat()
            top = 0f
            bottom = width.toFloat()
            left = 0f
            right = width.toFloat()
            height = width
            dst_left = 0f
            dst_top = 0f
            dst_right = width.toFloat()
            dst_bottom = width.toFloat()
        } else {
            roundPx = (height / 2).toFloat()
            val clip = ((width - height) / 2).toFloat()
            left = clip
            right = width - clip
            top = 0f
            bottom = height.toFloat()
            width = height
            dst_left = 0f
            dst_top = 0f
            dst_right = height.toFloat()
            dst_bottom = height.toFloat()
        }
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val src = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        val dst = Rect(dst_left.toInt(), dst_top.toInt(), dst_right.toInt(), dst_bottom.toInt())
        val rectF = RectF(dst)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, src, dst, paint)
        return output
    }

    /**
     * 释放Bitmap对象.
     *
     * @param bitmap
     * 要释放的Bitmap
     */
    fun releaseBitmap(bitmap: Bitmap?) {
        var bitmap = bitmap
        if (bitmap != null) {
            try {
                if (!bitmap.isRecycled) {
                    AbLogUtil.d(AbImageUtil::class.java,
                        "Bitmap释放$bitmap")
                    bitmap.recycle()
                }
            } catch (e: Exception) {
            }
            bitmap = null
        }
    }

    /**
     * 释放Bitmap数组.
     *
     * @param bitmaps
     * 要释放的Bitmap数组
     */
    fun releaseBitmapArray(bitmaps: Array<Bitmap?>?) {
        if (bitmaps != null) {
            try {
                for (bitmap in bitmaps) {
                    if (bitmap != null && !bitmap.isRecycled) {
                        AbLogUtil.d(AbImageUtil::class.java,
                            "Bitmap释放$bitmap")
                        bitmap.recycle()
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 描述：图像的特征值颜色分布 将颜色分4个区，0,1,2,3 区组合共64组，计算每个像素点属于哪个区.
     *
     * @param bitmap
     * the bitmap
     * @return the color histogram
     */
    fun getColorHistogram(bitmap: Bitmap): IntArray {
        val width = bitmap.width
        val height = bitmap.height
        // 区颜色分布
        val areaColor = IntArray(64)

        // 获取色彩数组。
        for (i in 0 until width) {
            for (j in 0 until height) {
                val pixels = bitmap.getPixel(i, j)
                val alpha = pixels shr 24 and 0xFF
                val red = pixels shr 16 and 0xFF
                val green = pixels shr 8 and 0xFF
                val blue = pixels and 0xFF
                var redArea = 0
                var greenArea = 0
                var blueArea = 0
                // 0-63 64-127 128-191 192-255
                if (red >= 192) {
                    redArea = 3
                } else if (red >= 128) {
                    redArea = 2
                } else if (red >= 64) {
                    redArea = 1
                } else if (red >= 0) {
                    redArea = 0
                }
                if (green >= 192) {
                    greenArea = 3
                } else if (green >= 128) {
                    greenArea = 2
                } else if (green >= 64) {
                    greenArea = 1
                } else if (green >= 0) {
                    greenArea = 0
                }
                if (blue >= 192) {
                    blueArea = 3
                } else if (blue >= 128) {
                    blueArea = 2
                } else if (blue >= 64) {
                    blueArea = 1
                } else if (blue >= 0) {
                    blueArea = 0
                }
                val index = redArea * 16 + greenArea * 4 + blueArea
                // 加入
                areaColor[index] += 1
            }
        }
        return areaColor
    }

    /**
     * 灰度值计算.
     *
     * @param pixels
     * 像素
     * @return int 灰度值
     */
    private fun rgbToGray(pixels: Int): Int {
        // int _alpha = (pixels >> 24) & 0xFF;
        val _red = pixels shr 16 and 0xFF
        val _green = pixels shr 8 and 0xFF
        val _blue = pixels and 0xFF
        return (0.3 * _red + 0.59 * _green + 0.11 * _blue).toInt()
    }

    /**毛玻璃效果 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun maoBoLi(context: Context?, sentBitmap: Bitmap, radius: Int): Bitmap? {
        val bitmap: Bitmap
        if (VERSION.SDK_INT > 16) {
            bitmap = sentBitmap.copy(sentBitmap.config, true)
            val rs = RenderScript.create(context)
            val input =
                Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT)
            val output = Allocation.createTyped(rs, input.type)
            val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            script.setRadius(radius.toFloat())
            script.setInput(input)
            script.forEach(output)
            output.copyTo(bitmap)
            return bitmap
        } else {
            bitmap = sentBitmap.copy(sentBitmap.config, true)
            if (radius < 1) {
                return null
            }
            val w = bitmap.width
            val h = bitmap.height
            val pix = IntArray(w * h)
            bitmap.getPixels(pix, 0, w, 0, 0, w, h)
            val wm = w - 1
            val hm = h - 1
            val wh = w * h
            val div = radius + radius + 1
            val r = IntArray(wh)
            val g = IntArray(wh)
            val b = IntArray(wh)
            var rsum: Int
            var gsum: Int
            var bsum: Int
            var x: Int
            var y: Int
            var i: Int
            var p: Int
            var yp: Int
            var yi: Int
            var yw: Int
            val vmin = IntArray(Math.max(w, h))
            var divsum = div + 1 shr 1
            divsum *= divsum
            val temp = 256 * divsum
            val dv = IntArray(temp)
            i = 0
            while (i < temp) {
                dv[i] = i / divsum
                i++
            }
            yi = 0
            yw = yi
            val stack = Array(div) { IntArray(3) }
            var stackpointer: Int
            var stackstart: Int
            var sir: IntArray
            var rbs: Int
            val r1 = radius + 1
            var routsum: Int
            var goutsum: Int
            var boutsum: Int
            var rinsum: Int
            var ginsum: Int
            var binsum: Int
            y = 0
            while (y < h) {
                bsum = 0
                gsum = bsum
                rsum = gsum
                boutsum = rsum
                goutsum = boutsum
                routsum = goutsum
                binsum = routsum
                ginsum = binsum
                rinsum = ginsum
                i = -radius
                while (i <= radius) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))]
                    sir = stack[i + radius]
                    sir[0] = p and 0xff0000 shr 16
                    sir[1] = p and 0x00ff00 shr 8
                    sir[2] = p and 0x0000ff
                    rbs = r1 - Math.abs(i)
                    rsum += sir[0] * rbs
                    gsum += sir[1] * rbs
                    bsum += sir[2] * rbs
                    if (i > 0) {
                        rinsum += sir[0]
                        ginsum += sir[1]
                        binsum += sir[2]
                    } else {
                        routsum += sir[0]
                        goutsum += sir[1]
                        boutsum += sir[2]
                    }
                    i++
                }
                stackpointer = radius
                x = 0
                while (x < w) {
                    r[yi] = dv[rsum]
                    g[yi] = dv[gsum]
                    b[yi] = dv[bsum]
                    rsum -= routsum
                    gsum -= goutsum
                    bsum -= boutsum
                    stackstart = stackpointer - radius + div
                    sir = stack[stackstart % div]
                    routsum -= sir[0]
                    goutsum -= sir[1]
                    boutsum -= sir[2]
                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm)
                    }
                    p = pix[yw + vmin[x]]
                    sir[0] = p and 0xff0000 shr 16
                    sir[1] = p and 0x00ff00 shr 8
                    sir[2] = p and 0x0000ff
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                    rsum += rinsum
                    gsum += ginsum
                    bsum += binsum
                    stackpointer = (stackpointer + 1) % div
                    sir = stack[stackpointer % div]
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                    rinsum -= sir[0]
                    ginsum -= sir[1]
                    binsum -= sir[2]
                    yi++
                    x++
                }
                yw += w
                y++
            }
            x = 0
            while (x < w) {
                bsum = 0
                gsum = bsum
                rsum = gsum
                boutsum = rsum
                goutsum = boutsum
                routsum = goutsum
                binsum = routsum
                ginsum = binsum
                rinsum = ginsum
                yp = -radius * w
                i = -radius
                while (i <= radius) {
                    yi = Math.max(0, yp) + x
                    sir = stack[i + radius]
                    sir[0] = r[yi]
                    sir[1] = g[yi]
                    sir[2] = b[yi]
                    rbs = r1 - Math.abs(i)
                    rsum += r[yi] * rbs
                    gsum += g[yi] * rbs
                    bsum += b[yi] * rbs
                    if (i > 0) {
                        rinsum += sir[0]
                        ginsum += sir[1]
                        binsum += sir[2]
                    } else {
                        routsum += sir[0]
                        goutsum += sir[1]
                        boutsum += sir[2]
                    }
                    if (i < hm) {
                        yp += w
                    }
                    i++
                }
                yi = x
                stackpointer = radius
                y = 0
                while (y < h) {

                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] =
                        -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                    rsum -= routsum
                    gsum -= goutsum
                    bsum -= boutsum
                    stackstart = stackpointer - radius + div
                    sir = stack[stackstart % div]
                    routsum -= sir[0]
                    goutsum -= sir[1]
                    boutsum -= sir[2]
                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w
                    }
                    p = x + vmin[y]
                    sir[0] = r[p]
                    sir[1] = g[p]
                    sir[2] = b[p]
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                    rsum += rinsum
                    gsum += ginsum
                    bsum += binsum
                    stackpointer = (stackpointer + 1) % div
                    sir = stack[stackpointer]
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                    rinsum -= sir[0]
                    ginsum -= sir[1]
                    binsum -= sir[2]
                    yi += w
                    y++
                }
                x++
            }
            bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        }
        return bitmap
    }

    /**设置textview的drableleft */
    fun setTextViewDrableLeft(activity: Context, tv: TextView, wh: Int, drableRid: Int) {
        var wh = wh
        val drawable = activity.resources.getDrawable(drableRid)
        wh = AbViewUtil.dp2px(activity, wh.toFloat())
        drawable.setBounds(0, 0, wh, wh)
        tv.setCompoundDrawables(drawable, null, null, null)
    }

    /**设置textview的drableleft */
    fun setTextViewDrableTop(activity: Context, tv: TextView, wh: Int, drableRid: Int) {
        var wh = wh
        val drawable = activity.resources.getDrawable(drableRid)
        wh = AbViewUtil.dp2px(activity, wh.toFloat())
        drawable.setBounds(0, 0, wh, wh)
        tv.setCompoundDrawables(null, drawable, null, null)
    }

    /**设置textview的drableleft */
    fun setBackground(activity: Context, view: View, wh: Int, drableRid: Int) {
        var wh = wh
        val drawable = activity.resources.getDrawable(drableRid)
        wh = AbViewUtil.dp2px(activity, wh.toFloat())
        drawable.setBounds(0, 0, wh, wh)
        view.setBackgroundDrawable(drawable)
    }
}