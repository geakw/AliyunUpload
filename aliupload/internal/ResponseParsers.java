package com.eastmoney.video.network.aliupload.internal;



import com.eastmoney.video.network.aliupload.callback.ServiceException;
import com.eastmoney.video.network.aliupload.common.OSSHeaders;
import com.eastmoney.video.network.aliupload.model.AbortMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.CompleteMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.InitiateMultipartUploadResult;
import com.eastmoney.video.network.aliupload.model.ListPartsResult;
import com.eastmoney.video.network.aliupload.model.OSSObjectSummary;
import com.eastmoney.video.network.aliupload.model.PartSummary;
import com.eastmoney.video.network.aliupload.model.PutObjectResult;
import com.eastmoney.video.network.aliupload.model.UploadPartResult;
import com.eastmoney.video.network.aliupload.utils.DateUtil;
import com.eastmoney.video.network.aliupload.utils.OSSLog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public final class ResponseParsers {

    public static final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

    public static final class PutObjectReponseParser implements ResponseParser<PutObjectResult> {

        @Override
        public PutObjectResult parse(Response response)
                throws IOException {
            try {
                PutObjectResult result = new PutObjectResult();
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                result.setETag(trimQuotes(response.header(OSSHeaders.ETAG)));
                if (response.body().contentLength() > 0) {
                    result.setServerCallbackReturnBody(response.body().string());
                }
                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }


    public static final class InitMultipartResponseParser implements ResponseParser<InitiateMultipartUploadResult> {

        @Override
        public InitiateMultipartUploadResult parse(Response response) throws IOException {
            try {
                InitiateMultipartUploadResult result = parseInitMultipartResponseXML(response.body().byteStream());

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class UploadPartResponseParser implements ResponseParser<UploadPartResult> {

        @Override
        public UploadPartResult parse(Response response) throws IOException {
            try {
                UploadPartResult result = new UploadPartResult();

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                result.setETag(trimQuotes(response.header(OSSHeaders.ETAG)));

                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class AbortMultipartUploadResponseParser implements ResponseParser<AbortMultipartUploadResult> {

        @Override
        public AbortMultipartUploadResult parse(Response response) throws IOException {
            try {
                AbortMultipartUploadResult result = new AbortMultipartUploadResult();

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class CompleteMultipartUploadResponseParser implements ResponseParser<CompleteMultipartUploadResult> {

        @Override
        public CompleteMultipartUploadResult parse(Response response) throws IOException {
            try {
                CompleteMultipartUploadResult result = new CompleteMultipartUploadResult();
                if (response.header(OSSHeaders.CONTENT_TYPE).equals("application/xml")) {
                    result = parseCompleteMultipartUploadResponseXML(response.body().byteStream());
                } else if (response.body() != null) {
                    result.setServerCallbackReturnBody(response.body().string());
                }
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class ListPartsResponseParser implements ResponseParser<ListPartsResult> {

        @Override
        public ListPartsResult parse(Response response) throws IOException {
            try {
                ListPartsResult result = parseListPartsResponseXML(response.body().byteStream());

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }


    private static ListPartsResult parseListPartsResponseXML(InputStream in)
            throws ParserConfigurationException, IOException, SAXException, ParseException {

        ListPartsResult result = new ListPartsResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[parseObjectListResponse] - " + element.getNodeName());

        List<PartSummary> partEtagList = new ArrayList<PartSummary>();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equals("Bucket")) {
                result.setBucketName(checkChildNotNullAndGetValue(item));
            } else if (name.equals("Key")) {
                result.setKey(checkChildNotNullAndGetValue(item));
            } else if (name.equals("UploadId")) {
                result.setUploadId(checkChildNotNullAndGetValue(item));
            } else if (name.equals("PartNumberMarker")) {
                String partNumberMarker = checkChildNotNullAndGetValue(item);
                if (partNumberMarker != null) {
                    result.setPartNumberMarker(Integer.valueOf(partNumberMarker));
                }
            } else if (name.equals("NextPartNumberMarker")) {
                String nextPartNumberMarker = checkChildNotNullAndGetValue(item);
                if (nextPartNumberMarker != null) {
                    result.setNextPartNumberMarker(Integer.valueOf(nextPartNumberMarker));
                }
            } else if (name.equals("MaxParts")) {
                String maxParts = checkChildNotNullAndGetValue(item);
                if (maxParts != null) {
                    result.setMaxParts(Integer.valueOf(maxParts));
                }
            } else if (name.equals("IsTruncated")) {
                String isTruncated = checkChildNotNullAndGetValue(item);
                if (isTruncated != null) {
                    result.setTruncated(Boolean.valueOf(isTruncated));
                }
            } else if (name.equals("Part")) {
                NodeList partNodeList = item.getChildNodes();
                PartSummary partSummary = new PartSummary();
                for (int k = 0; k < partNodeList.getLength(); k++) {
                    Node partItem = partNodeList.item(k);
                    String partItemName = partItem.getNodeName();
                    if (partItemName == null) {
                        continue;
                    } else if (partItemName.equals("PartNumber")) {
                        String partNumber = checkChildNotNullAndGetValue(partItem);
                        if (partNumber != null) {
                            partSummary.setPartNumber(Integer.valueOf(partNumber));
                        }
                    } else if (partItemName.equals("LastModified")) {
                        partSummary.setLastModified(DateUtil.parseIso8601Date(checkChildNotNullAndGetValue(partItem)));
                    } else if (partItemName.equals("ETag")) {
                        partSummary.setETag(checkChildNotNullAndGetValue(partItem));
                    } else if(partItemName.equals("Size")) {
                        String size = checkChildNotNullAndGetValue(partItem);
                        if (size != null) {
                            partSummary.setSize(Integer.valueOf(size));
                        }
                    }
                }
                partEtagList.add(partSummary);
            }
        }
        result.setParts(partEtagList);
        return result;
    }

    private static CompleteMultipartUploadResult parseCompleteMultipartUploadResponseXML(InputStream in) throws ParserConfigurationException, IOException, SAXException {
        CompleteMultipartUploadResult result = new CompleteMultipartUploadResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[item] - " + element.getNodeName());

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equalsIgnoreCase("Location")) {
                result.setLocation(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("Bucket")) {
                result.setBucketName(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("Key")) {
                result.setObjectKey(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("ETag")) {
                result.setETag(checkChildNotNullAndGetValue(item));
            }
        }

        return result;
    }

    private static InitiateMultipartUploadResult parseInitMultipartResponseXML(InputStream in)
            throws IOException, SAXException, ParserConfigurationException {

        InitiateMultipartUploadResult result = new InitiateMultipartUploadResult();

        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[item] - " + element.getNodeName());

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equalsIgnoreCase("UploadId")) {
                result.setUploadId(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("Bucket")) {
                result.setBucketName(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("Key")) {
                result.setObjectKey(checkChildNotNullAndGetValue(item));
            }
        }
        return result;
    }

    /**
     * 解析XML中的Contents
     *
     * @param list
     * @return
     */
    private static OSSObjectSummary parseObjectSummaryXML(NodeList list) throws ParseException {
        OSSObjectSummary object = new OSSObjectSummary();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();

            if (name == null) {
                continue;
            } else if (name.equals("Key")) {
                object.setKey(checkChildNotNullAndGetValue(item));
            } else if (name.equals("LastModified")) {
                object.setLastModified(DateUtil.parseIso8601Date(checkChildNotNullAndGetValue(item)));
            } else if (name.equals("Size")) {
                String size = checkChildNotNullAndGetValue(item);
                if (size != null) {
                    object.setSize(Integer.valueOf(size));
                }
            } else if (name.equals("ETag")) {
                object.setETag(checkChildNotNullAndGetValue(item));
            } else if (name.equals("Type")) {
                object.setType(checkChildNotNullAndGetValue(item));
            } else if (name.equals("StorageClass")) {
                object.setStorageClass(checkChildNotNullAndGetValue(item));
            }
        }
        return object;
    }

    private static String parseCommonPrefixXML(NodeList list) {
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equals("Prefix")) {
                return checkChildNotNullAndGetValue(item);
            }
        }
        return "";
    }


    public static String trimQuotes(String s) {
        if (s == null) return null;

        s = s.trim();
        if (s.startsWith("\"")) s = s.substring(1);
        if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);

        return s;
    }


    public static Map<String, String> parseResponseHeader(Response response) {
        Map<String, String> result = new HashMap<String, String>();
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }

    public static ServiceException parseResponseErrorXML(Response response, boolean isHeadRequest)
            throws IOException {

        int statusCode = response.code();
        String requestId = response.header(OSSHeaders.OSS_HEADER_REQUEST_ID);
        String code = null;
        String message = null;
        String hostId = null;
        String errorMessage = null;

        if (!isHeadRequest) {
            try {
                errorMessage = response.body().string();
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(errorMessage));
                Document dom = builder.parse(is);
                Element element = dom.getDocumentElement();

                NodeList list = element.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node item = list.item(i);
                    String name = item.getNodeName();
                    if (name == null) continue;

                    if (name.equals("Code")) {
                        code = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("Message")) {
                        message = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("RequestId")) {
                        requestId = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("HostId")) {
                        hostId = checkChildNotNullAndGetValue(item);
                    }
                }
                response.body().close();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return new ServiceException(statusCode, message, code, requestId, hostId, errorMessage);
    }

    /**
     * 检查xml单节点有子节点并取值
     * @param item
     */
    public static String checkChildNotNullAndGetValue(Node item) {
        if (item.getFirstChild() != null) {
            return item.getFirstChild().getNodeValue();
        }
        return null;
    }

    public static void safeCloseResponse(Response response) {
        try {
            response.body().close();
        } catch(Exception e) {
        }
    }
}
