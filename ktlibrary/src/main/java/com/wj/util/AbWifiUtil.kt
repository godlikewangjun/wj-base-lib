package com.wj.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import android.net.wifi.WifiManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.os.Build.VERSION
import android.net.wifi.ScanResult
import androidx.core.app.ActivityCompat

object AbWifiUtil {
    /**
     * 描述：打开wifi.
     *
     * @param context
     * @param enabled
     * @return
     */
    fun setWifiEnabled(context: Context, enabled: Boolean) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enabled
    }

    /**
     * 描述：是否有网络连接.
     *
     * @param context
     * @return
     */
    fun isConnectivity(context: Context?): Boolean {
        if (VERSION.SDK_INT < 23) {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable
                }
            }
        } else {
            val manager =
                context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = manager.getNetworkCapabilities(manager.activeNetwork)
            if (networkCapabilities != null) {
                return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        return false
    }

    /**
     * 判断当前网络是否可用(6.0以上版本)
     * 实时
     * @param context
     * @return
     */
    fun isNetSystemUsable(context: Context?): Boolean {
        return false
    }

    /**
     * 判断当前网络是否是wifi网络.
     *
     * @param context the context
     * @return boolean
     */
    fun isWifiConnectivity(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return if (activeNetInfo != null
            && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
        ) {
            true
        } else false
    }

    /**
     * 判断当前网络是否是流量网络
     *
     * @param context the context
     * @return boolean
     */
    fun isMobileConnectivity(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return (activeNetInfo != null
                && activeNetInfo.type == ConnectivityManager.TYPE_MOBILE)
    }

    /**
     * 描述：得到所有的WiFi列表.
     *
     * @param context
     * @return
     */
    fun getScanResults(context: Context): List<ScanResult>? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var list: List<ScanResult>? = null
        //开始扫描WiFi
        val f = wifiManager.startScan()
        if (!f) {
            getScanResults(context)
        } else {
            // 得到扫描结果
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            list = wifiManager.scanResults
        }
        return list
    }

    /**
     * 描述：根据SSID过滤扫描结果.
     *
     * @param context
     * @param bssid
     * @return
     */
    fun getScanResultsByBSSID(context: Context, bssid: String): ScanResult? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var scanResult: ScanResult? = null
        //开始扫描WiFi
        val f = wifiManager.startScan()
        if (!f) {
            getScanResultsByBSSID(context, bssid)
        }
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        // 得到扫描结果
        val list =  wifiManager.scanResults
        if (list != null) {
            for (i in list.indices) {
                // 得到扫描结果
                scanResult = list[i]
                if (scanResult.BSSID == bssid) {
                    break
                }
            }
        }
        return scanResult
    }

    /**
     * 描述：获取连接的WIFI信息.
     *
     * @param context
     * @return
     */
    fun getConnectionInfo(context: Context): WifiInfo {
        val wifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.connectionInfo
    }

    //没有网络连接
    private const val NETWORN_NONE = "none"

    //wifi连接
    private const val NETWORN_WIFI = "wifi"

    //手机网络数据连接类型
    private const val NETWORN_2G = "2G"
    private const val NETWORN_3G = "3G"
    private const val NETWORN_4G = "4G"
    private const val NETWORN_MOBILE = "5G"

    /**
     * 获取当前网络连接类型
     *
     * @param context
     * @return
     */
    fun getNetworkState(context: Context): String {
        //获取系统的网络服务
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                ?: return NETWORN_NONE
        //如果当前没有网络
        //获取当前网络类型，如果为空，返回无网络
        val activeNetInfo = connManager.activeNetworkInfo
        if (activeNetInfo == null || !activeNetInfo.isAvailable) {
            return NETWORN_NONE
        }
        // 判断是不是连接的是不是wifi
        val wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (null != wifiInfo) {
            val state = wifiInfo.state
            if (null != state) if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                return NETWORN_WIFI
            }
        }
        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (null != networkInfo) {
            val state = networkInfo.state
            val strSubTypeName = networkInfo.subtypeName
            if (null != state) if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                return when (activeNetInfo.subtype) {
                    TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> NETWORN_2G
                    TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> NETWORN_3G
                    TelephonyManager.NETWORK_TYPE_LTE -> NETWORN_4G
                    else -> //中国移动 联通 电信 三种3G制式
                        if (strSubTypeName.equals("TD-SCDMA",
                                ignoreCase = true) || strSubTypeName.equals("WCDMA",
                                ignoreCase = true) || strSubTypeName.equals("CDMA2000",
                                ignoreCase = true)
                        ) {
                            NETWORN_3G
                        } else {
                            NETWORN_MOBILE
                        }
                }
            }
        }
        return NETWORN_NONE
    }
}