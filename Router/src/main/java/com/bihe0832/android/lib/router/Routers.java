package com.bihe0832.android.lib.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-07-19.
 *         Description: Description
 */
public class Routers {

    public static final String ROUTERS_KEY_RAW_URL = "com.bihe0832.router.URI";

    private static void initIfNeed(String format) {
        if (!RouterMappingManager.getInstance().getMappings().isEmpty() && RouterMappingManager.getInstance()
                .getMappings().containsKey(format)) {
            return;
        }
        RouterMappingManager.getInstance().initMapping(format);
    }

    public static boolean open(Context context, String url) {
        return open(context, Uri.parse(url), Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    public static boolean open(Context context, String url, int startFlag) {
        return open(context, Uri.parse(url), startFlag);
    }


    public static boolean open(Context context, String url, int startFlag, RouterContext.RouterCallback callback) {
        return open(context, Uri.parse(url), startFlag, callback);
    }

    public static boolean open(Context context, Uri uri, int startFlag) {
        return open(context, uri, startFlag, RouterContext.INSTANCE.getGlobalRouterCallback());
    }

    public static boolean open(Context context, Uri uri, int startFlag, RouterContext.RouterCallback callback) {
        return open(context, uri, -1, startFlag, callback);
    }

    public static boolean openForResult(Activity activity, String url, int requestCode, int startFlag) {
        return openForResult(activity, Uri.parse(url), requestCode, startFlag);
    }

    public static boolean openForResult(Activity activity, String url, int requestCode, int startFlag,
            RouterContext.RouterCallback callback) {
        return openForResult(activity, Uri.parse(url), requestCode, startFlag, callback);
    }

    public static boolean openForResult(Activity activity, Uri uri, int requestCode, int startFlag) {
        return openForResult(activity, uri, requestCode, startFlag, RouterContext.INSTANCE.getGlobalRouterCallback());
    }

    public static boolean openForResult(Activity activity, Uri uri, int requestCode, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(activity, uri, requestCode, startFlag, callback);
    }

    private static boolean open(Context context, Uri uri, int requestCode, int startFlag,
            RouterContext.RouterCallback callback) {
        boolean success = false;
        if (callback != null) {
            if (callback.beforeOpen(context, uri)) {
                return false;
            }
        }

        try {
            success = doOpen(context, uri, requestCode, startFlag);
        } catch (Throwable e) {
            e.printStackTrace();
            if (callback != null) {
                callback.error(context, uri, e);
            }
        }

        if (callback != null) {
            if (success) {
                callback.afterOpen(context, uri);
            } else {
                callback.notFound(context, uri);
            }
        }
        return success;
    }

    public static Intent resolve(Context context, String url) {
        return resolve(context, Uri.parse(url));
    }

    public static Intent resolve(Context context, Uri uri) {
        initIfNeed(uri.getHost());
        for (Map.Entry<String, Class<? extends Activity>> entry : RouterMappingManager.getInstance().getMappings()
                .entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            if (entry.getKey().equalsIgnoreCase(uri.getHost()) && null != entry.getValue()) {
                Intent intent = new Intent(context, entry.getValue());
                intent.putExtras(parseExtras(uri));
                intent.putExtra(ROUTERS_KEY_RAW_URL, uri.toString());
                return intent;
            }
        }
        return null;
    }

    private static boolean doOpen(Context context, Uri uri, int requestCode, int startFlag) {
        initIfNeed(uri.getHost());
        if (RouterMappingManager.getInstance().getMappings().containsKey(uri.getHost())) {
            Class<? extends Activity> activityClass = RouterMappingManager.getInstance().getMappings()
                    .get(uri.getHost());
            if (null != activityClass) {
                try {
                    Intent intent = new Intent(context, activityClass);
                    intent.putExtras(parseExtras(uri));
                    intent.putExtra(ROUTERS_KEY_RAW_URL, uri.toString());
                    if (!(context instanceof Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(startFlag);
                    }
                    if (requestCode >= 0) {
                        if (context instanceof Activity) {
                            ((Activity) context).startActivityForResult(intent, requestCode);
                        } else {
                            throw new RuntimeException("can not startActivityForResult context " + context);
                        }
                    } else {
                        context.startActivity(intent);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        } else {
            try {
                Intent intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                System.out.println("jumpToOtherApp url:" + uri.toString() + ",intent:" + intent.toString());
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(startFlag);
                }
                if (requestCode >= 0) {
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, requestCode);
                    } else {
                        throw new RuntimeException("can not startActivityForResult context " + context);
                    }
                } else {
                    context.startActivity(intent);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public static Bundle parseExtras(Uri uri) {
        Bundle bundle = new Bundle();
        Set<String> names = UriCompact.getQueryParameterNames(uri);
        for (String name : names) {
            String value = uri.getQueryParameter(name);
            bundle.putString(name.toLowerCase(), value);
        }
        return bundle;
    }

    public static ArrayList<Class<? extends Activity>> getMainActivityList() {
        return RouterMappingManager.getInstance().getActivityIsMain();
    }

    public static HashMap<String, Class<? extends Activity>> getRouterMappings() {
        return RouterMappingManager.getInstance().getMappings();
    }
}
