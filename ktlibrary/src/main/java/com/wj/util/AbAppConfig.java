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
package com.wj.util;




// TODO: Auto-generated Javadoc
/**
 * 名称：AbAppConfig.java
 * 描述：初始设置类.
 */
public class AbAppConfig {

	/**  UI设计的基准宽度. */
	public static int UI_WIDTH = 1080;

	/**  UI设计的基准高度. */
	public static int UI_HEIGHT = 1920;

	/** 默认下载文件地址. */
	public static  String DOWNLOAD_ROOT_DIR = "download";
	
    /** 默认下载图片文件地址. */
	public static  String DOWNLOAD_IMAGE_DIR = "images";
	
    /** 默认下载文件地址. */
	public static  String DOWNLOAD_FILE_DIR = "files";
	
	/** APP缓存目录. */
	public static  String CACHE_DIR = "cache";
	
	/** DB目录. */
	public static  String DB_DIR = "db";
	
	/** Log目录. */
	public static  String Log_DIR = "log";
	
	/** 默认缓存超时时间设置. */
	public static int IMAGE_CACHE_EXPIRES_TIME = 3600*24*3;
	
	/** 缓存大小  单位10M. */
	public static int MAX_CACHE_SIZE_INBYTES = 10*1024*1024;
	

}
