package com.abase.view.parent;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abase.util.AbAppUtil;
import com.abase.util.AbViewUtil;
import com.abase.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.wjabase.R;


/**
 * activity基础类
 *
 * @author Admin
 * @version 1.0
 * @date 2017/4/13
 */

public abstract class BaseActivity extends AppCompatActivity {
    public LinearLayout titlebar, other_down, lin_back;//标题栏
    private LayoutInflater inflater;//view实例接口
    public TextView title_content, backto, other;//标题 返回 新增
    public ImageView other_icon, backto_img;
    public RelativeLayout title;
    public View contentView, title_line,base_view;
    public Activity activity;
    public View title_systembar;
    public RequestManager glide;


    public View getContentView() {
        return contentView;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        before();
        base_view=LayoutInflater.from(this).inflate(R.layout.titlebar,null);
        setContentView(base_view);
        initTitle();//初始加载title
        if (setContentView() != 0) {
//            boolean b = false;
//            int[] attrsArray = {android.R.attr.windowFullscreen};
//            TypedArray typedArray = obtainStyledAttributes(attrsArray);
//            b = typedArray.getBoolean(0, b);
//            typedArray.recycle();
            contentView = inflater.inflate(setContentView(), null);
//            if(b){
            titlebar.addView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//            }else{
//                RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
//                layoutParams.addRule(RelativeLayout.BELOW,R.id.title_line);
//                title.addView(contentView,layoutParams);
//            }

        }
        activity = this;
        init();//子类的操作类
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        glide.onStop();
    }
    /**
     * 设置关闭返回
     */
    public void setBacktoFinsh(@DrawableRes int rif) {
        if (rif != 0) {
            int wh = AbViewUtil.dp2px(activity, 15);
            backto_img.setImageResource(rif);
            backto_img.getLayoutParams().height = wh;
            backto_img.getLayoutParams().width = wh;
        }
    }

    /**
     * 设置关闭返回
     */
    public void setBacktoFinsh(@DrawableRes int rif, int width, int height) {
        if (rif != 0) {
            backto_img.setImageResource(rif);
            backto_img.getLayoutParams().height = width;
            backto_img.getLayoutParams().width = height;
        }
    }


    /**
     * setcontent之前
     */
    public void before() {
    }

    /**
     * 初始化titlebar
     */
    private void initTitle() {
        glide = Glide.with(this);
        inflater = LayoutInflater.from(this);
        lin_back =  findViewById(R.id.lin_back);
        other_down = findViewById(R.id.other_down);
        titlebar = findViewById(R.id.titlebar);
        title_content = findViewById(R.id.title_content);
        backto =  findViewById(R.id.backto);
        backto_img = findViewById(R.id.backto_img);
        other =findViewById(R.id.other);
        other_icon = findViewById(R.id.other_icon);
        title = findViewById(R.id.title);
        title_line = findViewById(R.id.title_line);
        title_systembar = findViewById(R.id.title_systembar);

        //设置事件
        backto.setOnClickListener(parentClick);
        lin_back.setOnClickListener(parentClick);


    }

    /**
     * 设置主题色
     */
    public void setThemeColor(@ColorRes int color) {
        title_systembar.setBackgroundColor(getResources().getColor(color));
        title.setBackgroundColor(getResources().getColor(color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//如果大于4.4 沉浸式导航栏
//            setTranslucentStatus(true);
            boolean b = false;
            int[] attrsArray = {android.R.attr.windowTranslucentStatus};
            TypedArray typedArray = activity.obtainStyledAttributes(attrsArray);
            b = typedArray.getBoolean(0, b);
            typedArray.recycle();
            if (b) {
                ViewGroup.LayoutParams layoutParams=title_systembar.getLayoutParams();
                layoutParams.height= Tools.getStatusBarHeight(activity);
                title_systembar.setLayoutParams(layoutParams);
                title_systembar.setVisibility(View.VISIBLE);
            }
        } else {
            title_systembar.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//大于21 去掉阴影
            getWindow().setStatusBarColor(Color.TRANSPARENT);// SDK21
        }
    }


    /**
     * 设置显示内容
     */
    public abstract int setContentView();

    /**
     * 设置标题之后的操作
     */
    public abstract void init();

    /**
     * 事件
     */
    View.OnClickListener parentClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.backto) {
                finshTo();

            } else if (i == R.id.lin_back) {
                finshTo();
            }
        }
    };

    /**
     * 是否隐藏标题栏
     *
     * @param isHide
     */
    public void setTitleBarShow(boolean isHide) {
        if (!isHide) {
            title_line.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
        } else {
            title_line.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(glide!=null){
            glide.pauseRequests();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Looper.myLooper()==null) {
            Looper.prepare();
        }
        if(glide!=null){
            glide.resumeRequests();
        }
    }

    /**
     * 关闭
     */
    public void finshTo() {
        AbAppUtil.closeSoftInput(activity);
        this.finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 让app的字体不随系统的字体改变
     */
    @Override
    public Resources getResources() {
        try {
            Resources res = super.getResources();
            Configuration config = new Configuration();
            config.setToDefaults();
            res.updateConfiguration(config, res.getDisplayMetrics());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return super.getResources();
        }
    }
}
