package com.mobiistar.starbud.bean;

import java.io.Serializable;

/**
 * Description: Product info.
 * Date：18-11-13-下午8:27
 * Author: black
 */
public class ProductInfo implements Serializable {
    private String productName;
    private String description;
    private int fwVersionCode;
    private String fwVersionName;
    private String fwFileUrl;
    private long fwFileLength;
    private long fwReleaseDate;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFwVersionCode() {
        return fwVersionCode;
    }

    public void setFwVersionCode(int fwVersionCode) {
        this.fwVersionCode = fwVersionCode;
    }

    public String getFwVersionName() {
        return fwVersionName;
    }

    public void setFwVersionName(String fwVersionName) {
        this.fwVersionName = fwVersionName;
    }

    public String getFwFileUrl() {
        return fwFileUrl;
    }

    public void setFwFileUrl(String fwFileUrl) {
        this.fwFileUrl = fwFileUrl;
    }

    public long getFwFileLength() {
        return fwFileLength;
    }

    public void setFwFileLength(long fwFileLength) {
        this.fwFileLength = fwFileLength;
    }

    public long getFwReleaseDate() {
        return fwReleaseDate;
    }

    public void setFwReleaseDate(long fwReleaseDate) {
        this.fwReleaseDate = fwReleaseDate;
    }
}
