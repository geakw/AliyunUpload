/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.eastmoney.video.network.aliupload.model;

/**
 * 包含Multipart上传的Part的返回结果信息。
 *
 */
public class PartETag {

    private int partNumber;

    private String eTag;

    /**
     * 构造函数。
     * @param partNumber
     *          Part标识号码。
     * @param eTag
     *          Part的ETag值。
     */
    public PartETag(int partNumber, String eTag) {
        this.partNumber = partNumber;
        this.eTag = eTag;
    }

    /**
     * 返回Part标识号码。
     * @return Part标识号码。
     */
    public int getPartNumber() {
        return partNumber;
    }

    /**
     * 设置Part标识号码。
     * @param partNumber
     *          Part标识号码。
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * 返回Part的ETag值。
     * @return Part的ETag值。
     */
    public String getETag() {
        return eTag;
    }

    /**
     * 设置Part的ETag值。
     * @param eTag
     *          Part的ETag值。
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

}
