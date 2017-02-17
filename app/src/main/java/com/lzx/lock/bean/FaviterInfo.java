package com.lzx.lock.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by xian on 2017/2/17.
 *推荐加锁的应用信息
 */

public class FaviterInfo extends DataSupport {
    public String packageName;

    public FaviterInfo() {
    }

    public FaviterInfo(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
