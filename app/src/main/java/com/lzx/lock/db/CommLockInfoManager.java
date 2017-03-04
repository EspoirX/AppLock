package com.lzx.lock.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.lzx.lock.base.AppConstants;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.bean.FaviterInfo;
import com.lzx.lock.utils.DataUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.litepal.crud.DataSupport.where;

/**
 * Created by xian on 2017/2/17.
 */

public class CommLockInfoManager {

    private PackageManager mPackageManager;
    private Context mContext;

    public CommLockInfoManager(Context mContext) {
        this.mContext = mContext;
        mPackageManager = mContext.getPackageManager();
    }

    /**
     * 查找所有
     */
    public synchronized List<CommLockInfo> getAllCommLockInfos() {
        List<CommLockInfo> commLockInfos = DataSupport.findAll(CommLockInfo.class);
        Collections.sort(commLockInfos, commLockInfoComparator);
        return commLockInfos;
    }

    /**
     * 删除数据
     */
    public synchronized void deleteCommLockInfoTable(List<CommLockInfo> commLockInfos) {
        for (CommLockInfo info : commLockInfos) {
            DataSupport.deleteAll(CommLockInfo.class, "packageName = ?", info.getPackageName());
        }
    }

    /**
     * 将手机应用信息插入数据库
     */
    public synchronized void instanceCommLockInfoTable(List<ResolveInfo> resolveInfos) throws PackageManager.NameNotFoundException {
        List<CommLockInfo> list = new ArrayList<>();

        for (ResolveInfo resolveInfo : resolveInfos) {
            boolean isfaviterApp = isHasFaviterAppInfo(resolveInfo.activityInfo.packageName); //是否为推荐加锁的app
            CommLockInfo commLockInfo = new CommLockInfo(resolveInfo.activityInfo.packageName, false, isfaviterApp); // 后续需添加默认的开启保护
            ApplicationInfo appInfo = mPackageManager.getApplicationInfo(commLockInfo.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
            String appName = mPackageManager.getApplicationLabel(appInfo).toString();
            //过滤掉一些应用
            if (!commLockInfo.getPackageName().equals(AppConstants.APP_PACKAGE_NAME) && !commLockInfo.getPackageName().equals("com.android.settings")
                    && !commLockInfo.getPackageName().equals("com.google.android.googlequicksearchbox")) {
                if (isfaviterApp) { //如果是推荐的
                    commLockInfo.setLocked(true);
                } else {
                    commLockInfo.setLocked(false);
                }
                commLockInfo.setAppName(appName);
                commLockInfo.setSetUnLock(false);

                list.add(commLockInfo);
            }
        }
        list = DataUtil.clearRepeatCommLockInfo(list);  //去除重复数据

        DataSupport.saveAll(list);
    }

    /**
     * 判断是否是推荐加锁的应用
     */
    public boolean isHasFaviterAppInfo(String packageName) {
        List<FaviterInfo> infos = DataSupport.where("packageName = ?", packageName).find(FaviterInfo.class);
        return infos.size() > 0;
    }

    /**
     * 更改数据库app状态为锁定
     */
    public void lockCommApplication(String packageName) {
        updateLockStatus(packageName, true);
    }

    /**
     * 更改数据库app状态为已解锁
     */
    public void unlockCommApplication(String packageName) {
        updateLockStatus(packageName, false);
    }

    public void updateLockStatus(String packageName, boolean isLock) {
        ContentValues values = new ContentValues();
        values.put("isLocked", isLock);
        DataSupport.updateAll(CommLockInfo.class, values, "packageName = ?", packageName);
    }


    /**
     * 是否设置了不锁
     */
    public boolean isSetUnLock(String packageName) {
        List<CommLockInfo> lockInfos = where("packageName = ?", packageName).find(CommLockInfo.class);
        for (CommLockInfo commLockInfo : lockInfos) {
            if (commLockInfo.isSetUnLock()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查状态是否为锁定
     *
     * @param packageName
     * @return
     */
    public boolean isLockedPackageName(String packageName) {
        List<CommLockInfo> lockInfos = where("packageName = ?", packageName).find(CommLockInfo.class);
        for (CommLockInfo commLockInfo : lockInfos) {
            if (commLockInfo.isLocked()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 模糊匹配
     */
    public List<CommLockInfo> queryBlurryList(String appName) {
        List<CommLockInfo> infos = DataSupport.where("appName like ?", "%" + appName + "%").find(CommLockInfo.class);
        return infos;
    }

    public void setIsUnLockThisApp(String packageName, boolean isSetUnLock) {
        ContentValues values = new ContentValues();
        values.put("isSetUnLock", isSetUnLock);
        DataSupport.updateAll(CommLockInfo.class, values, "packageName = ?", packageName);
    }


    private Comparator commLockInfoComparator = new Comparator() {

        @Override
        public int compare(Object lhs, Object rhs) {
            CommLockInfo leftCommLockInfo = (CommLockInfo) lhs;
            CommLockInfo rightCommLockInfo = (CommLockInfo) rhs;

            if (leftCommLockInfo.isFaviterApp()
                    && !leftCommLockInfo.isLocked()
                    && !rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                return -1;
            } else if (leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && !rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                return -1;
            } else if (leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                if (leftCommLockInfo.getAppInfo() != null
                        && rightCommLockInfo.getAppInfo() != null)
                    return 1;
                else
                    return 0;
            } else if (!leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && !rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                return -1;
            } else if (leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && !rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                return -1;
            } else if (leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                if (leftCommLockInfo.getAppInfo() != null
                        && rightCommLockInfo.getAppInfo() != null)
                    return 1;
                else
                    return 0;
            } else if (!leftCommLockInfo.isFaviterApp()
                    && !leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                return 1;
            } else if (leftCommLockInfo.isFaviterApp()
                    && !leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                if (leftCommLockInfo.getAppInfo() != null
                        && rightCommLockInfo.getAppInfo() != null)
                    return 1;
                else
                    return 0;
            } else if (leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                if (leftCommLockInfo.getAppInfo() != null
                        && rightCommLockInfo.getAppInfo() != null)
                    return 1;
                else
                    return 0;
            } else if (!leftCommLockInfo.isFaviterApp()
                    && !leftCommLockInfo.isLocked()
                    && !rightCommLockInfo.isFaviterApp()
                    && rightCommLockInfo.isLocked()) {
                return 1;
            } else if (!leftCommLockInfo.isFaviterApp()
                    && !leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && rightCommLockInfo.isLocked()) {
                return 1;
            } else if (!leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && rightCommLockInfo.isLocked()) {
                return 1;
            } else if (!leftCommLockInfo.isFaviterApp()
                    && !leftCommLockInfo.isLocked()
                    && !rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                if (leftCommLockInfo.getAppInfo() != null
                        && rightCommLockInfo.getAppInfo() != null)
                    return 1;
                else
                    return 0;
            } else if (leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && rightCommLockInfo.isLocked()) {
                if (leftCommLockInfo.getAppInfo() != null
                        && rightCommLockInfo.getAppInfo() != null)
                    return 1;
                else
                    return 0;
            } else if (!leftCommLockInfo.isFaviterApp()
                    && !leftCommLockInfo.isLocked()
                    && rightCommLockInfo.isFaviterApp()
                    && rightCommLockInfo.isLocked()) {
                return 1;
            } else if (!leftCommLockInfo.isFaviterApp()
                    && leftCommLockInfo.isLocked()
                    && !rightCommLockInfo.isFaviterApp()
                    && !rightCommLockInfo.isLocked()) {
                return -1;
            }
            return 0;
        }
    };

}
