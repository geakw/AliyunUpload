package com.eastmoney.video.network.aliupload.model;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class AbortMultipartUploadRequest extends OSSRequest {

    /** The name of the bucket containing the multipart upload to abort */
    private String url;

    /** The objectKey of the multipart upload to abort */
    private String objectKey;

    /** The ID of the multipart upload to abort */
    private String uploadId;

    /**
     * 构造函数。
     * @param url
     *          Bucket名称。
     * @param objectKey
     *          Object objectKey。
     * @param uploadId
     *          标识Multipart上传事件的Upload ID。
     */
    public AbortMultipartUploadRequest(String url, String objectKey, String uploadId) {
        this.url = url;
        this.objectKey = objectKey;
        this.uploadId = uploadId;
    }

    /**
     * 返回Bucket名称。
     * @return Bucket名称。
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * 设置Bucket名称。
     * @param url
     *          Bucket名称。
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 返回OSSObject objectKey。
     * @return Object objectKey。
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置OSSObject objectKey。
     * @param objectKey
     *          Object objectKey。
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * 返回标识Multipart上传事件的Upload ID。
     * @return 标识Multipart上传事件的Upload ID。
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * 设置标识Multipart上传事件的Upload ID。
     * @param uploadId
     *          标识Multipart上传事件的Upload ID。
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
