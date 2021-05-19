package com.abase.view.parent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.WindowManager;

import com.abase.util.Tools;
import com.abase.view.weight.LoadWeb;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网页浏览器
 * @author Administrator
 * @version 1.0
 * @date 2018/10/28/028
 */
public class BaseWebActivity extends BaseActivity{

    @Override
    public void before() {
        super.before();
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public int setContentView() {
        return 0;
    }

    @Override
    public void init() {

    }


}
