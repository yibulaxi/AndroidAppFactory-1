package com.bihe0832.android.lib.http.common.core;

import static com.bihe0832.android.lib.http.common.core.BaseConnection.HTTP_REQ_PROPERTY_CHARSET;
import static com.bihe0832.android.lib.http.common.core.BaseConnection.HTTP_REQ_PROPERTY_CONTENT_TYPE;
import static com.bihe0832.android.lib.http.common.core.BaseConnection.HTTP_REQ_VALUE_CHARSET;
import static com.bihe0832.android.lib.http.common.core.BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_FORM;
import static com.bihe0832.android.lib.http.common.core.HttpBasicRequest.HTTP_REQ_ENTITY_MERGE;
import static com.bihe0832.android.lib.http.common.core.HttpBasicRequest.LOG_TAG;

import android.content.Context;
import android.text.TextUtils;
import com.bihe0832.android.lib.http.common.HTTPServer;
import com.bihe0832.android.lib.log.ZLog;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hardyshi code@bihe0832.com Created on 2021/11/18.
 */
public class HttpFileUpload {

    /**
     * post请求方法
     */
    public String postRequest(final Context context, final BaseConnection baseConnection,
            final Map<String, String> strParams,
            final List<FileInfo> fileParams) {

        baseConnection.setURLConnectionCommonPara();
        HashMap<String, String> requestProperty = new HashMap<>();
        requestProperty.put(HTTP_REQ_PROPERTY_CHARSET, HTTP_REQ_VALUE_CHARSET);
        requestProperty.put(HTTP_REQ_PROPERTY_CONTENT_TYPE, HTTP_REQ_VALUE_CONTENT_TYPE_FORM);
        baseConnection.setURLConnectionRequestProperty(requestProperty);
        HttpURLConnection urlConnection = baseConnection.getURLConnection();
        DataOutputStream paramDataOutputStream = null;
        InputStream resultInptStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            urlConnection.setRequestMethod(BaseConnection.HTTP_REQ_METHOD_POST);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty(HTTP_REQ_PROPERTY_CHARSET, HTTP_REQ_VALUE_CHARSET);
            urlConnection.setRequestProperty(HTTP_REQ_PROPERTY_CONTENT_TYPE,
                    HTTP_REQ_VALUE_CONTENT_TYPE_FORM + ";boundary=" + HTTPServer.BOUNDARY);

            paramDataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            paramDataOutputStream.writeBytes(getFormDataString(strParams).toString());
            paramDataOutputStream.flush();

            for (FileInfo fileInfo : fileParams) {
                paramDataOutputStream.writeBytes(fileInfo.getRequesetData(HTTPServer.BOUNDARY));
                paramDataOutputStream.flush();
                InputStream paramInputStream = null;

                if (fileInfo.getFileUri() != null) {
                    paramInputStream =
                            context.getContentResolver().openInputStream(fileInfo.getFileUri());
                } else {
                    paramInputStream = new FileInputStream(fileInfo.getFile());
                }
                if (paramInputStream != null) {
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = paramInputStream.read(buffer)) != -1) {
                        paramDataOutputStream.write(buffer, 0, len);
                    }
                    paramInputStream.close();
                }

                paramDataOutputStream.writeBytes(BaseConnection.HTTP_REQ_ENTITY_LINE_END);
            }
            //请求结束标志
            paramDataOutputStream.writeBytes(
                    BaseConnection.HTTP_REQ_ENTITY_PREFIX + HTTPServer.BOUNDARY + BaseConnection.HTTP_REQ_ENTITY_PREFIX
                            + BaseConnection.HTTP_REQ_ENTITY_LINE_END);
            paramDataOutputStream.flush();
            paramDataOutputStream.close();
            ZLog.e(LOG_TAG, "postResponseCode() = " + urlConnection.getResponseCode());
            //读取服务器返回信息
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                resultInptStream = urlConnection.getInputStream();
                byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] resultBuffer = new byte[8192];
                int resultLen;
                while ((resultLen = resultInptStream.read(resultBuffer)) != -1) {
                    byteArrayOutputStream.write(resultBuffer, 0, resultLen);
                }
                resultInptStream.close();
                return byteArrayOutputStream.toString(HTTP_REQ_VALUE_CHARSET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (paramDataOutputStream != null) {
                    paramDataOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (resultInptStream != null) {
                    resultInptStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 对post参数进行编码处理
     */
    public String getFormDataString(Map<String, String> strParams) {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : strParams.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey()) && TextUtils.isEmpty(entry.getValue())) {
                break;
            } else {
                stringBuffer.append(BaseConnection.HTTP_REQ_ENTITY_PREFIX)
                        .append(HTTPServer.BOUNDARY)
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                        .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_DISPOSITION).append(": ").append("form-data")
                        .append(";")
                        .append(" name").append(HTTP_REQ_ENTITY_MERGE).append("\"").append(entry.getKey()).append("\"")
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                        .append(HTTP_REQ_PROPERTY_CONTENT_TYPE).append(": ")
                        .append(BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_TEXT).append("; ")
                        .append(HTTP_REQ_PROPERTY_CHARSET).append(HTTP_REQ_ENTITY_MERGE)
                        .append(HTTP_REQ_VALUE_CHARSET).append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                        .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_TRANSFER_ENCODING).append(": 8bit")
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)// 参数头设置完以后需要两个换行，然后才是参数内容
                        .append(entry.getValue())
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END);
            }
        }
        String result = stringBuffer.toString();
        ZLog.e(LOG_TAG, "getFormDataString = " + result);
        return stringBuffer.toString();

    }
}