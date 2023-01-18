package com.bihe0832.android.lib.file.select;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

/**
 * @author zixie code@bihe0832.com Created on 4/8/21.
 */
public class FileSelectTools {

    public static final int FILE_CHOOSER = 1;
    public static final String INTENT_EXTRA_KEY_WEB_URL = "url";
    public static final String INTENT_EXTRA_KEY_NEED_SDCARD_PERMISSION = "permission";

    public static void openFileSelect(Activity activity, String url, boolean needSDCardPermission) {
        try {
            Intent intent = new Intent(activity, FileActivity.class);
            intent.setAction(Intent.ACTION_PICK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (!TextUtils.isEmpty(url)) {
                intent.putExtra(INTENT_EXTRA_KEY_WEB_URL, url);
            }
            if (needSDCardPermission) {
                intent.putExtra(INTENT_EXTRA_KEY_NEED_SDCARD_PERMISSION, true);
            }
            ActivityCompat.startActivityForResult(activity, intent, FILE_CHOOSER, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openFileSelect(Activity activity, String url) {
        openFileSelect(activity, url, false);
    }


    public static void openFileSelect(Activity activity) {
        openFileSelect(activity, "");
    }
}
