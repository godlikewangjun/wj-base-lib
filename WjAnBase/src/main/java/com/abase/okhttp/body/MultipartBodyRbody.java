package com.abase.okhttp.body;

import com.abase.okhttp.OhFileCallBakListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 多文件上传监听
 *
 * @author Admin
 * @version 1.0
 * @date 2018/1/26
 */

public class MultipartBodyRbody extends RequestBody {

    private final RequestBody requestBody;
    private BufferedSink bufferedSink;
    private OhFileCallBakListener ohFileCallBakListener;

    public MultipartBodyRbody(RequestBody requestBody, OhFileCallBakListener ohFileCallBakListener) {
        this.requestBody = requestBody;
        this.ohFileCallBakListener = ohFileCallBakListener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            private long current;
            private long total;
            private long last = 0;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (total == 0) {
                    total = contentLength();
                }
                current += byteCount;
                long now = current;
                if (last < now) {
                    ohFileCallBakListener.onRequestProgress(current,total, total == current);
                    last = now;
                }
            }
        };
    }
}
