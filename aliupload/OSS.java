/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.eastmoney.video.network.aliupload;


import com.eastmoney.video.network.aliupload.callback.ClientException;
import com.eastmoney.video.network.aliupload.callback.OSSCompletedCallback;
import com.eastmoney.video.network.aliupload.callback.ServiceException;
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

/**
 * 阿里云开放存储服务（Open Storage Service， OSS）的访问接口。
 * <p>
 * 阿里云存储服务（Open Storage Service，简称OSS），是阿里云对外提供的海量，安全，低成本，
 * 高可靠的云存储服务。用户可以通过简单的REST接口，在任何时间、任何地点上传和下载数据，
 * 也可以使用WEB页面对数据进行管理。<br />
 * 基于OSS，用户可以搭建出各种多媒体分享网站、网盘、个人企业数据备份等基于大规模数据的服务。
 * </p>
 *
 * <p>
 * OSS为SDK的接口类，封装了OSS的RESTFul Api接口，考虑到移动端不能在UI线程发起网络请求的编程规范，
 * SDK为所有接口提供了异步的调用形式，也提供了同步接口。
 * </p>
 */
public interface OSS {

    /**
     * 异步上传文件
     * Put Object用于上传文件。
     *
     * @param request 请求信息
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<PutObjectResult> asyncPutObject(
            PutObjectRequest request, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback);

    /**
     * 同步上传文件
     * Put Object用于上传文件。
     *
     * @param request 请求信息
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public PutObjectResult putObject(PutObjectRequest request)
            throws ClientException, ServiceException;


    /**
     * 异步初始化分块上传
     * 使用Multipart Upload模式传输数据前，必须先调用该接口来通知OSS初始化一个Multipart Upload事件。
     * 该接口会返回一个OSS服务器创建的全局唯一的Upload ID，用于标识本次Multipart Upload事件。
     * 用户可以根据这个ID来发起相关的操作，如中止Multipart Upload、查询Multipart Upload等。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<InitiateMultipartUploadResult> asyncInitMultipartUpload(
            InitiateMultipartUploadRequest request, OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> completedCallback);

    /**
     * 同步初始化分块上传
     * 使用Multipart Upload模式传输数据前，必须先调用该接口来通知OSS初始化一个Multipart Upload事件。
     * 该接口会返回一个OSS服务器创建的全局唯一的Upload ID，用于标识本次Multipart Upload事件。
     * 用户可以根据这个ID来发起相关的操作，如中止Multipart Upload、查询Multipart Upload等。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public InitiateMultipartUploadResult initMultipartUpload(InitiateMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步上传分块
     * 初始化一个Multipart Upload之后，可以根据指定的Object名和Upload ID来分块（Part）上传数据。
     * 每一个上传的Part都有一个标识它的号码（part number，范围是1~10,000）。
     * 对于同一个Upload ID，该号码不但唯一标识这一块数据，也标识了这块数据在整个文件内的相对位置。
     * 如果你用同一个part号码，上传了新的数据，那么OSS上已有的这个号码的Part数据将被覆盖。
     * 除了最后一块Part以外，其他的part最小为100KB；最后一块Part没有大小限制。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<UploadPartResult> asyncUploadPart(
            UploadPartRequest request, OSSCompletedCallback<UploadPartRequest, UploadPartResult> completedCallback);

    /**
     * 同步上传分块
     * 初始化一个Multipart Upload之后，可以根据指定的Object名和Upload ID来分块（Part）上传数据。
     * 每一个上传的Part都有一个标识它的号码（part number，范围是1~10,000）。
     * 对于同一个Upload ID，该号码不但唯一标识这一块数据，也标识了这块数据在整个文件内的相对位置。
     * 如果你用同一个part号码，上传了新的数据，那么OSS上已有的这个号码的Part数据将被覆盖。
     * 除了最后一块Part以外，其他的part最小为100KB；最后一块Part没有大小限制。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public UploadPartResult uploadPart(UploadPartRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步完成分块上传
     * 在将所有数据Part都上传完成后，必须调用Complete Multipart Upload API来完成整个文件的Multipart Upload。
     * 在执行该操作时，用户必须提供所有有效的数据Part的列表（包括part号码和ETAG）；OSS收到用户提交的Part列表后，会逐一验证每个数据Part的有效性。
     * 当所有的数据Part验证通过后，OSS将把这些数据part组合成一个完整的Object。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<CompleteMultipartUploadResult> asyncCompleteMultipartUpload(
            CompleteMultipartUploadRequest request, OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback);

    /**
     * 同步完成分块上传
     * 在将所有数据Part都上传完成后，必须调用Complete Multipart Upload API来完成整个文件的Multipart Upload。
     * 在执行该操作时，用户必须提供所有有效的数据Part的列表（包括part号码和ETAG）；OSS收到用户提交的Part列表后，会逐一验证每个数据Part的有效性。
     * 当所有的数据Part验证通过后，OSS将把这些数据part组合成一个完整的Object。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步取消分块上传
     * 该接口可以根据用户提供的Upload ID中止其对应的Multipart Upload事件。
     * 当一个Multipart Upload事件被中止后，就不能再使用这个Upload ID做任何操作，已经上传的Part数据也会被删除。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<AbortMultipartUploadResult> asyncAbortMultipartUpload(
            AbortMultipartUploadRequest request, OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult> completedCallback);

    /**
     * 同步取消分块上传
     * 该接口可以根据用户提供的Upload ID中止其对应的Multipart Upload事件。
     * 当一个Multipart Upload事件被中止后，就不能再使用这个Upload ID做任何操作，已经上传的Part数据也会被删除。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public AbortMultipartUploadResult abortMultipartUpload(AbortMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步罗列分块
     * List Parts命令可以罗列出指定Upload ID所属的所有已经上传成功Part。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<ListPartsResult> asyncListParts(
            ListPartsRequest request, OSSCompletedCallback<ListPartsRequest, ListPartsResult> completedCallback);

    /**
     * 同步罗列分块
     * List Parts命令可以罗列出指定Upload ID所属的所有已经上传成功Part。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ListPartsResult listParts(ListPartsRequest request)
            throws ClientException, ServiceException;



    /**
     * 异步断点上传
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public OSSAsyncTask<ResumableUploadResult> asyncResumableUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback);


    /**
     * 同步断点上传
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ResumableUploadResult resumableUpload(ResumableUploadRequest request)
            throws ClientException, ServiceException;

}
