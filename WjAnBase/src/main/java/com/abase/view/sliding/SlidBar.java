/*
 * Copyright (C) 2012 www.amsoft.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abase.view.sliding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.abase.adapter.AbViewPagerAdapter;
import com.abase.util.AbViewUtil;
import com.abase.view.viewpager.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：可播放显示的View.
 */

public class SlidBar extends RelativeLayout {
    private boolean isgFirst=false;
    /** 上下文. */
    private Context context;

    /** 内部的ViewPager. */
    private ViewPager mViewPager;


    /** 计数. */
    private int count,position;

    /** 点击. */
    private AbOnItemClickListener mOnItemClickListener;

    /** 改变. */
    private AbOnChangeListener mAbChangeListener;

    /** 滚动. */
    private AbOnScrollListener mAbScrolledListener;

    /** 触摸. */
    private AbOnTouchListener mAbOnTouchListener;

    /** List views. */
    private ArrayList<View> mListViews = null;

    /** 适配器. */
    private AbViewPagerAdapter mAbViewPagerAdapter = null;

    /*导航方向*/
    private int navHorizontalGravity=10000;

    /** 播放的方向. */
    private int playingDirection = 0;

    /*导航点*/
    private CirclePageIndicator circlePageIndicator;

    /** 播放的开关. */
    private boolean play = false;
    public boolean isPlay() {
        return play;
    }

    public void setPlay(boolean play) {
        this.play = play;
    }

    /**轮播时间*/
    private int time=5000;
    private boolean isTouch=false;

    /**获取导航条*/
    public CirclePageIndicator getCirclePageIndicator() {
        return circlePageIndicator;
    }

    public ArrayList<View> getmListViews() {
        return mListViews;
    }

    /**
     * 创建一个AbSlidingPlayView.
     *
     * @param context the context
     */
    public SlidBar(Context context) {
        super(context);
        initView(context);
    }

