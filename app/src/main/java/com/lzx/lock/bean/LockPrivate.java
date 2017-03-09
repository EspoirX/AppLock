package com.lzx.lock.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by lzx on 2017/3/9.
 * 386707112@qq.com
 */

public class LockPrivate extends DataSupport {

    private boolean isRead;
    private long lookDate;
    private String picPath;
    private String packageName;

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getLookDate() {
        return lookDate;
    }

    public void setLookDate(long lookDate) {
        this.lookDate = lookDate;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
