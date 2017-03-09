package com.lzx.lock.db;

import android.content.ContentValues;
import android.content.Context;

import com.lzx.lock.bean.LockPrivate;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by lzx on 2017/3/9.
 * 386707112@qq.com
 */

public class LockPrivateManager {

    private Context mContext;

    public LockPrivateManager(Context context) {
        this.mContext = context;
    }

    public void addLockPrivate(LockPrivate lockPrivate) {
        lockPrivate.save();
    }

    public void replaceLockPrivate(LockPrivate lockPrivate) {
        ContentValues values = new ContentValues();
        values.put("isRead", lockPrivate.isRead());
        values.put("lookDate", lockPrivate.getLookDate());
        values.put("picPath", lockPrivate.getPicPath());
        DataSupport.updateAll(LockPrivate.class, values, "packageName = ?", lockPrivate.getPackageName());
    }

    public void setLockPrivateWasReaded(LockPrivate lockPrivate) {
        ContentValues values = new ContentValues();
        values.put("isRead", true);
        DataSupport.updateAll(LockPrivate.class, values, "packageName = ?", lockPrivate.getPackageName());
    }

    public List<LockPrivate> getAllLockPrivate() {
        return DataSupport.findAll(LockPrivate.class);
    }

    public void clearLookMyPrivate() {
        DataSupport.deleteAll(LockPrivate.class);
    }
}
