package com.eastmoney.video.network.aliupload.internal;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public interface ResponseParser<T> {

    public T parse(Response response) throws IOException;
}
