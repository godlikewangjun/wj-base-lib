package com.wj.ktolin.baselibrary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.abase.okhttp.OhObjectListener;
import com.abase.util.AbStrUtil;
import com.abase.util.Tools;
import com.abase.view.parent.BaseActivity;
import com.abase.view.weight.MyDialog;
import com.wj.ktolin.baselibrary.weight.LoadWeb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * 分享详情界面
 *
 * @author Admin
 * @version 1.0
 * @date 2017/5/6
 */

public class ShareDetail extends BaseActivity implements View.OnClickListener {
    private View view0, view1;
    private ShareDetailModel detailModel;
    private ShareWzModel shareWzModel;
    private ProgressDialog progressDialog;
    private Handler handler = new Handler();
    private LoadWeb loadWeb;

    @Override
    public int setContentView() {
        return R.layout.sharedetail_layout;
    }

    @Override
    public void afertOp() {
        title_content.setText("分享红包");
        backto.setText("返回");
        setThemeColor(android.R.color.holo_red_dark);


        init();

    }

    /**
     * 初始化
     */
    private void init() {
        loadWeb = (LoadWeb) findViewById(R.id.web);
        view1 = findViewById(R.id.view1);
        view0 = findViewById(R.id.view0);

        Intent intent = getIntent();
        if (intent != null) {
            shareWzModel = intent.getParcelableExtra("data");
        }
        //获取文章详情
        loadWeb.setUrl(shareWzModel.getDetailurl());

        view1.setOnClickListener(this);
        view0.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.view0://微信好友
                view0.setEnabled(false);
//                startUpPlugin(detailModel, 0);
//                SharePluginTool.startPlugin(activity,detailModel,0,platformActionListener);
                break;
            case R.id.view1://微信朋友圈
                view1.setEnabled(false);
//                startUpPlugin(detailModel, 1);
//                startPlugin(detailModel, 1);
//                SharePluginTool.startPlugin(activity,detailModel,1,platformActionListener);
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (view0 != null) {
            view0.setEnabled(true);
            view1.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
