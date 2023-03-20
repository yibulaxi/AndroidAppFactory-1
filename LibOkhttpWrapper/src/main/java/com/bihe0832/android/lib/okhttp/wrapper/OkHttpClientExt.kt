/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/21 下午3:44
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/21 下午3:44
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper

import com.bihe0832.android.lib.log.ZLog
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/21.
 * Description: Description
 *
 */

fun Response.getResponseData(enableLog: Boolean): String {
    val result = StringBuffer()
    if (enableLog) {
        var jsonReader: Reader? = null
        var reader: BufferedReader? = null
        try {
            val responseBody = peekBody(Long.MAX_VALUE)
            val charset = responseBody.contentType()?.charset() ?: Charset.forName("UTF-8")
            jsonReader = InputStreamReader(responseBody.byteStream(), charset)
            reader = BufferedReader(jsonReader)

            var line: String? = reader.readLine()
            do {
                result.append(line)
                line = reader.readLine()
            } while (line != null)
        } catch (e: java.lang.Exception) {
            ZLog.e(OkHttpWrapper.TAG, "getResponseData cause Exception:$e")
            if (enableLog) {
                e.printStackTrace()
            }
        } finally {
            try {
                jsonReader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                reader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return result.toString()
}

fun Request.getRequestParams(enableLog: Boolean): String {
    if (enableLog) {
        this.body()?.let {
            val buffer = okio.Buffer()
            try {
                it.writeTo(buffer)
                val charset = it.contentType()?.charset() ?: Charset.forName("UTF-8")
                return buffer.readString(charset)
            } catch (e: java.lang.Exception) {
                ZLog.e(OkHttpWrapper.TAG, "getResponseData cause Exception:$e")
                e.printStackTrace()
            } finally {
                try {
                    buffer.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    return ""
}


fun getRequestBodyByJsonString(jsonString: String): RequestBody {
    return RequestBody.create(MediaType.parse("application/json"), jsonString)
}