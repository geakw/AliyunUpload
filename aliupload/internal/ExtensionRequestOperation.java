package com.eastmoney.video.network.aliupload.internal;


import com.eastmoney.video.network.aliupload.ExecutionContext;
import com.eastmoney.video.network.aliupload.callback.ClientException;
import com.eastmoney.video.network.aliupload.callback.OSSCompletedCallback;
import com.eastmoney.video.network.aliupload.callback.OSSProgressCallback;
import com.eastmoney.video.network.aliupload.callback.ServiceException;
import com.eastmoney.video.network.aliupload.model.AbortMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.CompleteMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.CompleteMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.HeadObjectRequest;
import com.eastmoney.video.network.aliupload.model.InitiateMultipartUploadRequest;
import com.eastmoney.video.network.aliupload.model.InitiateMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.ListPartsRequest;
import com.eastmoney.video.network.aliupload.model.ListPartsResult;
import com.eastmoney.video.network.aliupload.model.PartETag;
import com.eastmoney.video.network.aliupload.model.PartSummary;
import com.eastmoney.video.network.aliupload.model.ResumableUploadRequest;
import com.eastmoney.video.network.aliupload.model.ResumableUploadResult;
import com.eastmoney.video.network.aliupload.model.UploadPartRequest;
import com.eastmoney.video.network.aliupload.model.UploadPartResult;
import com.eastmoney.video.network.aliupload.utils.BinaryUtil;
import com.eastmoney.video.network.aliupload.utils.IOUtils;
import com.eastmoney.video.network.aliupload.utils.OSSLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhouzhuo on 11/27/15.
 */
public class ExtensionRequestOperation {

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private InternalRequestOperation apiOperation;

    public ExtensionRequestOperation(InternalRequestOperation apiOperation) {
        this.apiOperation = apiOperation;
    }


