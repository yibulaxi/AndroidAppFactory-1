package com.bihe0832.android.lib.utils.encrypt;

import java.io.File;
import java.io.InputStream;

/**
 * Created by zixie on 2017/9/15.
 */

public class MD5 {

    private static final String MESSAGE_DIGEST_TYPE = "MD5";

    public static String getMd5(String string) {
        return MessageDigestUtils.getDigestData(string, MESSAGE_DIGEST_TYPE);
    }

    public static String getMd5(byte[] bytes) {
        return MessageDigestUtils.getDigestData(bytes, MESSAGE_DIGEST_TYPE);
    }

    public static String getFileMD5(String fileName) {
        return MessageDigestUtils.getFileDigestData(fileName, MESSAGE_DIGEST_TYPE);
    }

    public static String getFileMD5(File sourceFile) {
        return MessageDigestUtils.getFileDigestData(sourceFile, MESSAGE_DIGEST_TYPE);
    }

    public static String getInputStreamMd5(InputStream is) {
        return MessageDigestUtils.getInputStreamDigestData(is, MESSAGE_DIGEST_TYPE);
    }

    public static String getFileMD5(String fileName, long start, long end) {
        return MessageDigestUtils.getFileDigestData(fileName, MESSAGE_DIGEST_TYPE, start, end);
    }

    public static String getFileMD5(File sourceFile, long start, long end) {
        return MessageDigestUtils.getFileDigestData(sourceFile, MESSAGE_DIGEST_TYPE, start, end);
    }

    public static String getInputStreamMd5(InputStream bis, long start, long end) {
        return MessageDigestUtils.getInputStreamDigestData(bis, MESSAGE_DIGEST_TYPE, start, end);
    }
}