package com.mobiistar.starbud.bean;

import java.io.Serializable;

/**
 * Description: App info
 * Date：18-11-13-下午8:27
 * Author: black
 */
public class AppInfo implements Serializable {
    private String appName;
    private String description;
    private int versionCode;
    private String versionName;
    private String apkUrl;
    private long apkLength;
    private long apkReleaseDate;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public long getApkLength() {
        return apkLength;
    }

    public void setApkLength(long apkLength) {
        this.apkLength = apkLength;
    }

    public long getApkReleaseDate() {
        return apkReleaseDate;
    }

    public void setApkReleaseDate(long apkReleaseDate) {
        this.apkReleaseDate = apkReleaseDate;
    }
}
