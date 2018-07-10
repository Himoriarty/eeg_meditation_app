package com.aliyun.oss.ossdemo;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by OSS on 2015/12/9 0009.
 * 重载InputStream用来触发进度回调
 */
public class ProgressInputStream extends InputStream {
    private InputStream stream;
    private OSSProgressCallback progressCallback;
    private long totalSize;
    private long currentSize = 0;

    private long step = 1;

    public ProgressInputStream(InputStream stream, OSSProgressCallback progressCallback, long totalSize) {
        this.stream = stream;
        this.progressCallback = progressCallback;
        this.totalSize = totalSize;
        //默认的进度显示进度为百分之一
        if (totalSize > 100) {
            this.step = totalSize / 100;
        }
    }

    @Override
    public int read() throws IOException {
        int result = stream.read();
        currentSize++;
        if ((currentSize % step == 0) || (currentSize == totalSize)) {
            progressCallback.onProgress(null, currentSize, totalSize);
        }
        return result;
    }
}
