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
package com.abase.util;

import java.util.Calendar;

import android.content.Context;
import android.util.Log;

import com.wjabase.BuildConfig;

import static android.R.attr.start;

/**
 * 名称：AbLogUtil.java
 * 描述：日志工具类.
 */
public class AbLogUtil {
	
    /** debug开关. */
	public static boolean D = true;
	
	/**每行日志最多好长 长了就换行继续显示*/
	private static int LOG_MAXLENGTH=1024*4-10;

	/**
	 * debug日志
	 * @param tag
	 * @param message
	 */
	public static void d(String tag,String message) {

		if(D){
			int strLength = message.length();
			if(strLength<=LOG_MAXLENGTH){
				Log.d(tag, message);
				return;
			}
			while (message.length() > LOG_MAXLENGTH) {
				Log.d(tag, message.substring(0, LOG_MAXLENGTH));
				message = message.substring(LOG_MAXLENGTH);
			}
		}
	}
	
	/**
	 * debug日志
	 * @param context
	 * @param message
	 */
	public static void d(Context context,String message) {
		String tag = context.getClass().getSimpleName();
		d(tag, message);
	}
	
	/**
	 * debug日志
	 * @param clazz
	 * @param message
	 */
	public static void d(Class<?> clazz,String message) {
		String tag = clazz.getSimpleName();
		d(tag, message);
	}
	
	/**
	 * info日志
	 * @param tag
	 * @param message
	 */
	public static void i(String tag,String message) {
		if(D){
			int strLength = message.length();
			int start = 0;
			int end = LOG_MAXLENGTH;
			int count=1;
			if(strLength%LOG_MAXLENGTH>0){
				count=strLength/LOG_MAXLENGTH+1;
			}else if(strLength%LOG_MAXLENGTH==0){
				count=strLength/LOG_MAXLENGTH;
			}
			if(strLength<=LOG_MAXLENGTH){
				Log.i(tag, message);
				return;
			}
			while (message.length() > LOG_MAXLENGTH) {
				Log.i(tag, message.substring(0, LOG_MAXLENGTH));
				message = message.substring(LOG_MAXLENGTH);
			}
		}
	}
	
	/**
	 * info日志
	 * @param context
	 * @param message
	 */
	public static void i(Context context,String message) {
		String tag = context.getClass().getSimpleName();
		i(tag, message);
	}
	
	/**
	 * info日志
	 * @param clazz
	 * @param message
	 */
	public static void i(Class<?> clazz,String message) {
		String tag = clazz.getSimpleName();
		i(tag, message);
	}
	
	
	
	/**
	 * error日志
	 * @param tag
	 * @param message
	 */
	public static void e(String tag,String message) {
		if(D){
			Log.e(tag, message);
		}
	}
	
	/**
	 * error日志
	 * @param context
	 * @param message
	 */
	public static void e(Context context,String message) {
		String tag = context.getClass().getSimpleName();
		e(tag, message);
	}
	
	/**
	 * error日志
	 * @param clazz
	 * @param message
	 */
	public static void e(Class<?> clazz,String message) {
		String tag = clazz.getSimpleName();
		e(tag, message);
	}

	/**
	 * 设置日志的开关
	 * @param e
	 */
	public static void closeLog() {
		D  = false;
	}

}
