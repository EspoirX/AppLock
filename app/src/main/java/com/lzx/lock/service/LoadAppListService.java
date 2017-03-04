package com.lzx.lock.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.lzx.lock.base.AppConstants;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.bean.FaviterInfo;
import com.lzx.lock.db.CommLockInfoManager;
import com.lzx.lock.utils.SpUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 后台加载应用列表
 * Created by xian on 2017/2/17.
 */

public class LoadAppListService extends IntentService {

    public static final String ACTION_START_LOAD_APP = "com.lzx.lock.service.action.LOADAPP";
    private PackageManager mPackageManager;
    private CommLockInfoManager mLockInfoManager;
    long time = 0;

    public LoadAppListService() {
        super("LoadAppListService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPackageManager = getPackageManager();
        mLockInfoManager = new CommLockInfoManager(this);
    }

    @Override
    protected void onHandleIntent(Intent handleIntent) {

        time = System.currentTimeMillis();

        boolean isInitFaviter = SpUtil.getInstance().getBoolean(AppConstants.LOCK_IS_INIT_FAVITER, false);
        boolean isInitDb = SpUtil.getInstance().getBoolean(AppConstants.LOCK_IS_INIT_DB, false);
        if (!isInitFaviter) {
            SpUtil.getInstance().putBoolean(AppConstants.LOCK_IS_INIT_FAVITER, true);
            initFavoriteApps();
        }

        //每次都获取手机上的所有应用
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(intent, 0);
        //非第一次，对比数据
        if (isInitDb) {
            List<ResolveInfo> appList = new ArrayList<>();
            List<CommLockInfo> dbList = mLockInfoManager.getAllCommLockInfos(); //获取数据库列表
            //处理应用列表
            for (ResolveInfo resolveInfo : resolveInfos) {
                if (!resolveInfo.activityInfo.packageName.equals(AppConstants.APP_PACKAGE_NAME) &&
                        !resolveInfo.activityInfo.packageName.equals("com.android.settings")) {
                    appList.add(resolveInfo);
                }
            }
            if (appList.size() > dbList.size()) { //如果有安装新应用
                List<ResolveInfo> reslist = new ArrayList<>();
                HashMap<String, CommLockInfo> hashMap = new HashMap<>();
                for (CommLockInfo info : dbList) {
                    hashMap.put(info.getPackageName(), info);
                }
                for (ResolveInfo info : appList) {
                    if (!hashMap.containsKey(info.activityInfo.packageName)) {
                        reslist.add(info);
                    }
                }
                try {
                    if (reslist.size() != 0)
                        mLockInfoManager.instanceCommLockInfoTable(reslist); //将剩下不同的插入数据库
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (appList.size() < dbList.size()) { //如果有卸载应用
                List<CommLockInfo> commlist = new ArrayList<>();
                HashMap<String, ResolveInfo> hashMap = new HashMap<>();
                for (ResolveInfo info : appList) {
                    hashMap.put(info.activityInfo.packageName, info);
                }
                for (CommLockInfo info : dbList) {
                    if (!hashMap.containsKey(info.getPackageName())) {
                        commlist.add(info);
                    }
                }
                //Logger.d("有应用卸载，个数是 = " + dbList.size());
                if (commlist.size() != 0)
                    mLockInfoManager.deleteCommLockInfoTable(commlist);//将多的从数据库删除
            } else {
                //Logger.d("应用没多没少，正常");
            }
        } else {
            //数据库只插入一次
            SpUtil.getInstance().putBoolean(AppConstants.LOCK_IS_INIT_DB, true);
            try {
                mLockInfoManager.instanceCommLockInfoTable(resolveInfos);    //插入数据库
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        // Log.i("onHandleIntent", "耗时 = " + (System.currentTimeMillis() - time));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLockInfoManager = null;
    }

    /**
     * 初始化推荐加锁的应用
     */
    public void initFavoriteApps() {
        List<String> packageList = new ArrayList<>();
        List<FaviterInfo> faviterInfos = new ArrayList<>();
        packageList.add("com.android.gallery3d");       //相册
        packageList.add("com.android.mms");             //短信
        packageList.add("com.tencent.mm");              //微信
        packageList.add("com.android.contacts");        //联系人和电话
        packageList.add("com.facebook.katana");         //facebook
        packageList.add("com.facebook.orca");           //facebook Messenger
        packageList.add("com.mediatek.filemanager");    //文件管理器
        packageList.add("com.sec.android.gallery3d");   //也是个相册
        packageList.add("com.android.email");           //邮箱
        packageList.add("com.sec.android.app.myfiles"); //三星的文件
        packageList.add("com.android.vending");         //应用商店
        packageList.add("com.google.android.youtube");  //youtube
        packageList.add("com.tencent.mobileqq");        //qq
        packageList.add("com.tencent.qq");              //qq
        packageList.add("com.android.dialer");          //拨号
        packageList.add("com.twitter.android");         //twitter
        for (String packageName : packageList) {
            FaviterInfo info = new FaviterInfo();
            info.setPackageName(packageName);
            faviterInfos.add(info);
        }

        DataSupport.deleteAll(FaviterInfo.class);
        DataSupport.saveAll(faviterInfos);
    }
}
