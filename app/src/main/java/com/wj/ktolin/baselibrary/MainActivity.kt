package com.wj.ktolin.baselibrary

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.abase.okhttp.OhFileCallBakListener
import com.abase.okhttp.OhHttpClient
import com.abase.okhttp.util.DownLoad
import com.abase.util.AbAppUtil
import com.abase.util.AbDoubleTool
import com.abase.util.ToastUtil
import com.abase.util.sql.SqlTool
import com.abase.view.weight.RecyclerSpace
import com.wj.ktolin.baselibrary.weight.TestAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var cpc: Button? = null
    private var downLoad: DownLoad? = null
    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        AndroidKeyboardHeight.assistActivity(this)
        val start = findViewById<Button>(R.id.start)
        val pause = findViewById<Button>(R.id.pause)
        val stop = findViewById<Button>(R.id.stop)
        textView = findViewById(R.id.progress)

        start.setOnClickListener(this)
        pause.setOnClickListener(this)
        stop.setOnClickListener(this)

        recycler_list.adapter= TestAdapter()
        recycler_list.layoutManager= GridLayoutManager(this,6)
        recycler_list.addItemDecoration(RecyclerSpace(2,resources.getColor(R.color.colorAccent)))

        println(SqlTool.createTable(TestMode::class.java)+" ==================== ")

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.start -> downLoad = OhHttpClient.getInit().downFile(this@MainActivity, "http://comsystem.webfile.test.mxlemon.com/api/file/GetSource//8637a28e8e2546a092458fbd6630c68b?buskey=67f777d627394fae8a62fb5917046e3a&buscode=0e863d3893054d718fc43c20e01a454f&timespan=1523505607&sign=1f3d0c6691b80cd25e2d86068b7eb216", object : OhFileCallBakListener() {
                override fun onSuccess(content: String) {
                   AbAppUtil.installApk(this@MainActivity, File(content))
                }

                override fun onFailure(code: String, content: String) {

                }

                override fun onError(e: Exception) {

                }

                override fun onFinish() {

                }

                override fun onRequestProgress(bytesWritten: Long, contentLength: Long, done: Boolean) {
                    textView!!.text = AbDoubleTool.div(bytesWritten.toDouble(), contentLength.toDouble()).toString() + "%"
                }
            })
            R.id.pause -> {
                ToastUtil.showTip(this,"asdasdasds")
                ToastUtil.showTip(this,"改变")
                if (downLoad != null) {
                    downLoad!!.cancle()
                }
            }
            R.id.stop -> if (downLoad != null) {
                downLoad!!.stop()
            }
        }
    }
}