    public OSSAsyncTask<ResumableUploadResult> resumableUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback) {

        ExecutionContext<ResumableUploadRequest> executionContext = new ExecutionContext<ResumableUploadRequest>(apiOperation.getInnerClient(), request);

        return OSSAsyncTask.wrapRequestTask(executor.submit(new ResumableUploadTask(request, completedCallback, executionContext)), executionContext);
    }

    class ResumableUploadTask implements Callable<ResumableUploadResult> {

        private ResumableUploadRequest request;
        private OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback;
        private ExecutionContext context;

        private String uploadId;
        private File recordFile;
        private List<PartETag> partETags = new ArrayList<PartETag>();

        private long fileLength;
        private long currentUploadLength;

        public ResumableUploadTask(ResumableUploadRequest request,
                                   OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback,
                                   ExecutionContext context) {
            this.request = request;
            this.completedCallback = completedCallback;
            this.context = context;
        }

        @Override
        public ResumableUploadResult call() throws Exception {

            try {

                initUploadId();
                ResumableUploadResult result = doMultipartUpload();

                if (completedCallback != null) {
                    completedCallback.onSuccess(request, result);
                }
                return result;
            } catch (ServiceException e) {
                if (completedCallback != null) {
                    completedCallback.onFailure(request, null, e);
                }
                throw e;
            } catch (ClientException e) {
                if (completedCallback != null) {
                    completedCallback.onFailure(request, e, null);
                }
                throw e;
            } catch (IOException e) {
                ClientException clientException = new ClientException(e.toString(), e);
                if (completedCallback != null) {
                    completedCallback.onFailure(request, clientException, null);
                }
                throw clientException;
            }
        }

        private void initUploadId() throws IOException, ServiceException, ClientException {

            String uploadFilePath = request.getUploadFilePath();

            if (request.getRecordDirectory() != null) {
                String fileMd5 = BinaryUtil.calculateMd5Str(uploadFilePath);
                String recordFileName = BinaryUtil.calculateMd5Str((fileMd5 + request.getUrl() + String.valueOf(request.getPartSize())).getBytes());
                String recordPath = request.getRecordDirectory() + "/" + recordFileName;
                recordFile = new File(recordPath);
                if (recordFile.exists()) {
                    BufferedReader br = new BufferedReader(new FileReader(recordFile));
                    uploadId = br.readLine();
                    br.close();

                    OSSLog.logD("[initUploadId] - Found record file, uploadid: " + uploadId);
                    ListPartsRequest listParts = new ListPartsRequest(request.getUrl(), request.getObjectKey(), uploadId);
                    OSSAsyncTask<ListPartsResult> task = apiOperation.listParts(listParts, null);
                    try {
                        for (PartSummary part : task.getResult().getParts()) {
                            partETags.add(new PartETag(part.getPartNumber(), part.getETag()));
                        }
                        return;
                    } catch (ServiceException e) {
                        if (e.getStatusCode() == 404) {
                            uploadId = null;
                        } else {
                            throw e;
                        }
                    } catch (ClientException e) {
                        throw e;
                    }
                }

                if (!recordFile.exists() && !recordFile.createNewFile()) {
                    throw new ClientException("Can't create file at path: " + recordFile.getAbsolutePath()
                            + "\nPlease make sure the directory exist!");
                }
            }

            InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(
                    request.getUrl(), request.getObjectKey(), request.getMetadata());

            InitiateMultipartUploadResult initResult = apiOperation.initMultipartUpload(init, null).getResult();

            uploadId = initResult.getUploadId();

            if (recordFile != null) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(recordFile));
                bw.write(uploadId);
                bw.close();
            }
        }

        private ResumableUploadResult doMultipartUpload() throws IOException, ClientException, ServiceException {

            if (context.getCancellationHandler().isCancelled()) {
                abortThisResumableUpload();
                if (recordFile != null) {
                    recordFile.delete();
                }
                throwOutInterruptClientException();
            }

            long blockSize = request.getPartSize();
            int currentUploadIndex = partETags.size() + 1;
            File uploadFile = new File(request.getUploadFilePath());
            fileLength = uploadFile.length();
            int totalBlockNum;
            final OSSProgressCallback progressCallback = request.getProgressCallback();

            totalBlockNum = (int) (fileLength / blockSize) + (fileLength % blockSize == 0 ? 0 : 1);
            if (currentUploadIndex <= totalBlockNum) {
                currentUploadLength = blockSize * (currentUploadIndex - 1);
            } else {
                currentUploadLength = fileLength;
            }

            InputStream in = new FileInputStream(uploadFile);

            long at = 0;
            while (at < currentUploadLength) {
                long realSkip = in.skip(currentUploadLength - at);
                if (realSkip == -1) {
                    throw new IOException("Skip failed! [fileLength]: " + fileLength + " [needSkip]: " + currentUploadLength);
                }
                at += realSkip;
            }

            while (currentUploadIndex <= totalBlockNum) {
                UploadPartRequest uploadPart = new UploadPartRequest(
                        request.getUrl(), request.getObjectKey(), uploadId, currentUploadIndex);

                uploadPart.setProgressCallback(new OSSProgressCallback<UploadPartRequest>() {
                    @Override
                    public void onProgress(UploadPartRequest request, long currentSize, long totalSize) {
                        if (progressCallback != null) {
                            progressCallback.onProgress(ResumableUploadTask.this.request, currentUploadLength + currentSize, fileLength);
                        }
                    }
                });

                int toUpload = (int)Math.min(blockSize, fileLength - currentUploadLength);
                byte[] partContent = IOUtils.readStreamAsBytesArray(in, toUpload);
                uploadPart.setPartContent(partContent);
                uploadPart.setMd5Digest(BinaryUtil.calculateBase64Md5(partContent));

                UploadPartResult uploadPartResult = apiOperation.uploadPart(uploadPart, null).getResult();

                partETags.add(new PartETag(currentUploadIndex, uploadPartResult.getETag()));

                currentUploadLength += toUpload;
                currentUploadIndex++;

                if (context.getCancellationHandler().isCancelled()) {
                    abortThisResumableUpload();
                    if (recordFile != null) {
                        recordFile.delete();
                    }
                    throwOutInterruptClientException();
                }
            }

            CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(
                    request.getUrl(), request.getObjectKey(), uploadId, partETags);
            if (request.getCallbackParam() != null) {
                complete.setCallbackParam(request.getCallbackParam());
            }
            if (request.getCallbackVars() != null) {
                complete.setCallbackVars(request.getCallbackVars());
            }
            CompleteMultipartUploadResult completeResult = apiOperation.completeMultipartUpload(complete, null).getResult();

            if (recordFile != null) {
                recordFile.delete();
            }

            return new ResumableUploadResult(completeResult);
        }

        private void abortThisResumableUpload() {
            if (uploadId != null) {
                AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(
                        request.getUrl(), request.getObjectKey(), uploadId);
                apiOperation.abortMultipartUpload(abort, null).waitUntilFinished();
            }
        }

        private void throwOutInterruptClientException() throws ClientException {
            IOException e = new IOException();
            throw new ClientException(e.getMessage(), e);
        }
    }
}
