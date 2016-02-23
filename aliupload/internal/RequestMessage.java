package com.eastmoney.video.network.aliupload.internal;

import com.eastmoney.video.network.aliupload.utils.HttpMethod;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class RequestMessage {

    private String objectKey;
    private HttpMethod method;
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> parameters = new LinkedHashMap<String, String>();

    private byte[] uploadData;
    private String uploadFilePath;
    private InputStream uploadInputStream;
    private long readStreamLength;

    private String downloadFilePath;

    private String url;

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers = headers;
        }
    }

    public void addHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers.putAll(headers);
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public byte[] getUploadData() {
        return uploadData;
    }

    public void setUploadData(byte[] uploadData) {
        this.uploadData = uploadData;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public String getDownloadFilePath() {
        return downloadFilePath;
    }

    public void setDownloadFilePath(String downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
    }

    public void setUploadInputStream(InputStream uploadInputStream, long inputLength) {
        if (uploadInputStream != null) {
            this.uploadInputStream = uploadInputStream;
            this.readStreamLength = inputLength;
        }
    }

    public InputStream getUploadInputStream() {
        return uploadInputStream;
    }


    public long getReadStreamLength() {
        return readStreamLength;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
