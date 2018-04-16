package com.abase.okhttp.body;


import com.abase.okhttp.OhFileCallBakListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * 上传下载的进度监听
 *
 * @author Admin
 * @version 1.0
 * @date 2017/7/4
 */

public class FileRequestBody extends RequestBody {
    private MediaType contentType;
    private OhFileCallBakListener ohFileCallBakListener;
    private InputStream fileInputStream;
    private long lastSize = 0;
    private long totalLength = 0;
    public boolean isPause = false;
    private long maxSize =10;

    /**
     * 构造传入必要的参数 回传
     *
     * @param contentType
     * @param file
     * @param lastSize              最后续传的地方
     * @param ohFileCallBakListener
     */
    public FileRequestBody(MediaType contentType, File file, long lastSize, OhFileCallBakListener ohFileCallBakListener) {
        this.contentType = contentType;
        this.ohFileCallBakListener = ohFileCallBakListener;
        this.totalLength = file.length();
        this.lastSize = lastSize;
        if (lastSize > 0) {
            try {
                RandomAccessFile accessFile = new RandomAccessFile(file, "r");
                accessFile.seek(lastSize);// 仅上传未完成的文件内容
                fileInputStream = new FileInputStream(accessFile.getFD());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        try {
            return fileInputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastSize;
    }

    @Override
    public void writeTo(final BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(fileInputStream);
            Buffer buf = new Buffer();
            long remaining = 0;
//            sink.writeAll(source);
            while ((remaining = source.read(sink.buffer(), maxSize)) != -1) {
                if (isPause || remaining<1) {
                  break;
                }
                lastSize += remaining;
                ohFileCallBakListener.sendProgressMessage(lastSize, totalLength, totalLength == lastSize);
            }
            sink.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Util.closeQuietly(source);
        }
    }
}
