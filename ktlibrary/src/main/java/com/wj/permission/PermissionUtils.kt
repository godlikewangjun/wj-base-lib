package com.wj.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


/**
 * 动态权限改造 方便权限申请
 */
class PermissionUtils(vararg permissions: String) {

    private var mOnRationaleListener: OnRationaleListener? = null
    private var mSimpleCallback: SimpleCallback? = null
    private var mFullCallback: FullCallback? = null
    private var mThemeCallback: ThemeCallback? = null
    //拒绝询问是否开启的提示语
    private val mAskMessages: ArrayList<String>?=null
    //添加的权限
    private val mPermissions: MutableSet<String>
    //需要请求的权限
    private var mPermissionsRequest: ArrayList<String>? = null
    //已经同意权限
    private var mPermissionsGranted: ArrayList<String>? = null
    //再次请求
    private var mPermissionsDenied: ArrayList<String>? = null
    //永久拒绝
    private var mPermissionsDeniedForever: ArrayList<String>? = null

    init {
        mPermissions = LinkedHashSet()
        for (permission in permissions) {
            for (aPermission in PermissionConstants.getPermissions(permission)) {
                if (PERMISSIONS!!.contains(aPermission)) {
                    mPermissions.add(aPermission)
                }
            }
        }
        sInstance = this
    }

    /**
     * Set rationale listener.
     *
     * @param listener The rationale listener.
     * @return the single [PermissionUtils] instance
     */
    fun rationale(listener: OnRationaleListener): PermissionUtils {
        mOnRationaleListener = listener
        return this
    }

    /**
     * Set the simple call back.
     *
     * @param callback the simple call back
     * @return the single [PermissionUtils] instance
     */
    fun callback(callback: SimpleCallback): PermissionUtils {
        mSimpleCallback = callback
        return this
    }

    /**
     * Set the full call back.
     *
     * @param callback the full call back
     * @return the single [PermissionUtils] instance
     */
    fun callback(callback: FullCallback): PermissionUtils {
        mFullCallback = callback
        return this
    }

    /**
     * Set the theme callback.
     *
     * @param callback The theme callback.
     * @return the single [PermissionUtils] instance
     */
    fun theme(callback: ThemeCallback): PermissionUtils {
        mThemeCallback = callback
        return this
    }

