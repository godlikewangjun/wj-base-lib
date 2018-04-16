package com.abase.okhttp;

/**
 * 监听进度
 * @author wangjun
 * @version 2.0
 * @date 2016年2月2日
 */
public interface OhProgressListener {
	/**
	 * 监听
	 * @param bytesWritten 写入的长度
	 * @param contentLength 总长度
	 * @param done 是否完成
	 */
	 void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}
