package com.eastmoney.video.network.aliupload;

import com.eastmoney.video.network.aliupload.callback.OSSCompletedCallback;
import com.eastmoney.video.network.aliupload.callback.OSSProgressCallback;
import com.eastmoney.video.network.aliupload.model.OSSRequest;

import okhttp3.OkHttpClient;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class ExecutionContext<T extends OSSRequest> {

    private T request;
    private OkHttpClient client;
    private CancellationHandler cancellationHandler = new CancellationHandler();

    private OSSCompletedCallback completedCallback;
    private OSSProgressCallback progressCallback;

    public ExecutionContext(OkHttpClient client, T request) {
        this.client = client;
        this.request = request;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public CancellationHandler getCancellationHandler() {
        return cancellationHandler;
    }

    public void setCancellationHandler(CancellationHandler cancellationHandler) {
        this.cancellationHandler = cancellationHandler;
    }

    public OSSCompletedCallback getCompletedCallback() {
        return completedCallback;
    }

    public void setCompletedCallback(OSSCompletedCallback completedCallback) {
        this.completedCallback = completedCallback;
    }

    public OSSProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(OSSProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }
}
