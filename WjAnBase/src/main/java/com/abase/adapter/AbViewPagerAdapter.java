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
package com.abase.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * fragment 适配
 * @author wangjun
 * @version 1.0
 * @date 2016/8/1
 */
public class AbViewPagerAdapter extends PagerAdapter{
	
	/** The m list views. */
	private ArrayList<View> mListViews = null;
	
	private View v;
    private int tabNum;

	/**
	 * Instantiates a new ab view pager adapter.
	 * @param context the context
	 * @param mListViews the m list views
	 */
	@SuppressLint("UseSparseArrays")
	public AbViewPagerAdapter(Context context,ArrayList<View> mListViews) {
		this.mListViews = mListViews;
        this.tabNum=mListViews.size();
	}

	/**
	 * 描述：获取数量.
	 *
	 * @return the count
	 * @see PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mListViews.size();
	}

	/**
	 * 描述：Object是否对应这个View.
	 *
	 * @param arg0 the arg0
	 * @param arg1 the arg1
	 * @return true, if is view from object
	 * @see PagerAdapter#isViewFromObject(android.view.View, java.lang.Object)
	 */
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	/**
	 * 描述：显示View.
	 *
	 * @param container the container
	 * @param position the position
	 * @return the object
	 * @see PagerAdapter#instantiateItem(android.view.View, int)
	 */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mListViews.get(position);
        if(container.equals(view.getParent())) {
            container.removeView(view);
        }
        container.addView(view);
        return view;
    }

	/**
	 * 描述：移除View.
	 *
	 * @param container the container
	 * @param position the position
	 * @param object the object
	 * @see PagerAdapter#destroyItem(android.view.View, int, java.lang.Object)
	 */
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View)object);
	}
	
	/**
	 * 描述：很重要，否则不能notifyDataSetChanged.
	 *
	 * @param object the object
	 * @return the item position
	 * @see PagerAdapter#getItemPosition(java.lang.Object)
	 */
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	

}
