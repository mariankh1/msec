package com.app.malloc.malloc.data;

import android.app.usage.UsageStats;
import android.graphics.drawable.Drawable;

import java.util.Locale;

/**
 * App Item
 * Created by zb on 18/12/2017.
 */

public class AppItem {
    public String mName;
    public String mPackageName;
    public long mEventTime;
    public long mUsageTime;
    public int mEventType;
    public int mCount;
    public long mMobile;
    public boolean mCanOpen;
    public Long mWifi;
    private boolean mIsSystem;
    public int mMemory;
    public UsageStats usageStats;
    public Drawable appIcon;
    public String appLabel;
    public String cpuUsage;
    public String memUsageKB;
    public String memUsagePercent;
    public String pid;
    public String packageName;


    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "name:%s package_name:%s time:%d total:%d type:%d system:%b count:%d",
                mName, mPackageName, mEventTime, mUsageTime, mEventType, mIsSystem, mCount);
    }

    public AppItem copy() {
        AppItem newItem = new AppItem();
        newItem.mName = this.mName;
        newItem.mPackageName = this.mPackageName;
        newItem.packageName = this.packageName;
        newItem.mEventTime = this.mEventTime;
        newItem.mUsageTime = this.mUsageTime;
        newItem.mEventType = this.mEventType;
        newItem.mIsSystem = this.mIsSystem;
        newItem.mCount = this.mCount;
        newItem.mMemory = this.mMemory;
        newItem.usageStats = this.usageStats;
        newItem.appIcon = this.appIcon;
        newItem.appLabel = this.appLabel;
        newItem.cpuUsage = this.cpuUsage;
        newItem.memUsageKB = this.memUsageKB;

        return newItem;
    }
}