    /**
     * 从xml初始化的AbSlidingPlayView.
     *
     * @param context the context
     * @param attrs the attrs
     */
    public SlidBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /** 用与轮换的 handler. */
    @SuppressLint("HandlerLeak") private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            if (msg.what==0) {
                if(!play){
                    handler.removeCallbacks(runnable);
                    return;
                }
                if(isTouch){
                    handler.postDelayed(runnable, time);
                    return;
                }
                int count = mListViews.size();
                int i = mViewPager.getCurrentItem();
                if(playingDirection==0){
                    if(i == count-1){
                        i=0;
                    }else{
                        i++;
                    }
                }else{
                    if(i == 0){
                        i=count-1;
                    }else{
                        i--;
                    }
                }
                mViewPager.setCurrentItem(i, true);
                if(play){
                    handler.postDelayed(runnable, time);
                }
            }
        }
    };

    /**
     * 描述：初始化这个View.
     *
     * @param context the context
     */
    public void initView(Context context){
        this.context = context;
        this.setBackgroundColor(Color.rgb(255, 255, 255));
//        this.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        mViewPager = new ViewPager(context);
        //手动创建的ViewPager,如果用fragment必须调用setId()方法设置一个id
//        mViewPager.setId(1985);
        //导航的点
        circlePageIndicator=new CirclePageIndicator(context);
        circlePageIndicator.setPadding(5,5,5,5);
        circlePageIndicator.setFillColor(Color.WHITE);
        circlePageIndicator.setPageColor(Color.LTGRAY);
        circlePageIndicator.setRadius(5);
        circlePageIndicator.setFillSize(6);
        circlePageIndicator.setStrokeWidth(0);

        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp1.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        this.addView(mViewPager,lp1);


        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, AbViewUtil.dp2px(context,15));
        circlePageIndicator.setLayoutParams(lp2);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
        lp2.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        if(navHorizontalGravity!=10000){
            lp2.addRule(navHorizontalGravity,RelativeLayout.TRUE);
        }
        lp2.leftMargin= AbViewUtil.dp2px(context,5);
        this.addView(circlePageIndicator,lp2);

        mListViews = new ArrayList<View>();
        mAbViewPagerAdapter = new AbViewPagerAdapter(context,mListViews);
        mViewPager.setAdapter(mAbViewPagerAdapter);
        mViewPager.setFadingEdgeLength(0);
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                SlidBar.this.position=position;
                onPageSelectedCallBack(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position==count-1&&isgFirst){
                    if(null!=mAbScrolledListener){
                        mAbScrolledListener.onScrollToRight();
                    }
                }
                if(position<count-1){
                    isgFirst=false;
                }else if(position==count-1){
                    isgFirst=true;
                }
                onPageScrolledCallBack(position);
            }
        });
    }



    /**
     * 描述：添加可播放视图.
     *
     * @param view the view
     */
    public void addView(View view){
        mListViews.add(view);
        if(view instanceof AbsListView){
        }else{
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(mOnItemClickListener!=null){
                        mOnItemClickListener.onClick(position);
                    }
                }
            });
            view.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if(mAbOnTouchListener!=null){
                        mAbOnTouchListener.onTouch(event);
                    }
                    return false;
                }
            });
        }
        count=mListViews.size();
        mAbViewPagerAdapter.notifyDataSetChanged();
        setIndexs(count,mViewPager);
    }

    /**
     * 描述：添加可播放视图列表.
     *
     * @param views the views
     */
    public void addViews(List<View> views){
        mListViews.addAll(views);
        for(View view:views){
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(mOnItemClickListener!=null){
                        mOnItemClickListener.onClick(position);
                    }
                }
            });

            view.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if(mAbOnTouchListener!=null){
                        mAbOnTouchListener.onTouch(event);
                    }
                    return false;
                }
            });
        }
        count=mListViews.size();
        mAbViewPagerAdapter.notifyDataSetChanged();
        setIndexs(count,mViewPager);
    }
    /*设置导航点*/
    private void setIndexs(int count, ViewPager viewPager){
        if(count<=1){
            circlePageIndicator.setVisibility(GONE);
        }else{
            circlePageIndicator.setPager_count(count);
            circlePageIndicator.setViewPager(viewPager);
            circlePageIndicator.setVisibility(VISIBLE);
        }


    }
    /*隐藏导航点*/
    public void hintIndex(){
        circlePageIndicator.setVisibility(View.GONE);
    }

    /**
     * 设置导航点
     * @param navHorizontalGravity RelativeLayout的相对位置设置
     */
    public void setNavHorizontalGravity(int navHorizontalGravity){
        if(navHorizontalGravity!=10000){
            RelativeLayout.LayoutParams lp2 = (LayoutParams) circlePageIndicator.getLayoutParams();
            lp2.addRule(navHorizontalGravity,RelativeLayout.TRUE);
            circlePageIndicator.setLayoutParams(lp2);
        }else{
            this.navHorizontalGravity=navHorizontalGravity;
        }
    }
    /**
     * 描述：删除可播放视图.
     */
    @Override
    public void removeAllViews(){
        mListViews.clear();
        count=mListViews.size();
        mAbViewPagerAdapter.notifyDataSetChanged();
        setIndexs(count,mViewPager);
    }



    /**
     * 描述：设置页面切换事件.
     *
     * @param position the position
     */
    private void onPageScrolledCallBack(int position) {
        if(mAbScrolledListener!=null){
            mAbScrolledListener.onScroll(position);
        }

    }

    /**
     * 描述：设置页面切换事件.
     *
     * @param position the position
     */
    private void onPageSelectedCallBack(int position) {
        if(mAbChangeListener!=null){
            mAbChangeListener.onChange(position);
        }

    }




    /** 用于轮播的线程. */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(mViewPager!=null){
                handler.sendEmptyMessage(0);
            }
        }
    };


    /**
     * 描述：自动轮播.
     */
    public void startPlay(){
        if(handler!=null && !play){
            play  = true;
            handler.postDelayed(runnable,time);
        }
    }

    /**
     * 描述：自动轮播.
     */
    public void stopPlay(){
        if(handler!=null){
            play  = false;
            handler.removeCallbacks(runnable);
        }
        position=0;
    }

    /**
     * 设置点击事件监听.
     *
     * @param onItemClickListener the new on item click listener
     */
    public void setOnItemClickListener(AbOnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    /**
     * 描述：设置页面切换的监听器.
     *
     * @param abChangeListener the new on page change listener
     */
    public void setOnPageChangeListener(AbOnChangeListener abChangeListener) {
        mAbChangeListener = abChangeListener;
    }

    /**
     * 描述：设置页面滑动的监听器.
     *
     * @param abScrolledListener the new on page scrolled listener
     */
    public void setOnPageScrolledListener(AbOnScrollListener abScrolledListener) {
        mAbScrolledListener = abScrolledListener;
    }

    /**
     * 描述：设置页面Touch的监听器.
     *
     * @param abOnTouchListener the new on touch listener
     */
    public void setOnTouchListener(AbOnTouchListener abOnTouchListener){
        mAbOnTouchListener = abOnTouchListener;
    }


    /**
     * 描述：获取这个滑动的ViewPager类.
     *
     * @return the view pager
     */
    public ViewPager getViewPager() {
        return mViewPager;
    }

    /**
     * 描述：获取当前的View的数量.
     *
     * @return the count
     */
    public int getCount() {
        return mListViews.size();
    }

    /**
     * 设置图片轮播的时间
     */
    public void setTime(int changtime){
        time=changtime;
    }


    /**
     * 监听器.
     *
     */
    public interface AbOnChangeListener {

        /**
         * 改变.
         * @param position the position
         */
        public void onChange(int position);

    }

    /**
     * 条目点击接口.
     *
     */
    public interface AbOnItemClickListener {

        /**
         * 描述：点击事件.
         * @param position 索引
         */
        public void onClick(int position);
    }

    /**
     * 滚动.
     *
     */
    public interface AbOnScrollListener {

        /**
         * 滚动.
         * @param arg1 返回参数
         */
        public void onScroll(int arg1);

        /**
         * 滚动停止.
         */
        public void onScrollStoped();

        /**
         * 滚到了最左边.
         */
        public void onScrollToLeft();

        /**
         * 滚到了最右边.
         */
        public void onScrollToRight();

    }

    /**
     * 触摸屏幕接口.
     *
     */
    public interface AbOnTouchListener {
        /**
         * 描述：Touch事件.
         *
         * @param event 触摸手势
         */
        public void onTouch(MotionEvent event);
    }
}
