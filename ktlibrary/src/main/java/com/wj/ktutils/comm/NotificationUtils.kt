package com.wj.ktutils.comm


import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import android.widget.RemoteViews

/**
 * <pre>
 * @author yangchong
 * blog  : https://www.jianshu.com/p/514eb6193a06
 * time  : 2018/2/10
 * desc  : 通知栏工具类
 * revise:
</pre> *
 */
class NotificationUtils(base: Context) : ContextWrapper(base) {
    private var mManager: NotificationManager? = null
    private var flags: IntArray? = null

    /**
     * 获取创建一个NotificationManager的对象
     * @return                          NotificationManager对象
     */
    val manager: NotificationManager
        get() {
            if (mManager == null) {
                mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return mManager as NotificationManager
        }


    private var ongoing = false
    private var remoteViews: RemoteViews? = null
    private var intent: PendingIntent? = null
    private var ticker: String? = ""
    private var priority = Notification.PRIORITY_DEFAULT
    private var onlyAlertOnce = false
    private var `when`: Long = 0
    private var sound: Uri? = null
    private var defaults = 0
    private var pattern: LongArray? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android 8.0以上需要特殊处理，也就是targetSDKVersion为26以上
            createNotificationChannel()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        //第一个参数：channel_id
        //第二个参数：channel_name
        //第三个参数：设置通知重要性级别
        //注意：该级别必须要在 NotificationChannel 的构造函数中指定，总共要五个级别；
        //范围是从 NotificationManager.IMPORTANCE_NONE(0) ~ NotificationManager.IMPORTANCE_HIGH(4)
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)
        channel.canBypassDnd()//是否绕过请勿打扰模式
        channel.enableLights(true)//闪光灯
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET//锁屏显示通知
        channel.lightColor = Color.RED//闪关灯的灯光颜色
        channel.canShowBadge()//桌面launcher的消息角标
        channel.enableVibration(true)//是否允许震动
        channel.audioAttributes//获取系统通知响铃声音的配置
        channel.group//获取通知取到组
        channel.setBypassDnd(true)//设置可绕过 请勿打扰模式
        channel.vibrationPattern = longArrayOf(100, 100, 200)//设置震动模式
        channel.shouldShowLights()//是否会有灯光
        manager.createNotificationChannel(channel)
    }

    /**
     * 清空所有的通知
     */
    fun clearNotification() {
        manager.cancelAll()
    }

    /**
     * 获取Notification
     * @param title                     title
     * @param content                   content
     */
    fun getNotification(notifyId: Int,title: String, content: String, icon: Int,smallIcon:Int): Notification {
        val build: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android 8.0以上需要特殊处理，也就是targetSDKVersion为26以上
            //通知用到NotificationCompat()这个V4库中的方法。但是在实际使用时发现书上的代码已经过时并且Android8.0已经不支持这种写法
            val builder = getChannelNotification(title, content, icon,smallIcon)
            builder.setChannelId(notifyId.toString())
            build = builder.build()
        } else {
            val builder = getNotificationCompat(notifyId,title, content, icon,smallIcon)
            build = builder.build()
        }
        if (flags != null && flags!!.isNotEmpty()) {
            for (a in flags!!.indices) {
                build.flags = build.flags or flags!![a]
            }
        }
        return build
    }

    /**
     * 建议使用这个发送通知
     * 调用该方法可以发送通知
     * @param notifyId                  notifyId
     * @param title                     title
     * @param content                   content
     */
    fun sendNotification(notifyId: Int, title: String, content: String, icon: Int,smallIcon:Int) {
        val build: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android 8.0以上需要特殊处理，也就是targetSDKVersion为26以上
            //通知用到NotificationCompat()这个V4库中的方法。但是在实际使用时发现书上的代码已经过时并且Android8.0已经不支持这种写法
            val builder = getChannelNotification(title, content, icon,smallIcon)
            build = builder.build()
        } else {
            val builder = getNotificationCompat(notifyId,title, content, icon,smallIcon)
            build = builder.build()
        }
        if (flags != null && flags!!.isNotEmpty()) {
            for (a in flags!!.indices) {
                build.flags = build.flags or flags!![a]
            }
        }
        manager.notify(notifyId, build)
    }

    /**
     * 调用该方法可以发送通知
     * @param notifyId                  notifyId
     * @param title                     title
     * @param content                   content
     */
    fun sendNotificationCompat(notifyId: Int, title: String, content: String, icon: Int,smallIcon:Int) {
        val builder = getNotificationCompat(notifyId,title, content, icon,smallIcon)
        val build = builder.build()
        if (flags != null && flags!!.isNotEmpty()) {
            for (a in flags!!.indices) {
                build.flags = build.flags or flags!![a]
            }
        }
        manager.notify(notifyId, build)
    }


    private fun getNotificationCompat(notifyId: Int,title: String, content: String, icon: Int,smallIcon: Int): NotificationCompat.Builder {
        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        } else {
            //注意用下面这个方法，在8.0以上无法出现通知栏。8.0之前是正常的。这里需要增强判断逻辑
            builder = NotificationCompat.Builder(applicationContext,notifyId.toString())
            builder.priority = NotificationCompat.PRIORITY_DEFAULT
        }
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setSmallIcon(smallIcon)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources,icon))
        builder.priority = priority
        builder.setOnlyAlertOnce(onlyAlertOnce)
        builder.setOngoing(ongoing)
        if (remoteViews != null) {
            builder.setContent(remoteViews)
        }
        if (intent != null) {
            builder.setContentIntent(intent)
        }
        if (ticker != null && ticker!!.isNotEmpty()) {
            builder.setTicker(ticker)
        }
        if (`when` != 0L) {
            builder.setWhen(`when`)
        }
        if (sound != null) {
            builder.setSound(sound)
        }
        if (defaults != 0) {
            builder.setDefaults(defaults)
        }
        //点击自动删除通知
        builder.setAutoCancel(true)
        return builder
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getChannelNotification(title: String, content: String, icon: Int,smallIcon:Int): Notification.Builder {
        val builder = Notification.Builder(applicationContext, CHANNEL_ID)
        val notificationBuilder = builder
                //设置标题
                .setContentTitle(title)
                //消息内容
                .setContentText(content)
                .setLargeIcon((BitmapFactory.decodeResource(resources,icon)))
                //设置通知的图标
                .setSmallIcon(smallIcon)
                //让通知左右滑的时候是否可以取消通知
                .setOngoing(ongoing)
                //设置优先级
                .setPriority(priority)
                //是否提示一次.true - 如果Notification已经存在状态栏即使在调用notify函数也不会更新
                .setOnlyAlertOnce(onlyAlertOnce)
                .setAutoCancel(true)
        if (remoteViews != null) {
            //设置自定义view通知栏
            notificationBuilder.setContent(remoteViews)
        }
        if (intent != null) {
            notificationBuilder.setContentIntent(intent)
        }
        if (ticker != null && ticker!!.isNotEmpty()) {
            //设置状态栏的标题
            notificationBuilder.setTicker(ticker)
        }
        if (`when` != 0L) {
            //设置通知时间，默认为系统发出通知的时间，通常不用设置
            notificationBuilder.setWhen(`when`)
        }
        if (sound != null) {
            //设置sound
            notificationBuilder.setSound(sound)
        }
        if (defaults != 0) {
            //设置默认的提示音
            notificationBuilder.setDefaults(defaults)
        }
        if (pattern != null) {
            //自定义震动效果
            notificationBuilder.setVibrate(pattern)
        }
        return notificationBuilder
    }

    /**
     * 让通知左右滑的时候是否可以取消通知
     * @param ongoing                   是否可以取消通知
     * @return
     */
    fun setOngoing(ongoing: Boolean): NotificationUtils {
        this.ongoing = ongoing
        return this
    }

    /**
     * 设置自定义view通知栏布局
     * @param remoteViews               view
     * @return
     */
    fun setContent(remoteViews: RemoteViews): NotificationUtils {
        this.remoteViews = remoteViews
        return this
    }

    /**
     * 设置内容点击
     * @param intent                    intent
     * @return
     */
    fun setContentIntent(intent: PendingIntent): NotificationUtils {
        this.intent = intent
        return this
    }

    /**
     * 设置状态栏的标题
     * @param ticker                    状态栏的标题
     * @return
     */
    fun setTicker(ticker: String): NotificationUtils {
        this.ticker = ticker
        return this
    }


    /**
     * 设置优先级
     * 注意：
     * Android 8.0以及上，在 NotificationChannel 的构造函数中指定，总共要五个级别；
     * Android 7.1（API 25）及以下的设备，还得调用NotificationCompat 的 setPriority方法来设置
     *
     * @param priority                  优先级，默认是Notification.PRIORITY_DEFAULT
     * @return
     */
    fun setPriority(priority: Int): NotificationUtils {
        this.priority = priority
        return this
    }

    /**
     * 是否提示一次.true - 如果Notification已经存在状态栏即使在调用notify函数也不会更新
     * @param onlyAlertOnce             是否只提示一次，默认是false
     * @return
     */
    fun setOnlyAlertOnce(onlyAlertOnce: Boolean): NotificationUtils {
        this.onlyAlertOnce = onlyAlertOnce
        return this
    }

    /**
     * 设置通知时间，默认为系统发出通知的时间，通常不用设置
     * @param when                      when
     * @return
     */
    fun setWhen(`when`: Long): NotificationUtils {
        this.`when` = `when`
        return this
    }

    /**
     * 设置sound
     * @param sound                     sound
     * @return
     */
    fun setSound(sound: Uri): NotificationUtils {
        this.sound = sound
        return this
    }


    /**
     * 设置默认的提示音
     * @param defaults                  defaults
     * @return
     */
    fun setDefaults(defaults: Int): NotificationUtils {
        this.defaults = defaults
        return this
    }

    /**
     * 自定义震动效果
     * @param pattern                  pattern
     * @return
     */
    fun setVibrate(pattern: LongArray): NotificationUtils {
        this.pattern = pattern
        return this
    }

    /**
     * 设置flag标签
     * @param flags                     flags
     * @return
     */
    fun setFlags(vararg flags: Int): NotificationUtils {
        this.flags = flags
        return this
    }

    companion object {
        val CHANNEL_ID = "default"
        private val CHANNEL_NAME = "Default_Channel"
    }

}
