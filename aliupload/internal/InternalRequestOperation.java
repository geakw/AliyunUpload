package com.eastmoney.video.network.aliupload.internal;

import android.content.Context;


import com.eastmoney.video.network.aliupload.ExecutionContext;
import com.eastmoney.video.network.aliupload.OSSRequestTask;
import com.eastmoney.video.network.aliupload.callback.OSSCompletedCallback;
import com.eastmoney.video.network.aliupload.common.OSSHeaders;
import com.eastmoney.video.network.aliupload.model.AbortMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.AbortMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.CompleteMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.CompleteMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.InitiateMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.InitiateMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.ListPartsRequest;
import com.eastmoney.video.network.aliupload.model.ListPartsResult;
import com.eastmoney.video.network.aliupload.model.PutObjectRequest;
import com.eastmoney.video.network.aliupload.model.PutObjectResult;
import com.eastmoney.video.network.aliupload.model.UploadPartRequest;
import com.eastmoney.video.network.aliupload.model.UploadPartResult;
import com.eastmoney.video.network.aliupload.network.ClientConfiguration;
import com.eastmoney.video.network.aliupload.utils.DateUtil;
import com.eastmoney.video.network.aliupload.utils.HttpHeaders;
import com.eastmoney.video.network.aliupload.utils.HttpMethod;
import com.eastmoney.video.network.aliupload.utils.OSSUtils;
import com.eastmoney.video.network.aliupload.utils.VersionInfoUtils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class InternalRequestOperation {

    private OkHttpClient innerClient;
    private Context applicationContext;
    private int maxRetryCount = 2;

    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    public InternalRequestOperation(Context context, ClientConfiguration conf) {
        this.applicationContext = context;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .cache(null);

        if (conf != null) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(conf.getMaxConcurrentRequest());

            builder.connectTimeout(conf.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .dispatcher(dispatcher);

            this.maxRetryCount = conf.getMaxErrorRetry();

        }

        this.innerClient = builder.build();
    }

    public OSSAsyncTask<PutObjectResult> putObject(
            PutObjectRequest request, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setUrl(request.getUrl());
        requestMessage.setObjectKey(request.getObjectKey());
        if (request.getUploadData() != null) {
            requestMessage.setUploadData(request.getUploadData());
        }
        if (request.getUploadFilePath() != null) {
            requestMessage.setUploadFilePath(request.getUploadFilePath());
        }
        if (request.getCallbackParam() != null) {
            requestMessage.getHeaders().put("x-oss-callback", OSSUtils.populateMapToBase64JsonString(request.getCallbackParam()));
        }
        if (request.getCallbackVars() != null) {
            requestMessage.getHeaders().put("x-oss-callback-var", OSSUtils.populateMapToBase64JsonString(request.getCallbackVars()));
        }

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<PutObjectRequest> executionContext = new ExecutionContext<PutObjectRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        executionContext.setProgressCallback(request.getProgressCallback());
        ResponseParser<PutObjectResult> parser = new ResponseParsers.PutObjectReponseParser();

        Callable<PutObjectResult> callable = new OSSRequestTask<PutObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }


    public OSSAsyncTask<InitiateMultipartUploadResult> initMultipartUpload(
            InitiateMultipartUploadRequest request, OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setUrl(request.getUrl());
        requestMessage.setObjectKey(request.getObjectKey());
        requestMessage.getParameters().put("uploads", "");

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<InitiateMultipartUploadRequest> executionContext = new ExecutionContext<InitiateMultipartUploadRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<InitiateMultipartUploadResult> parser = new ResponseParsers.InitMultipartResponseParser();

        Callable<InitiateMultipartUploadResult> callable = new OSSRequestTask<InitiateMultipartUploadResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<UploadPartResult> uploadPart(
            UploadPartRequest request, OSSCompletedCallback<UploadPartRequest, UploadPartResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setMethod(HttpMethod.PUT);
        request.setUrl(request.getUrl());
        requestMessage.setObjectKey(request.getObjectKey());

        requestMessage.getParameters().put("uploadId", request.getUploadId());
        requestMessage.getParameters().put("partNumber", String.valueOf(request.getPartNumber()));
        requestMessage.setUploadData(request.getPartContent());

        if (request.getMd5Digest() != null) {
            requestMessage.getHeaders().put(OSSHeaders.CONTENT_MD5, request.getMd5Digest());
        }

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<UploadPartRequest> executionContext = new ExecutionContext<UploadPartRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        executionContext.setProgressCallback(request.getProgressCallback());
        ResponseParser<UploadPartResult> parser = new ResponseParsers.UploadPartResponseParser();

        Callable<UploadPartResult> callable = new OSSRequestTask<UploadPartResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<CompleteMultipartUploadResult> completeMultipartUpload(
            CompleteMultipartUploadRequest request, OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setUrl(request.getUrl());
        requestMessage.setObjectKey(request.getObjectKey());
        requestMessage.setUploadData(OSSUtils.buildXMLFromPartEtagList(request.getPartETags()).getBytes());

        requestMessage.getParameters().put("uploadId", request.getUploadId());
        if (request.getCallbackParam() != null) {
            requestMessage.getHeaders().put("x-oss-callback", OSSUtils.populateMapToBase64JsonString(request.getCallbackParam()));
        }
        if (request.getCallbackVars() != null) {
            requestMessage.getHeaders().put("x-oss-callback-var", OSSUtils.populateMapToBase64JsonString(request.getCallbackVars()));
        }

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<CompleteMultipartUploadRequest> executionContext = new ExecutionContext<CompleteMultipartUploadRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<CompleteMultipartUploadResult> parser = new ResponseParsers.CompleteMultipartUploadResponseParser();

        Callable<CompleteMultipartUploadResult> callable = new OSSRequestTask<CompleteMultipartUploadResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<AbortMultipartUploadResult> abortMultipartUpload(
            AbortMultipartUploadRequest request, OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setMethod(HttpMethod.DELETE);
        requestMessage.setUrl(request.getUrl());
        requestMessage.setObjectKey(request.getObjectKey());

        requestMessage.getParameters().put("uploadId", request.getUploadId());

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<AbortMultipartUploadRequest> executionContext = new ExecutionContext<AbortMultipartUploadRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<AbortMultipartUploadResult> parser = new ResponseParsers.AbortMultipartUploadResponseParser();

        Callable<AbortMultipartUploadResult> callable = new OSSRequestTask<AbortMultipartUploadResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<ListPartsResult> listParts(
            ListPartsRequest request, OSSCompletedCallback<ListPartsRequest, ListPartsResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setUrl(request.getUrl());
        requestMessage.setObjectKey(request.getObjectKey());

        requestMessage.getParameters().put("uploadId", request.getUploadId());

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<ListPartsRequest> executionContext = new ExecutionContext<ListPartsRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ListPartsResult> parser = new ResponseParsers.ListPartsResponseParser();

        Callable<ListPartsResult> callable = new OSSRequestTask<ListPartsResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }


    public OkHttpClient getInnerClient() {
        return innerClient;
    }

    private void canonicalizeRequestMessage(RequestMessage message) {
        Map<String, String> header = message.getHeaders();

        if (header.get(OSSHeaders.DATE) == null) {
            header.put(OSSHeaders.DATE, DateUtil.currentFixedSkewedTimeInRFC822Format());
        }

        if (message.getMethod() == HttpMethod.POST || message.getMethod() == HttpMethod.PUT) {
            if (header.get(OSSHeaders.CONTENT_TYPE) == null) {
                String determineContentType = OSSUtils.determineContentType(null,
                        message.getUploadFilePath(), message.getObjectKey());
                header.put(OSSHeaders.CONTENT_TYPE, determineContentType);
            }
        }
        message.getHeaders().put(HttpHeaders.USER_AGENT, VersionInfoUtils.getUserAgent());
    }
}
