package com.wj.ui.view.parent

import com.wj.util.AbViewUtil.dp2px
import com.wj.util.Tools.getStatusBarHeight
import com.wj.util.AbAppUtil.closeSoftInput
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.view.View
import android.app.Activity
import com.bumptech.glide.RequestManager
import android.os.Bundle
import java.lang.Exception
import com.bumptech.glide.Glide
import android.os.Build
import android.graphics.Color
import android.os.Looper
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.wj.ktutils.R

/**
 * activity基础类
 *
 * @author Admin
 * @version 1.0
 * @date 2017/4/13
 */
abstract class BaseActivity : AppCompatActivity() {
    lateinit var titlebar: LinearLayout
    lateinit var other_down: LinearLayout
    lateinit var lin_back //标题栏
            : LinearLayout
    lateinit var inflater //view实例接口
            : LayoutInflater
    lateinit var title_content: TextView
    lateinit var backto: TextView
    lateinit var other //标题 返回 新增
            : TextView
    lateinit var other_icon: ImageView
    lateinit var backto_img: ImageView
    lateinit var title: RelativeLayout
    lateinit var mContentView: View
    lateinit var title_line: View
    lateinit var base_view: View
    lateinit var activity: Activity
    lateinit var title_systembar: View
    var glide: RequestManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        before()
        base_view = LayoutInflater.from(this).inflate(R.layout.titlebar, null)
        setContentView(base_view)
        initTitle() //初始加载title
        if (setContentView() != 0) {
//            boolean b = false;
//            int[] attrsArray = {android.R.attr.windowFullscreen};
//            TypedArray typedArray = obtainStyledAttributes(attrsArray);
//            b = typedArray.getBoolean(0, b);
//            typedArray.recycle();
            mContentView = inflater.inflate(setContentView(), null)
            //            if(b){
            titlebar.addView(mContentView,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT))
            //            }else{
//                RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
//                layoutParams.addRule(RelativeLayout.BELOW,R.id.title_line);
//                title.addView(contentView,layoutParams);
//            }
        }
        activity = this
        init() //子类的操作类
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        glide?.onStop()
    }

    /**
     * 设置关闭返回
     */
    fun setBackFinish(@DrawableRes rif: Int) {
        if (rif != 0) {
            val wh = dp2px(activity, 15f)
            backto_img.setImageResource(rif)
            backto_img.layoutParams.height = wh
            backto_img.layoutParams.width = wh
        }
    }

    /**
     * 设置关闭返回
     */
    fun setBackFinish(@DrawableRes rif: Int, width: Int, height: Int) {
        if (rif != 0) {
            backto_img.setImageResource(rif)
            backto_img.layoutParams.height = width
            backto_img.layoutParams.width = height
        }
    }

    /**
     * setcontent之前
     */
    open fun before() {}

    /**
     * 初始化titlebar
     */
    private fun initTitle() {
        glide = Glide.with(this)
        inflater = LayoutInflater.from(this)
        lin_back = findViewById(R.id.lin_back)
        other_down = findViewById(R.id.other_down)
        titlebar = findViewById(R.id.titlebar)
        title_content = findViewById(R.id.title_content)
        backto = findViewById(R.id.backto)
        backto_img = findViewById(R.id.backto_img)
        other = findViewById(R.id.other)
        other_icon = findViewById(R.id.other_icon)
        title = findViewById(R.id.title)
        title_line = findViewById(R.id.title_line)
        title_systembar = findViewById(R.id.title_systembar)

        //设置事件
        backto.setOnClickListener(parentClick)
        lin_back.setOnClickListener(parentClick)
    }

    /**
     * 设置主题色
     */
    fun setThemeColor(@ColorRes color: Int) {
        title_systembar.setBackgroundColor(resources.getColor(color))
        title.setBackgroundColor(resources.getColor(color))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //如果大于4.4 沉浸式导航栏
//            setTranslucentStatus(true);
            var b = false
            val attrsArray = intArrayOf(android.R.attr.windowTranslucentStatus)
            val typedArray = activity.obtainStyledAttributes(attrsArray)
            b = typedArray.getBoolean(0, b)
            typedArray.recycle()
            if (b) {
                val layoutParams = title_systembar.layoutParams
                layoutParams.height = getStatusBarHeight(activity)
                title_systembar.layoutParams = layoutParams
                title_systembar.visibility = View.VISIBLE
            }
        } else {
            title_systembar.visibility = View.GONE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //大于21 去掉阴影
            window.statusBarColor = Color.TRANSPARENT // SDK21
        }
    }

    /**
     * 设置显示内容
     */
    abstract fun setContentView(): Int

    /**
     * 设置标题之后的操作
     */
    abstract fun init()

    /**
     * 事件
     */
    var parentClick = View.OnClickListener { v ->
        val i = v.id
        if (i == R.id.backto) {
            finishTo()
        } else if (i == R.id.lin_back) {
            finishTo()
        }
    }

    /**
     * 是否隐藏标题栏
     *
     * @param isHide
     */
    fun setTitleBarShow(isHide: Boolean) {
        if (!isHide) {
            title_line.visibility = View.GONE
            title.visibility = View.GONE
        } else {
            title_line.visibility = View.VISIBLE
            title.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        glide?.pauseRequests()
    }

    override fun onResume() {
        super.onResume()
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }
        glide?.resumeRequests()
    }

    /**
     * 关闭
     */
    fun finishTo() {
        closeSoftInput(activity)
        finish()
    } //    @Override
    //    public void onConfigurationChanged(Configuration newConfig) {
    //        if (newConfig.fontScale != 1)//非默认值
    //            getResources();
    //        super.onConfigurationChanged(newConfig);
    //    }
    //    /**
    //     * 让app的字体不随系统的字体改变
    //     */
    //    @Override
    //    public Resources getResources() {
    //        try {
    //            Resources res = super.getResources();
    //            Configuration config = new Configuration();
    //            config.setToDefaults();
    //            res.updateConfiguration(config, res.getDisplayMetrics());
    //            return res;
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            return super.getResources();
    //        }
    //    }
}