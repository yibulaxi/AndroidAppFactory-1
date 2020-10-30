package com.bihe0832.android.framework.base;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.bihe0832.android.lib.config.Config;
import com.bihe0832.android.lib.lifecycle.LifecycleHelper;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.web.WebViewHelper;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.ZixieCoreInit;
import com.bihe0832.android.framework.constant.Constants;

import java.util.List;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Application", "base BaseApplication onCreate start");
        LifecycleHelper.INSTANCE.init(this);
        ZixieContext.INSTANCE.init(getApplicationContext());
        ZixieCoreInit.INSTANCE.initCore(getApplicationContext());
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo it : runningApps) {
            if (it.pid == android.os.Process.myPid() && it.processName != null && it.processName.contains(getPackageName())) {
                ZLog.e("Application initCore process: name:" + it.processName + " and id:" + it.pid);
                if (it.processName.equalsIgnoreCase(getPackageName())) {
                    ZixieContext.INSTANCE.showDebugEditionToast();
                } else if (it.processName.equalsIgnoreCase(getPackageName() + ":web")) {
                    //WEb进程
                    ZixieCoreInit.INSTANCE.initCore(getApplicationContext());
                    initWeb(getApplicationContext(), it.processName);
                }else {
                    ZixieContext.INSTANCE.showDebug("独立进程初始化：" + it.processName);
                    ZLog.e("Application skip initCore process: name:" + it.processName + " and id:" + it.pid);
                }
            }
        }
        Log.d("Application", "base BaseApplication onCreate end");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    protected void initWeb(final Context context,final String name) {
        ZLog.e("Application process: name:" + name + " initCore web");
        int delay = 0;
        if(!TextUtils.isEmpty(name) && name.equalsIgnoreCase(getPackageName())){
            delay = 5;
        }
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                try {
                    ZLog.e("Application process: name::" + name + " initCore web start");
                    WebViewHelper.init(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        },delay);
    }
}
