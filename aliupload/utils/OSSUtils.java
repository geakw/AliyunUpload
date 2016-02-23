package com.eastmoney.video.network.aliupload.utils;


import android.util.Base64;
import android.webkit.MimeTypeMap;

import com.eastmoney.video.network.aliupload.model.ObjectMetadata;
import com.eastmoney.video.network.aliupload.model.PartETag;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSUtils {
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 判断一个字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmptyString(String str) {
        return str == null || str.length() == 0;
    }

    public static String populateMapToBase64JsonString(Map<String, String> map) {
        JSONObject jsonObj = new JSONObject(map);
        return Base64.encodeToString(jsonObj.toString().getBytes(), Base64.NO_WRAP);
    }

    /**
     * Populate metadata to headers.
     */
    public static void populateRequestMetadata(Map<String, String> headers, ObjectMetadata metadata) {
        if (metadata == null) {
            return;
        }

        Map<String, Object> rawMetadata = metadata.getRawMetadata();
        if (rawMetadata != null) {
            for (Map.Entry<String, Object> entry : rawMetadata.entrySet()) {
                headers.put(entry.getKey(), entry.getValue().toString());
            }
        }

        Map<String, String> userMetadata = metadata.getUserMetadata();
        if (userMetadata != null) {
            for (Map.Entry<String, String> entry : userMetadata.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null) key = key.trim();
                if (value != null) value = value.trim();
                headers.put(key, value);
            }
        }
    }

    public static String buildXMLFromPartEtagList(List<PartETag> partETagList) {
        StringBuilder builder = new StringBuilder();
        builder.append("<CompleteMultipartUpload>\n");
        for (PartETag partETag : partETagList) {
            builder.append("<Part>\n");
            builder.append("<PartNumber>" + partETag.getPartNumber() + "</PartNumber>\n");
            builder.append("<ETag>" + partETag.getETag() + "</ETag>\n");
            builder.append("</Part>\n");
        }
        builder.append("</CompleteMultipartUpload>\n");
        return builder.toString();
    }

    public static String determineContentType(String initValue, String srcPath, String toObjectKey) {
        if (initValue != null) {
            return initValue;
        }

        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        if (srcPath != null) {
            String extension = srcPath.substring(srcPath.lastIndexOf('.') + 1);
            String contentType = typeMap.getMimeTypeFromExtension(extension);
            if (contentType != null) {
                return contentType;
            }
        }

        if (toObjectKey != null) {
            String extension = toObjectKey.substring(toObjectKey.lastIndexOf('.') + 1);
            String contentType = typeMap.getMimeTypeFromExtension(extension);
            if (contentType != null) {
                return contentType;
            }
        }

        return "application/octet-stream";
    }

}
