package com.bihe0832.android.lib.router;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2017-07-18.
 *         Description: Description
 */

public class RouterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RouterContext.RouterCallback callback = RouterContext.INSTANCE.getGlobalRouterCallback();

        Uri uri = getIntent().getData();
        int flag = Intent.FLAG_ACTIVITY_SINGLE_TOP;

        if (uri != null) {
            try {
                flag = Integer.parseInt(uri.getQueryParameter("zixie_router_flag"), Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } catch (Exception e) {
                e.printStackTrace();
                flag = Intent.FLAG_ACTIVITY_SINGLE_TOP;
            }
            Routers.open(this, uri, flag, callback);
        }
        finish();
    }
}
