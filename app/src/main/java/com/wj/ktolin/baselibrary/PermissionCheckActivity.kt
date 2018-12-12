package com.wj.ktolin.baselibrary

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity

/**
 * Android 6.0 上权限分为**正常**和**危险**级别
 *
 *  * 正常级别权限：开发者仅仅需要在AndroidManifext.xml上声明，那么应用就会被允许拥有该权限，如：android.permission.INTERNET
 *  * 危险级别权限：开发者需要在AndroidManifext.xml上声明，并且在运行时进行申请，而且用户允许了，应用才会被允许拥有该权限，如：android.permission.WRITE_EXTERNAL_STORAGE
 *
 * 有米的以下权限需要在Android6.0上被允许，有米广告sdk才能正常工作，开发者需要在调用有米的任何代码之前，提前让用户允许权限
 *
 *  * 必须申请的权限
 *
 *  * android.permission.READ_PHONE_STATE
 *  * android.permission.WRITE_EXTERNAL_STORAGE
 *
 *
 *
 *
 * @since 2015-12-10 16:36
 */
class PermissionCheckActivity : AppCompatActivity() {


    /**
     * 小tips:这里的int数值不能太大，否则不会弹出请求权限提示，测试的时候,改到1000就不会弹出请求了
     */

    private val WRITE_EXTERNAL_STORAGE_CODE = 102
    private val READSTATUS_CODE = 103
    private val READ_EXTERNAL_STORAGE = 104
    private val ACCESS_FINE_LOCATION = 105
    private var isOpenAllPermissin = false//是否打开所有权限
    /**
     * 有米 Android SDK 所需要向用户申请的权限列表
     */
    private val models = arrayOf(PermissionModel(Manifest.permission.WRITE_EXTERNAL_STORAGE, "我们需要您允许我们读写你的存储卡，以方便我们临时保存一些数据",
            WRITE_EXTERNAL_STORAGE_CODE), PermissionModel(Manifest.permission.READ_EXTERNAL_STORAGE, "我们需要您允许我们读写你的存储卡，以方便我们临时保存一些数据",
            READ_EXTERNAL_STORAGE))

    private val isAllRequestedPermissionGranted: Boolean
        get() {
            for (model in models) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, model.permission!!)) {
                    return false
                }
            }
            return true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 22) {
            checkPermissions()
        }
    }

    /**
     * 跳转
     */
    fun openMainActivity() {
        startActivity<MainActivityBase>()
        finish()
    }


    /**
     * 这里我们演示如何在Android 6.0+上运行时申请权限
     *
     * @param models
     */
    private fun checkPermissions() {
        try {
            for (model in models) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, model.permission!!)) {
                    if (model.permission === Manifest.permission.ACCESS_FINE_LOCATION) {
                        ActivityCompat.requestPermissions(this, arrayOf(model.permission!!, Manifest.permission.ACCESS_COARSE_LOCATION), model.requestCode)
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(model.permission!!), model.requestCode)
                    }
                    return
                }
            }

            // 到这里就表示有米所有需要的权限已经通过申请，权限已经申请就打开demo
            isOpenAllPermissin = true
            openMainActivity()
        } catch (e: Throwable) {

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_CODE, READSTATUS_CODE, READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION -> {
                // 如果用户不允许，我们视情况发起二次请求或者引导用户到应用页面手动打开
                if (grantResults.isNotEmpty() && PackageManager.PERMISSION_GRANTED != grantResults[0]) {

                    // 二次请求，表现为：以前请求过这个权限，但是用户拒接了
                    // 在二次请求的时候，会有一个“不再提示的”checkbox
                    // 因此这里需要给用户解释一下我们为什么需要这个权限，否则用户可能会永久不在激活这个申请
                    // 方便用户理解我们为什么需要这个权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        AlertDialog.Builder(this).setTitle("权限申请").setMessage(findPermissionExplain(permissions[0]))
                                .setPositiveButton("确定") { _, _ -> checkPermissions() }.show()
                    } else {
                        showTip("部分权限被拒绝获取，将会会影响后续功能的使用，建议重新打开")
                        openAppPermissionSetting(123456789)
                    }// 到这里就表示已经是第3+次请求，而且此时用户已经永久拒绝了，这个时候，我们引导用户到应用权限页面，让用户自己手动打开
                    return
                }

                // 到这里就表示用户允许了本次请求，我们继续检查是否还有待申请的权限没有申请
                if (isAllRequestedPermissionGranted) {
                   openMainActivity()
                } else {
                    checkPermissions()
                }
            }
        }
    }

    private fun findPermissionExplain(permission: String): String? {
        for (model in models) {
            if (model.permission != null && model.permission == permission) {
                return model.explain
            }
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {

            123456789 -> if (isAllRequestedPermissionGranted) {
            } else {
                Toast.makeText(this, "部分权限被拒绝获取，退出", Toast.LENGTH_LONG).show()
                this.finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openAppPermissionSetting(requestCode: Int): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.packageName))
            intent.addCategory(Intent.CATEGORY_DEFAULT)

            // Android L 之后Activity的启动模式发生了一些变化
            // 如果用了下面的 Intent.FLAG_ACTIVITY_NEW_TASK ，并且是 startActivityForResult
            // 那么会在打开新的activity的时候就会立即回调 onActivityResult
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivityForResult(intent, requestCode)
            return true
        } catch (e: Throwable) {
        }

        return false
    }


//    //防止用户返回键退出 APP
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK
//                || keyCode == KeyEvent.KEYCODE_HOME) {
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    class PermissionModel(
            /**
             * 请求的权限
             */
            var permission: String?,
            /**
             * 解析为什么请求这个权限
             */
            var explain: String,
            /**
             * 请求代码
             */
            var requestCode: Int)

}
