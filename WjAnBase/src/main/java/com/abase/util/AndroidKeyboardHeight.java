package com.abase.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.lang.reflect.Field;

/**
 * 自适应弹出键盘
 * @author wangjun
 * @version 1.0
 * @date 2016/12/5
 */

public class AndroidKeyboardHeight {
    public static void assistActivity(Activity activity) {
        new AndroidKeyboardHeight(activity);
    }
    public static void assistActivity(Activity activity, View view) {
        new AndroidKeyboardHeight(activity,view);
    }
    private View mChildOfContent;
    private int usableHeightPrevious;
    private ViewGroup.LayoutParams frameLayoutParams;
    private int contentHeight;
    private   boolean isfirst = true;
    private   Activity activity;
    private  int statusBarHeight;
    public  int[] wh;// 屏幕的宽和高
    private  View view;
    private View rootView;

    private AndroidKeyboardHeight(Activity activity) {
        this.activity = activity;
        init();
    }
    private AndroidKeyboardHeight(Activity activity,View view) {
        this.view=view;
        this.activity = activity;
        init();
    }
    private void init(){
        //获取状态栏的高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        FrameLayout content = (FrameLayout)activity.findViewById(android.R.id.content);
        rootView = content.getChildAt(0);
        if(view==null){
            mChildOfContent=rootView;
        }else{
            mChildOfContent=view;
        }


        //界面出现变动都会调用这个监听事件
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (isfirst) {
                    frameLayoutParams = mChildOfContent.getLayoutParams();
                    contentHeight = mChildOfContent.getHeight();//兼容华为等机型
                    isfirst = false;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if(!isfirst){
                    possiblyResizeChildOfContent();
                }
            }
        });
    }
    //重新调整跟布局的高度
    private void possiblyResizeChildOfContent() {

        int usableHeightNow = computeUsableHeight();

        //当前可见高度和上一次可见高度不一致 布局变动
        if (usableHeightNow != usableHeightPrevious) {
            //int usableHeightSansKeyboard2 = mChildOfContent.getHeight();//兼容华为等机型
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                if (usableHeightSansKeyboard-getScreenWH(mChildOfContent.getContext())[1]!=statusBarHeight){
                    //frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference + statusBarHeight;
                }else if(isTranslucentStatus()){
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference + statusBarHeight;
                } else {
                    frameLayoutParams.height = usableHeightSansKeyboard -heightDifference;
                }
            } else {
                frameLayoutParams.height = contentHeight;
            }

            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    /**     * 计算mChildOfContent可见高度     ** @return     */
    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

    /**
     * 是否是状态透明的style
     * @return
     */
    private boolean isTranslucentStatus(){
        boolean b = false;
        int[] attrsArray = {android.R.attr.windowTranslucentStatus};
        TypedArray typedArray = activity.obtainStyledAttributes(attrsArray);
        b = typedArray.getBoolean(0, b);
        typedArray.recycle();
        return b;
    }

    /**
     * 获取屏幕大小
     *
     *            1是宽 2是高
     */
    public int[] getScreenWH(Context context) {
        if (wh != null && wh[0] != 0 && wh[1] != 0) {
            return wh;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        int width = 0;
        int height = 0;
        width = displayMetrics.widthPixels;

        height = displayMetrics.heightPixels - getStatusBarHeight(context);// 去掉通知栏的高度
        int[] is = { width, height };
        wh = is;
        return is;
    }

    /**
     * 获取通知栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }
}