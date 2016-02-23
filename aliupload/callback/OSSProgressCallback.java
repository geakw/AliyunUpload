package com.eastmoney.video.network.aliupload.callback;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public interface OSSProgressCallback<T> {

    public void onProgress(T request, long currentSize, long totalSize);

}
