/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.eastmoney.video.network.aliupload;

import android.content.Context;

import com.eastmoney.video.network.aliupload.callback.ClientException;
import com.eastmoney.video.network.aliupload.callback.OSSCompletedCallback;
import com.eastmoney.video.network.aliupload.callback.ServiceException;
import com.eastmoney.video.network.aliupload.internal.ExtensionRequestOperation;
import com.eastmoney.video.network.aliupload.internal.InternalRequestOperation;
import com.eastmoney.video.network.aliupload.internal.OSSAsyncTask;
import com.eastmoney.video.network.aliupload.model.AbortMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.AbortMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.CompleteMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.CompleteMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.GetObjectRequest;
import com.eastmoney.video.network.aliupload.model.GetObjectResult;
import com.eastmoney.video.network.aliupload.model.InitiateMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.InitiateMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.ListPartsRequest;
import com.eastmoney.video.network.aliupload.model.ListPartsResult;
import com.eastmoney.video.network.aliupload.model.PutObjectRequest;
import com.eastmoney.video.network.aliupload.model.PutObjectResult;
import com.eastmoney.video.network.aliupload.model.ResumableUploadRequest;
import com.eastmoney.video.network.aliupload.model.ResumableUploadResult;
import com.eastmoney.video.network.aliupload.model.UploadPartRequest;
import com.eastmoney.video.network.aliupload.model.UploadPartResult;
import com.eastmoney.video.network.aliupload.network.ClientConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 访问阿里云开放存储服务（Open Storage Service， OSS）的入口类。
 */
public class OSSClient implements OSS {

    private InternalRequestOperation internalRequestOperation;
    private ExtensionRequestOperation extensionRequestOperation;


    /**
     * 构造一个OSSClient实例
     *
     * @param context android应用的applicationContext
     * @param endpoint OSS访问域名，参考http://help.aliyun.com/document_detail/oss/user_guide/endpoint_region.html
     * @param credentialProvider 鉴权设置
     */
    public OSSClient(Context context) {
        this(context,null);
    }

    /**
     * 构造一个OSSClient实例
     *
     * @param context android应用的applicationContext
     * @param endpoint OSS访问域名，参考http://help.aliyun.com/document_detail/oss/user_guide/endpoint_region.html
     * @param credentialProvider 鉴权设置
     * @param conf 网络参数设置
     */
    public OSSClient(Context context, ClientConfiguration conf) {
        internalRequestOperation = new InternalRequestOperation(context, conf);
        extensionRequestOperation = new ExtensionRequestOperation(internalRequestOperation);
    }


    @Override
	public OSSAsyncTask<PutObjectResult> asyncPutObject(
            PutObjectRequest request, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {

        return internalRequestOperation.putObject(request, completedCallback);
	}

    @Override
    public PutObjectResult putObject(PutObjectRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.putObject(request, null).getResult();
    }



    @Override
    public OSSAsyncTask<InitiateMultipartUploadResult> asyncInitMultipartUpload(InitiateMultipartUploadRequest request, OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> completedCallback) {

        return internalRequestOperation.initMultipartUpload(request, completedCallback);
    }

    @Override
    public InitiateMultipartUploadResult initMultipartUpload(InitiateMultipartUploadRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.initMultipartUpload(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<UploadPartResult> asyncUploadPart(UploadPartRequest request, OSSCompletedCallback<UploadPartRequest, UploadPartResult> completedCallback) {

        return internalRequestOperation.uploadPart(request, completedCallback);
    }

    @Override
    public UploadPartResult uploadPart(UploadPartRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.uploadPart(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<CompleteMultipartUploadResult> asyncCompleteMultipartUpload(CompleteMultipartUploadRequest request, OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback) {

        return internalRequestOperation.completeMultipartUpload(request, completedCallback);
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.completeMultipartUpload(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<AbortMultipartUploadResult> asyncAbortMultipartUpload(AbortMultipartUploadRequest request, OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult> completedCallback) {

        return internalRequestOperation.abortMultipartUpload(request, completedCallback);
    }

    @Override
    public AbortMultipartUploadResult abortMultipartUpload(AbortMultipartUploadRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.abortMultipartUpload(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<ListPartsResult> asyncListParts(ListPartsRequest request, OSSCompletedCallback<ListPartsRequest, ListPartsResult> completedCallback) {

        return internalRequestOperation.listParts(request, completedCallback);
    }

    @Override
    public ListPartsResult listParts(ListPartsRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.listParts(request, null).getResult();
    }



    @Override
    public OSSAsyncTask<ResumableUploadResult> asyncResumableUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback) {

        return extensionRequestOperation.resumableUpload(request, completedCallback);
    }

    @Override
    public ResumableUploadResult resumableUpload(ResumableUploadRequest request)
            throws ClientException, ServiceException {

        return extensionRequestOperation.resumableUpload(request, null).getResult();
    }

}
