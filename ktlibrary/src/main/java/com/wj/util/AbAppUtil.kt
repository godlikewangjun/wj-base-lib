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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import java.lang.Exception
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.net.ConnectivityManager
import android.net.NetworkInfo
import java.lang.Class
import android.view.View
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import java.util.ArrayList
import android.net.Uri
import android.util.DisplayMetrics
import android.app.Activity
import android.os.Build
import java.lang.StringBuilder
import kotlin.jvm.Synchronized
import android.content.Intent
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.location.LocationManager
import android.view.inputmethod.InputMethodManager
import android.content.pm.ApplicationInfo
import java.lang.ProcessBuilder
import java.lang.Thread
import java.lang.NoSuchMethodException
import java.lang.IllegalAccessException
import java.lang.reflect.InvocationTargetException
import android.content.pm.PackageManager.NameNotFoundException
import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents
import android.app.ActivityManager.RunningTaskInfo
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import java.io.*
import java.util.regex.Pattern

/**
 *
 */
object AbAppUtil {

    /**
     * 描述：打开并安装文件.
     *
     * @param context the context
     * @param file    apk文件路径
     */
    @RequiresPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES)
    fun installApk(context: Context, file: File) {
        //判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val contentUri = FileProvider.getUriForFile(context,
                    context.packageName + ".fileProvider",
                    file)
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
            } else {
                intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive")
            }
            //            intent.setClassName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity");
        } catch (e: Exception) {
            e.printStackTrace()
        }
        context.startActivity(intent)
    }

    /**
     * 描述：卸载程序.
     *
     * @param context     the context
     * @param packageName 包名
     */
    fun uninstallApk(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE)
        val packageURI = Uri.parse("package:$packageName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = packageURI
        context.startActivity(intent)
    }

    /**
     * 清理内存
     *
     * @param context
     */
    fun onClearMemory(context: Context) {
        val activityManger = context
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = activityManger
            .runningAppProcesses
        if (list != null) for (i in list.indices) {
            val apinfo = list[i]
            val pkgList = apinfo.pkgList
            if (apinfo.importance >= RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (j in pkgList.indices) {
                    if (pkgList[j] == context.packageName) {
                        continue
                    }
                    activityManger.killBackgroundProcesses(pkgList[j])
                }
            }
        }
    }

    /**
     * 用来判断服务是否运行.
     *
     * @param context   the context
     * @param className 判断的服务名字 "com.xxx.xx..XXXService"
     * @return true 在运行 false 不在运行
     */
    fun isServiceRunning(context: Context, className: String): Boolean {
        var isRunning = false
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val servicesList = activityManager.getRunningServices(Int.MAX_VALUE)
        val l: Iterator<ActivityManager.RunningServiceInfo> = servicesList.iterator()
        while (l.hasNext()) {
            if (className == l.next().service.className) {
                isRunning = true
            }
        }
        return isRunning
    }

    /**
     * 打开一个应用
     *
     * @param packageName 包名
     * @param context
     */
    fun openApp(context: Context, packageName: String) {
        var intent = Intent()
        val packageManager = context.packageManager
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent = packageManager.getLaunchIntentForPackage(packageName)!!
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_SINGLE_TOP
        context.startActivity(intent)
    }

    /**
     * 停止服务.
     *
     * @param context   the context
     * @param className the class name
     * @return true, if successful
     */
    fun stopRunningService(context: Context, className: String?): Boolean {
        var intent_service: Intent? = null
        var ret = false
        try {
            intent_service = Intent(context, Class.forName(className))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (intent_service != null) {
            ret = context.stopService(intent_service)
        }
        return ret
    }//Check if filename is "cpu", followed by a single digit number
    //Return the number of cores (virtual CPU devices)
//Get directory containing CPU info
    //Filter to only list the devices we care about
    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    val numCores: Int
        get() = try {
            //Get directory containing CPU info
            val dir = File("/sys/devices/system/cpu/")
            //Filter to only list the devices we care about
            val files =
                dir.listFiles { pathname -> //Check if filename is "cpu", followed by a single digit number
                    Pattern.matches("cpu[0-9]", pathname.name)
                }
            //Return the number of cores (virtual CPU devices)
            files.size
        } catch (e: Exception) {
            e.printStackTrace()
            1
        }

    /**
     * 描述：判断网络是否有效.
     *
     * @param context the context
     * @return true, if is network available
     */
    fun isNetworkAvailable(context: Context): Boolean {
        try {
            val connectivity = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivity.activeNetworkInfo
            if (info != null && info.isConnected) {
                if (info.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }

    /**
     * Gps是否打开
     * 需要<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"></uses-permission>权限
     *
     * @param context the context
     * @return true, if is gps enabled
     */
    fun isGpsEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * 判断当前网络是否是移动数据网络.
     *
     * @param context the context
     * @return boolean
     */
    fun isMobile(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return if (activeNetInfo != null
            && activeNetInfo.type == ConnectivityManager.TYPE_MOBILE
        ) {
            true
        } else false
    }

    /**
     * 导入数据库.
     *
     * @param context the context
     * @param dbName  the db name
     * @param rawRes  the raw res
     * @return true, if successful
     */
    @SuppressLint("SdCardPath")
    fun importDatabase(context: Context, dbName: String, rawRes: Int): Boolean {
        val bufferSize = 1024
        var `is`: InputStream? = null
        var fos: FileOutputStream? = null
        var flag = false
        try {
            val dbPath = "/data/data/" + context.packageName + "/databases/" + dbName
            val dbfile = File(dbPath)
            //判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
            if (!dbfile.exists()) {
                //欲导入的数据库
                if (!dbfile.parentFile.exists()) {
                    dbfile.parentFile.mkdirs()
                }
                dbfile.createNewFile()
                `is` = context.resources.openRawResource(rawRes)
                fos = FileOutputStream(dbfile)
                val buffer = ByteArray(bufferSize)
                var count: Int
                while (`is`.read(buffer).also { count = it } > 0) {
                    fos.write(buffer, 0, count)
                }
                fos.flush()
            }
            flag = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: Exception) {
                }
            }
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: Exception) {
                }
            }
        }
        return flag
    }

    /**
     * 获取屏幕尺寸与密度.
     *
     * @param context the context
     * @return mDisplayMetrics
     */
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay
            .getMetrics(displayMetrics)
        return displayMetrics
    }

    /**
     * 打开键盘.
     *
     * @param context the context
     */
    fun showSoftInput(context: Activity) {
        try {
            val view = context.window.currentFocus
            if (view != null) {
                val inputmanger =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputmanger.showSoftInput(view, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 打开键盘.
     */
    fun showSoftInput(activity: Activity, view: View?) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }

    /**
     * 关闭键盘事件.
     *
     * @param context the context
     */
    fun closeSoftInput(context: Activity) {
        try {
            val view = context.window.currentFocus
            if (view != null) {
                val inputmanger =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputmanger.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 关闭键盘事件.
     */
    fun closeSoftInput(activity: Activity, view: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 获取包信息.
     *
     * @param context the context
     */
    fun getPackageInfo(context: Context): PackageInfo? {
        var info: PackageInfo? = null
        try {
            val packageName = context.packageName
            info = context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return info
    }

    /**
     * 描述：根据进程名返回应用程序.
     *
     * @param context
     * @param processName
     * @return
     */
    fun getApplicationInfo(context: Context, processName: String?): ApplicationInfo? {
        if (processName == null) {
            return null
        }
        val packageManager = context.applicationContext.packageManager
        val appList =
            packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)
        for (appInfo in appList) {
            if (processName == appInfo.processName) {
                return appInfo
            }
        }
        return null
    }

    /**
     * 描述：kill进程.
     *
     * @param context
     * @param pid
     */
    fun killProcesses(context: Context, pid: Int, processName: String) {
        /*String cmd = "kill -9 "+pid;
        Process process = null;
	    DataOutputStream os = null;
    	try {
			process = Runtime.getRuntime().exec("su"); 
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	AbLogUtil.d(AbAppUtil.class, "#kill -9 "+pid);*/
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName: String?
        try {
            packageName = if (processName.indexOf(":") == -1) {
                processName
            } else {
                processName.split(":").toTypedArray()[0]
            }
            activityManager.killBackgroundProcesses(packageName)

            //
            val forceStopPackage =
                activityManager.javaClass.getDeclaredMethod("forceStopPackage", String::class.java)
            forceStopPackage.isAccessible = true
            forceStopPackage.invoke(activityManager, packageName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 描述：执行命令.
     *
     * @param command
     * @param workdirectory
     * @return
     */
    fun runCommand(command: Array<String>, workdirectory: String?): String {
        var result = ""
        AbLogUtil.d(AbAppUtil::class.java, "#$command")
        try {
            val builder = ProcessBuilder(*command)
            // set working directory
            if (workdirectory != null) {
                builder.directory(File(workdirectory))
            }
            builder.redirectErrorStream(true)
            val process = builder.start()
            val `in` = process.inputStream
            val buffer = ByteArray(1024)
            while (`in`.read(buffer) != -1) {
                val str = String(buffer)
                result = result + str
            }
            `in`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 描述：运行脚本.
     *
     * @param script
     * @return
     */
    fun runScript(script: String?): String? {
        var sRet = ""
        try {
            val m_process = Runtime.getRuntime().exec(script)
            val sbread = StringBuilder()
            val tout = Thread {
                val bufferedReader = BufferedReader(
                    InputStreamReader(m_process.inputStream),
                    8192)
                var ls_1: String? = null
                try {
                    while (bufferedReader.readLine().also { ls_1 = it } != null) {
                        sbread.append(ls_1).append("\n")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        bufferedReader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            tout.start()
            val sberr = StringBuilder()
            val terr = Thread {
                val bufferedReader = BufferedReader(
                    InputStreamReader(m_process.errorStream),
                    8192)
                var ls_1: String? = null
                try {
                    while (bufferedReader.readLine().also { ls_1 = it } != null) {
                        sberr.append(ls_1).append("\n")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        bufferedReader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            terr.start()
            val retvalue = m_process.waitFor()
            while (tout.isAlive) {
                Thread.sleep(50)
            }
            if (terr.isAlive) terr.interrupt()
            val stdout = sbread.toString()
            val stderr = sberr.toString()
            sRet = stdout + stderr
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return sRet
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    fun getRootPermission(context: Context): Boolean {
        val packageCodePath = context.packageCodePath
        var process: Process? = null
        var os: DataOutputStream? = null
        try {
            val cmd = "chmod 777 $packageCodePath"
            //切换到root帐号
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes("""
    $cmd
    
    """.trimIndent())
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
        } catch (e: Exception) {
            return false
        } finally {
            try {
                os?.close()
                process!!.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    /**
     * 描述：获取进程运行的信息.
     *
     * @return
     */
    val processRunningInfo: List<Array<String>?>?
        get() {
            var processList: List<Array<String>?>? = null
            try {
                val result = runCommandTopN1()
                processList = parseProcessRunningInfo(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return processList
        }

    /**
     * 描述：top -n 1.
     *
     * @return
     */
    fun runCommandTopN1(): String? {
        var result: String? = null
        try {
            val args = arrayOf("/system/bin/top", "-n", "1")
            result = runCommand(args, "/system/bin/")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 描述：解析数据.
     *
     * @param info User 39%, System 17%, IOW 3%, IRQ 0%
     * PID    PR CPU% S   #THR     VSS     RSS    PCY    UID        Name
     * 31587  0  39%  S    14    542288K  42272K  fg   u0_a162  cn.amsoft.process
     * 313    1  17%  S    12    68620K   11328K  fg   system   /system/bin/surfaceflinger
     * 32076  1   2%  R     1    1304K    604K    bg   u0_a162  /system/bin/top
     * @return
     */
    fun parseProcessRunningInfo(info: String?): List<Array<String>?> {
        val processList: MutableList<Array<String>?> = ArrayList()
        val Length_ProcStat = 10
        var tempString = ""
        var bIsProcInfo = false
        var rows: Array<String>? = null
        var columns: Array<String>? = null
        rows = info!!.split("[\n]+").toTypedArray()
        // 使用正则表达式分割字符串
        for (i in rows.indices) {
            tempString = rows[i]
            //AbLogUtil.d(AbAppUtil.class, tempString);
            if (tempString.indexOf("PID") == -1) {
                if (bIsProcInfo == true) {
                    tempString = tempString.trim { it <= ' ' }
                    columns = tempString.split("[ ]+").toTypedArray()
                    if (columns.size == Length_ProcStat) {
                        //把/system/bin/的去掉
                        if (columns[9].startsWith("/system/bin/")) {
                            continue
                        }
                        //AbLogUtil.d(AbAppUtil.class, "#"+columns[9]+",PID:"+columns[0]);
                        processList.add(columns)
                    }
                }
            } else {
                bIsProcInfo = true
            }
        }
        return processList
    }

    /**
     * / **
     *
     *
     * 描述：获取可用内存.
     *
     * @param context
     * @return
     */
    fun getAvailMemory(context: Context): Long {
        //获取android当前可用内存大小  
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        //当前系统可用内存 ,将获得的内存大小规格化  
        return memoryInfo.availMem
    }

    /**
     * 描述：总内存.
     *
     * @param context
     * @return
     */
    fun getTotalMemory(context: Context?): Long {
        //系统内存信息文件
        val file = "/proc/meminfo"
        val memInfo: String
        val strs: Array<String>
        var memory: Long = 0
        try {
            val fileReader = FileReader(file)
            val bufferedReader = BufferedReader(fileReader, 8192)
            //读取meminfo第一行，系统内存大小 
            memInfo = bufferedReader.readLine()
            strs = memInfo.split("\\s+").toTypedArray()
            for (str in strs) {
                AbLogUtil.d(AbAppUtil::class.java, str + "\t")
            }
            //获得系统总内存，单位KB  
            memory = (Integer.valueOf(strs[1]).toInt() * 1024).toLong()
            bufferedReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //Byte转位KB或MB
        return memory
    }

    /**
     * 判断应用是否在运行
     *
     * @param context 上下文
     * @param intent  intent携带activity
     * @return boolean true为在运行，false为已结束
     */
    fun isRunning(context: Context, className: String?): Boolean {
        val intent = Intent()
        intent.setClassName(context.packageName, className!!)
        val resolveInfo =
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo != null
    }

    /**
     * 根据包名启动指定的应用
     *
     * @param context
     * @param packageName
     * @return
     */
    fun startActivityByPackageName(context: Context, packageName: String): Boolean {
        try {
            val pm = context.packageManager
            if (pm != null) {
                val intent = pm.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
            }
        } catch (_: Throwable) {
        }
        return false
    }

    /**
     * 打开选择器，通过uri打开activity
     *
     * @param context
     * @param uri
     * @param flags
     * @param title
     * @return
     */
    fun startActivityByUriWithChooser(
        context: Context,
        uri: String?,
        flags: Int,
        title: String?
    ): Boolean {
        try {
            val intent = Intent.parseUri(uri, flags) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val startIntent = Intent.createChooser(intent, title)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(startIntent)
            return true
        } catch (e: Throwable) {
        }
        return false
    }

    /**
     * 获取设备号
     */
    private var deviceidStr: StringBuilder? = null
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getDeviceId(context: Context?): String {
        if (context == null) {
            return ""
        }
        //        if (!AbStrUtil.isEmpty(deviceidStr)) {
//            return deviceidStr;
//        }
//        File file = new File(AbFileUtil.getCacheDownloadDir(context), "dv");
//        if (file.exists()) {
//            deviceidStr = AbFileUtil.okReadFile(file);
//            return deviceidStr;
//        }
        try {
            //IMEI（imei）
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                ?: return ""
            //检测权限
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
            ) {
                return ""
            }
            deviceidStr = StringBuilder()
            val strings = ArrayList<String>()
            val telephonyManager = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            addString(strings, tm.deviceId)
            try {
                val clazz: Class<*> = telephonyManager.javaClass
                val getImei =
                    clazz.getDeclaredMethod("getImei", Int::class.javaPrimitiveType) //(int slotId)

                //获得IMEI 1的信息：
                val imei1 = getImei.invoke(telephonyManager, 0) as String
                addString(strings, imei1)
                val imei2 = getImei.invoke(telephonyManager, 1) as String
                addString(strings, imei2)
            } catch (e: NoSuchMethodException) {
            } catch (e: IllegalAccessException) {
            } catch (e: InvocationTargetException) {
            }
            val cTelephoneInfo = CTelephoneInfo.instance!!
            cTelephoneInfo.setCTelephoneInfo(context)
            addString(strings, cTelephoneInfo.imeiSIM1)
            addString(strings, cTelephoneInfo.imeiSIM2)
            for (i in strings.indices) {
                if (!AbStrUtil.isEmpty(strings[i])) {
                    if (i < strings.size - 1) {
                        deviceidStr!!.append(strings[i]).append("|")
                    } else {
                        deviceidStr!!.append(strings[i])
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return deviceidStr.toString()
    }

    /**
     * 添加IMEI
     *
     * @param strings
     * @param id
     */
    private fun addString(strings: ArrayList<String>, id: String?) {
        if (id != null && !strings.contains(id) && (id.length == 14 || id.length == 15)) {
            strings.add(id)
        }
    }

    /**
     * 获取渠道
     */
    fun getMeta(context: Context, channel: String?): String {
        try {
            var msg = ""
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA)
            msg = appInfo.metaData.getString(channel)!!
            return msg
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 判断当前的activity是否为top
     *
     * @param context
     * @param className null就返回 “”表示检测包名
     * @return
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @Synchronized
    fun isTopActivity(context: Context, className: String?, isPack: Boolean): Boolean {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || className == null
        ) {
            return false
        }
        try {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //版本判断
                val m = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                if (m != null) {
                    val now = System.currentTimeMillis()
                    //获取60秒之内的应用数据
                    val usageEvents = m.queryEvents(now - 30 * 60 * 1000, now)
                    var topActivity = ""
                    var packName = ""
                    if (usageEvents != null) {
                        var event = UsageEvents.Event()
                        while (usageEvents.hasNextEvent()) {
                            val eventAux = UsageEvents.Event()
                            usageEvents.getNextEvent(eventAux)
                            if (eventAux.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                                event = eventAux
                            }
                        }
                        topActivity = event.className
                        packName = event.packageName
                    }
                    if (topActivity == null && packName == null) {
                        return false
                    }
                    return if (isPack) { //空就检查包名
                        AbLogUtil.d(AbAppUtil::class.java,
                            packName + "==栈顶包名==" + className + "===" + (packName == className))
                        packName == className
                    } else {
                        AbLogUtil.d(AbAppUtil::class.java,
                            topActivity + "==栈顶==" + className + "===" + (className == topActivity))
                        topActivity.contains(className)
                    }
                }
            } else {
                val rTasks = getRunningTask(context, 1)
                for (task in rTasks!!) {
                    if (isPack) {
                        if (task.topActivity!!.packageName == className) {
                            return true
                        }
                    } else {
                        if (task.topActivity!!.className.contains(className)) {
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 判断当前的activity是否为top
     *
     * @param context
     * @param className null就返回 “”表示检测包名
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @Synchronized
    fun haveActivity(context: Context, className: String?, isPack: Boolean): Boolean {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || className == null
        ) {
            return false
        }
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //版本判断
            val m = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            //获取60秒之内的应用数据
            val usageEvents = m.queryEvents(now - 30 * 60 * 1000, now)
            val topActivity = ""
            val packName = ""
            if (usageEvents != null) {
                while (usageEvents.hasNextEvent()) {
                    val eventAux = UsageEvents.Event()
                    usageEvents.getNextEvent(eventAux)
                    if (isPack) { //空就检查包名
                        if (eventAux.packageName == className) {
                            println(packName + "==栈顶包名==" + className + "===" + (eventAux.packageName == className))
                            return true
                        }
                    } else {
                        if (eventAux.className.contains(className)) {
                            println(topActivity + "==栈顶==" + className + "===" + (eventAux.className == topActivity))
                            return true
                        }
                    }
                }
            }
        } else {
            val rTasks = getRunningTask(context, 20)
            for (task in rTasks!!) {
                if (task.topActivity!!.className == className) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 获取顶层的activity
     * @return
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getTopActivity(context: Context): Array<String>? {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //版本判断
            val m = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            if (m != null) {
                val now = System.currentTimeMillis()
                //获取60秒之内的应用数据
                val usageEvents = m.queryEvents(now - 60 * 60 * 1000, now)
                var topActivity = ""
                var packName = ""
                if (usageEvents != null) {
                    var event = UsageEvents.Event()
                    while (usageEvents.hasNextEvent()) {
                        val eventAux = UsageEvents.Event()
                        usageEvents.getNextEvent(eventAux)
                        if (eventAux.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            event = eventAux
                        }
                    }
                    topActivity = event.className
                    packName = event.packageName
                }
                return arrayOf(packName, topActivity)
            }
        } else {
            val rTasks = getRunningTask(context, 1)
            val task = rTasks!![0]
            return arrayOf(task.topActivity!!.packageName, task.topActivity!!.className)
        }
        return null
    }

    /**
     * 获取activity的堆栈
     *
     * @param context
     * @param num
     * @return
     */
    fun getRunningTask(context: Context?, num: Int): List<RunningTaskInfo>? {
        if (context != null) {
            val am = context
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return am.getRunningTasks(num)
        }
        return null
    }

    /**
     * 取得设备信息
     *
     * @param context
     * @return
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getDeviceInfo(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val sb = StringBuilder()
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            return ""
        }
        sb.append("\\" + tm.deviceSoftwareVersion)
        sb.append("\\" + tm.line1Number)
        sb.append("\\" + tm.networkCountryIso)
        sb.append("\\" + tm.networkOperator)
        sb.append("\\" + tm.networkOperatorName)
        sb.append("\\" + tm.networkType)
        sb.append("\\" + tm.phoneType)
        sb.append("\\" + tm.simCountryIso)
        sb.append("\\" + tm.simOperator)
        sb.append("\\" + tm.simOperatorName)
        sb.append("\\" + tm.simSerialNumber)
        sb.append("\\" + tm.simState)
        sb.append("\\" + tm.subscriberId)
        sb.append("\\" + tm.voiceMailNumber)
        return sb.toString()
    }

    /**
     * 是否有SIM
     *
     * @param context
     * @return
     */
    fun isSiMDo(context: Context): Boolean {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val simSer = tm.simSerialNumber
        return if (simSer == null || simSer == "") {
            false
        } else true
    }

    /**
     * 判断是否安装apk
     * @param context
     * @param packageName
     * @return
     */
    fun isInstallApk(context: Context, packageName: String?): Boolean {
        return if (packageName == null || "" == packageName) false else try {
            val info = context.packageManager.getApplicationInfo(packageName,
                PackageManager.GET_UNINSTALLED_PACKAGES)
            true
        } catch (e: NameNotFoundException) {
            false
        }
    }
}