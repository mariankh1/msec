package com.app.malloc.malloc;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.app.malloc.malloc.data.AppItem;
import com.app.malloc.malloc.data.DataManager;
import com.app.malloc.malloc.db.DbHistoryExecutor;
import com.app.malloc.malloc.db.DbIgnoreExecutor;
import com.app.malloc.malloc.service.AppService;
import com.app.malloc.malloc.util.CrashHandler;
import com.app.malloc.malloc.util.PreferenceManager;

/**
 * My Application
 * Created by zb on 18/12/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.init(this);
        getApplicationContext().startService(new Intent(getApplicationContext(), AppService.class));
        DbIgnoreExecutor.init(getApplicationContext());
        DbHistoryExecutor.init(getApplicationContext());
        DataManager.init();
        addDefaultIgnoreAppsToDB();

        if (AppConst.CRASH_TO_FILE) CrashHandler.getInstance().init();
        initAppsFlyer();
    }

    private void addDefaultIgnoreAppsToDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> mDefaults = new ArrayList<String>();
                mDefaults.add("com.android.settings");
                mDefaults.add(BuildConfig.APPLICATION_ID);
                for (String packageName : mDefaults) {
                    AppItem item = new AppItem();
                    item.mPackageName = packageName;
                    item.mEventTime = System.currentTimeMillis();
                    DbIgnoreExecutor.getInstance().insertItem(item);
                }
            }
        }).run();
    }

    private void initAppsFlyer() {
        AppsFlyerConversionListener conversionDataListener = new AppsFlyerConversionListener() {
            @Override
            public void onInstallConversionDataLoaded(Map<String, String> map) {
                Log.d(">>>", "onInstallConversionDataLoaded:" + map.toString());
            }

            @Override
            public void onInstallConversionFailure(String s) {
                Log.d(">>>", "onInstallConversionFailure:" + s);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> map) {
                Log.d(">>>", "onAppOpenAttribution:" + map.toString());
            }

            @Override
            public void onAttributionFailure(String s) {
                Log.d(">>>", "onAttributionFailure:" + s);
            }
        };
        AppsFlyerLib.getInstance().setCustomerUserId(AppConst.AF_KEY);

      //  AppsFlyerLib.getInstance().init(AppConst.AF_KEY, conversionDataListener, getApplicationContext());
        AppsFlyerLib.getInstance().startTracking(this, "malloc");
    }
}
