package com.wj.ktolin.baselibrary

import android.Manifest
import android.graphics.PixelFormat
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.wj.ktolin.baselibrary.databinding.ActivityMainBinding
import com.wj.okhttp.OhFileCallBakListener
import com.wj.okhttp.OhHttpClient
import com.wj.okhttp.OhObjectListener
import com.wj.okhttp.util.DownLoad
import com.wj.util.AbAppUtil
import com.wj.util.AbDoubleTool
import com.wj.util.ToastUtil
import com.wj.ui.view.parent.BaseActivity
import com.wj.ui.view.weight.RecyclerSpace
import com.wj.ktolin.baselibrary.weight.TestAdapter
import com.wj.ktutils.WjSP
import com.wj.permission.PermissionUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.util.*


class MainActivityBase : BaseActivity(), View.OnClickListener {

    val binding: ActivityMainBinding by lazy { ActivityMainBinding.bind(mContentView!!) }
    override fun setContentView(): Int {
        return R.layout.activity_main
    }

    private var cpc: Button? = null
    private var downLoad: DownLoad? = null
    private var textView: TextView? = null

    override fun init() {
        window.setFormat(PixelFormat.TRANSLUCENT)
//        AndroidKeyboardHeight.assistActivity(this)
        val start = findViewById<Button>(R.id.start)
        val pause = findViewById<Button>(R.id.pause)
        val stop = findViewById<Button>(R.id.stop)
        textView = findViewById(R.id.progress)

        start.setOnClickListener(this)
        pause.setOnClickListener(this)
        stop.setOnClickListener(this)

        binding.recyclerList.apply {
            addItemDecoration(RecyclerSpace(2,
                resources.getColor(R.color.colorPrimary)))
            adapter = TestAdapter()
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@MainActivityBase, 6)
        }

//        QqWebHelper.X5Init(this)
//        web.loadUrl("https://baidu.com")
//        web.webMethodsListener=object : WebMethodsListener(){
//            override fun onX5GeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissionsCallback?): Boolean {
//                callback!!.invoke(origin, true, false)
//                return true
//            }
//        }
//        val intent = Intent("adasa")
//        intent.putExtra("data","asdas")
//        NotificationUtils(activity).setContentIntent(PendingIntent.getBroadcast(activity, (System.currentTimeMillis() / 1000).toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT))
//                .sendNotification((System.currentTimeMillis()/1000).toInt(),"","11111", R.mipmap.ic_launcher,  R.mipmap.ic_launcher)
//        Glide.with(this).load("https://n.sinaimg.cn/fashion/crawl/162/w550h412/20190814/594a-icapxpi5137199.jpg").preload()
//        WjSP.init(activity).setValues("123","1231")
        val result = WjSP.init(activity).getValues("123", "")
        println(result + " ---------------------- ")
        OhHttpClient.init.setLogcat()
        OhHttpClient.init.get("https://www.baidu.com", object : OhObjectListener<String>() {
            override fun onFailure(code: Int, content: String?, error: Throwable?) {
            }

            override fun onSuccess(content: String) {
            }

            override fun onFinish() {
            }

        })

        Thread {
            val client =
                OkHttpClient().newBuilder().addNetworkInterceptor(OhHttpClient.init.logging!!)
                    .build()
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val requestBody = FormBody.Builder()
            requestBody.add("uid", "51688448329")
            requestBody.add("guid", "d6f3ad25-b676-4ace-8d29-5f8da765532c")
            requestBody.add("username", "godlike3471")
            requestBody.add("task_id", "5608")
            requestBody.add("type", "1")
            requestBody.add("_csrftoken", "")
            requestBody.add("account_id", "3530")
            val body = requestBody.build()
            val request: Request = Request.Builder()
                .url("https://apiv2.kstarup.com/v2/tasks/uri")
                .method("POST", body)
                .addHeader("Authorization",
                    "bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FwaXYyLmtzdGFydXAuY29tL3YyL2F1dGgvcmVmcmVzaCIsImlhdCI6MTY1NTQ3MjMyNSwiZXhwIjoxNjU2MTQzMDQwLCJuYmYiOjE2NTU4ODM4NDAsImp0aSI6IlZaZlRFNU9GOTF6ZkpSV1UiLCJzdWIiOiIzMTIiLCJwcnYiOiIxZDBhMDIwYWNmNWM0YjZjNDk3OTg5ZGYxYWJmMGZiZDRlOGM4ZDYzIn0.K9U28-183li0EfOSKUkWkCujS1OVL8h9pSnwdz9-aRg")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
            val response = client.newCall(request).execute()
            println(response.body!!.string() + " ------------------- " + response.code)
        }.start()

    }

    //    override fun before() {
//        sonicFast(this, 1, "http://baidu.com")
//    }
    override fun onClick(view: View) {
        when (view.id) {
            R.id.start -> downLoad = OhHttpClient.init.downFile(this@MainActivityBase,
                "http://imtt.dd.qq.com/16891/apk/5BF428A44C92DDE2AEB3EE34E1785EB6.apk?fsname=com.wj.makebai_v1.5.1_38.apk&hsr=4d5s",
                object : OhFileCallBakListener() {
                    override fun onSuccess(content: String) {
                        AbAppUtil.installApk(this@MainActivityBase, File(content))
                    }

                    override fun onFailure(code: String, content: String) {

                    }

                    override fun onError(e: Exception) {

                    }

                    override fun onFinish() {

                    }

                    override fun onRequestProgress(
                        bytesWritten: Long,
                        contentLength: Long,
                        done: Boolean,
                    ) {
                        textView!!.text =
                            AbDoubleTool.div(bytesWritten.toDouble(), contentLength.toDouble())
                                .toString() + "%"
                    }
                })
            R.id.pause -> {
                ToastUtil.showTip(this, "asdasdasds")
                ToastUtil.showTip(this, "改变")
                if (downLoad != null) {
                    downLoad!!.cancle()
                }
            }
            R.id.stop -> {
                if (downLoad != null) {
                    downLoad!!.stop()
                }
//                OhHttpClient.getInit().isJsonFromMat=false
//                OhHttpClient.getInit().post("https://app-api-ali.zysc.dchost.cn/v1/content/recommend/palyer/recommend/more.api?authCode=5AFC725FAA7B4220AA3B6A4DD5A142C0&access_key=bcc00105941a4a128b5ffbb5ceed9794&areaCode=0001%2F0002%2F", OhHttpParams().put("name","sdf dasd12#"),object : OhObjectListener<String>(){
//                    override fun onFailure(code: Int, content: String?, error: Throwable?) {
//                    }
//
//                    override fun onSuccess(content: String?) {
//                    }
//
//                    override fun onFinish() {
//                    }
//
//                })
                PermissionUtils.permission(
                    activity!!,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                    .rationale(object : PermissionUtils.OnRationaleListener {
                        override fun rationale(shouldRequest: PermissionUtils.OnRationaleListener.ShouldRequest) {
                            shouldRequest.again(true)
                        }
                    })
                    .callback(object : PermissionUtils.FullCallback {
                        override fun onGranted(permissionsGranted: ArrayList<String>) {
                        }

                        override fun onDenied(
                            permissionsDeniedForever: ArrayList<String>?,
                            permissionsDenied: ArrayList<String>,
                        ) {
                        }

                    }).request()

            }
        }
    }
}
