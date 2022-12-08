/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/27 下午8:40
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/27 下午8:40
 *
 */
package com.bihe0832.android.lib.okhttp.wrapper.interceptor

import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.okhttp.wrapper.getRequestParams
import com.bihe0832.android.lib.okhttp.wrapper.getResponseData
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository.getNetworkContentDataRecordByContentID
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestContentDataRecord
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/6/27.
 * Description: Description
 */
open class AAFOKHttpInterceptor(private var enableIntercept: Boolean = false, private var enableLog: Boolean = false) : Interceptor {

    private val HTTP_REQ_PROPERTY_CONTENT_ENCODING = "Content-Encoding"

    protected fun interceptResponse(requestId: String?, response: Response): Response {
        return response
    }

    protected fun interceptRequest(requestId: String?, request: Request): Request {
        return request
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestId = OkHttpWrapper.generateRequestID()
        var requestContentDataRecord: RequestContentDataRecord? = null
        val request = interceptRequest(requestId, chain.request().newBuilder().header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_ID, requestId).build())
        if (enableIntercept) {
            requestContentDataRecord = getNetworkContentDataRecordByContentID(requestId)
            val connection = chain.connection()
            val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
            requestContentDataRecord.url = request.url().toString()
            requestContentDataRecord.method = request.method()
            requestContentDataRecord.protocol = protocol
            requestContentDataRecord.requestHeadersMap = request.headers()
            val requestBody = request.body()
            if (requestBody != null) {
                val contentLength = requestBody.contentLength()
                requestContentDataRecord.requestBodyLength = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
                if (requestBody.contentType() != null) {
                    requestContentDataRecord.requestContentType = requestBody.contentType()
                }
                requestContentDataRecord.requestBody = request.getRequestParams(enableLog)
            }
        }

        val response: Response
        try {
            response = interceptResponse(requestId, chain.proceed(request))
        } catch (var25: Exception) {
            if (enableIntercept) {
                requestContentDataRecord?.errorMsg = var25.toString()
            }
            throw var25
        }

        if (enableIntercept) {
            requestContentDataRecord?.responseHeadersMap = response.headers()
            requestContentDataRecord?.status = response.code()
            requestContentDataRecord?.errorMsg = response.message() ?: ""
            val responseBody = response.body()
            if (responseBody != null) {
                val contentLength = responseBody.contentLength()
                requestContentDataRecord?.responseBodyLength = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
                if (responseBody.contentType() != null) {
                    requestContentDataRecord?.responseContentType = responseBody.contentType()
                }
                requestContentDataRecord?.responseBody = response.getResponseData(enableLog)
            }
        }
        return response
    }
}