    /**
     * Start request.
     */
    fun request() {
        mPermissionsGranted = ArrayList()
        mPermissionsRequest = ArrayList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionsGranted!!.addAll(mPermissions)
            requestCallback()
        } else {
            for (permission in mPermissions) {
                if (isGranted(permission)) {
                    mPermissionsGranted!!.add(permission)
                } else {
                    mPermissionsRequest!!.add(permission)
                }
            }
            if (mPermissionsRequest!!.isEmpty()) {
                requestCallback()
            } else {
                startPermissionActivity()
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun startPermissionActivity() {
        mPermissionsDenied = ArrayList()
        mPermissionsDeniedForever = ArrayList()
        PermissionActivity.start(mApp!!.get()!!)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun rationale(activity: Activity): Boolean {
        var isRationale = false
        if (mOnRationaleListener != null) {
            for (permission in mPermissionsRequest!!) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    getPermissionsStatus(activity)
                    mOnRationaleListener!!.rationale(object : OnRationaleListener.ShouldRequest {
                        override fun again(again: Boolean) {
                            if (again) {
                                startPermissionActivity()
                            } else {
                                requestCallback()
                            }
                        }
                    })
                    isRationale = true
                    break
                }
            }
            mOnRationaleListener = null
        }
        return isRationale
    }

    private fun getPermissionsStatus(activity: Activity) {
        for (permission in mPermissionsRequest!!) {
            if (isGranted(permission)) {
                mPermissionsGranted!!.add(permission)
            } else {
                mPermissionsDenied!!.add(permission)
                if (!activity.shouldShowRequestPermissionRationale(permission)) {
                    mPermissionsDeniedForever!!.add(permission)
                }
            }
        }
    }

    private fun requestCallback() {
        if (mSimpleCallback != null) {
            if (mPermissionsRequest!!.size == 0 || mPermissions.size == mPermissionsGranted!!.size) {
                mSimpleCallback!!.onGranted()
            } else {
                if (mPermissionsDenied!!.isNotEmpty()) {
                    mSimpleCallback!!.onDenied()
                }
            }
            mSimpleCallback = null
        }
        if (mFullCallback != null) {
            if (mPermissionsRequest!!.size == 0 || mPermissions.size == mPermissionsGranted!!.size) {
                mFullCallback!!.onGranted(mPermissionsGranted!!)
            } else {
                if (mPermissionsDenied!!.isNotEmpty()) {
                    mFullCallback!!.onDenied(mPermissionsDeniedForever, mPermissionsDenied!!)
                }
            }
            mFullCallback = null
        }
        mOnRationaleListener = null
        mThemeCallback = null
    }

    private fun onRequestPermissionsResult(activity: Activity) {
        getPermissionsStatus(activity)
        requestCallback()
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    class PermissionActivity : Activity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
            window.statusBarColor = Color.TRANSPARENT
            if (sInstance == null) {
                super.onCreate(savedInstanceState)
                Log.e("PermissionUtils", "request permissions failed")
                finish()
                return
            }
            if (sInstance!!.mThemeCallback != null) {
                sInstance!!.mThemeCallback!!.onActivityCreate(this)
            }
            super.onCreate(savedInstanceState)

            if (sInstance!!.rationale(this)) {
                finish()
                return
            }
            if (sInstance!!.mPermissionsRequest != null) {
                val size = sInstance!!.mPermissionsRequest!!.size
                if (size <= 0) {
                    finish()
                    return
                }
                requestPermissions(sInstance!!.mPermissionsRequest!!.toTypedArray(), 1)
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int,
                                                permissions: Array<String>,
                                                grantResults: IntArray) {
            sInstance!!.onRequestPermissionsResult(this)
            finish()
        }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            finish()
            return true
        }

        companion object {

            fun start(context: Context) {
                val starter = Intent(context, PermissionActivity::class.java)
                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(starter)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////

    interface OnRationaleListener {

        fun rationale(shouldRequest: ShouldRequest)

        interface ShouldRequest {
            fun again(again: Boolean)
        }
    }

    interface SimpleCallback {
        fun onGranted()

        fun onDenied()
    }

    interface FullCallback {
        fun onGranted(permissionsGranted: ArrayList<String>)

        fun onDenied(permissionsDeniedForever: ArrayList<String>?, permissionsDenied: ArrayList<String>)
    }

    interface ThemeCallback {
        fun onActivityCreate(activity: Activity)
    }

    companion object {

        private var PERMISSIONS: ArrayList<String>? = null

        private var sInstance: PermissionUtils? = null
        private var mApp: WeakReference<Context>? = null

        /**
         * Return the permissions used in application.
         *
         * @return the permissions used in application
         */
        val permissions: ArrayList<String>
            get() = getPermissions(mApp!!.get()!!.packageName)


        /**
         * Return the permissions used in application.
         *
         * @param packageName The name of the package.
         * @return the permissions used in application
         */
        fun getPermissions(packageName: String): ArrayList<String> {
            val pm = mApp!!.get()!!.packageManager
            return try {
                val per = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                        .requestedPermissions
                if (per != null) {
                    per.toList() as ArrayList<String>
                } else {
                    ArrayList()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                arrayListOf()
            }

        }

        /**
         * Return whether *you* have granted the permissions.
         *
         * @param permissions The permissions.
         * @return `true`: yes<br></br>`false`: no
         */
        fun isGranted(vararg permissions: String): Boolean {
            for (permission in permissions) {
                if (!isGranted(permission)) {
                    return false
                }
            }
            return true
        }

        private fun isGranted(permission: String): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(mApp!!.get()!!, permission)
        }

        /**
         * Launch the application's details settings.
         */
        fun launchAppDetailsSettings() {
            val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
            intent.data = Uri.parse("package:" + mApp!!.get()!!.packageName)
            mApp!!.get()!!.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        /**
         * Set the permissions.
         *
         * @param permissions The permissions.
         * @return the single [PermissionUtils] instance
         */
        fun permission(mContext: Context, @PermissionConstants.Permission vararg permissions: String
        ): PermissionUtils {
            mApp = WeakReference(mContext)
            PERMISSIONS = PermissionUtils.permissions
            return PermissionUtils(*permissions)
        }
    }
}