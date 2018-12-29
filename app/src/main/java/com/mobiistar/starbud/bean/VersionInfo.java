package com.mobiistar.starbud.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Description: Version info, contain the app info and products info.
 * Date：18-11-13-下午8:27
 * Author: black
 */
public class VersionInfo implements Serializable {
    private AppInfo appInfo;
    private List<ProductInfo> productInfoList;
    private List<String> productNameWhiteList;

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public List<ProductInfo> getProductInfoList() {
        return productInfoList;
    }

    public void setProductInfoList(List<ProductInfo> productInfoList) {
        this.productInfoList = productInfoList;
    }

    public List<String> getProductNameWhiteList() {
        return productNameWhiteList;
    }

    public void setProductNameWhiteList(List<String> productNameWhiteList) {
        this.productNameWhiteList = productNameWhiteList;
    }
}
