package com.eastmoney.video.network.aliupload.callback;

import com.eastmoney.video.network.aliupload.model.OSSRequest;
import com.eastmoney.video.network.aliupload.model.OSSResult;

/**
 * Created by zhouzhuo on 11/19/15.
 */
public interface OSSCompletedCallback<T1 extends OSSRequest, T2 extends OSSResult> {

    public void onSuccess(T1 request, T2 result);

    public void onFailure(T1 request, ClientException clientException, ServiceException serviceException);
}
