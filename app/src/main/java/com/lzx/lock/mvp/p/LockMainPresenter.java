package com.lzx.lock.mvp.p;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.lzx.lock.base.Constants;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.db.CommLockInfoManager;
import com.lzx.lock.mvp.contract.LockMainContract;
import com.lzx.lock.utils.SpUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class LockMainPresenter implements LockMainContract.Presenter {

    private LockMainContract.View mView;
    private PackageManager mPackageManager;
    private CommLockInfoManager mLockInfoManager;
    private Context mContext;
    private LoadAppInfo mLoadAppInfo;
    private LoadLockAsyncTask mLoadLockAsyncTask;

    public LockMainPresenter(LockMainContract.View view, Context mContext) {
        mView = view;
        this.mContext = mContext;
        mPackageManager = mContext.getPackageManager();
        mLockInfoManager = new CommLockInfoManager(mContext);
    }

    /**
     * 加载所有app
     *
     * @param context
     * @param isSort
     */
    @Override
    public void loadAppInfo(Context context, boolean isSort) {
        mLoadAppInfo = new LoadAppInfo(isSort);
        mLoadAppInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 加载上锁的app
     *
     * @param context
     */
    @Override
    public void loadLockAppInfo(Context context) {
        mLoadLockAsyncTask = new LoadLockAsyncTask();
        mLoadLockAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onDestroy() {
        if (mLoadAppInfo != null && mLoadAppInfo.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadAppInfo.cancel(true);
        }
        if (mLoadLockAsyncTask != null && mLoadLockAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadLockAsyncTask.cancel(true);
        }
    }

    private class LoadAppInfo extends AsyncTask<Void, String, List<CommLockInfo>> {

        private boolean isSort = false;

        public LoadAppInfo(boolean isSort) {
            this.isSort = isSort;
        }

        @Override
        protected List<CommLockInfo> doInBackground(Void... params) {
            List<CommLockInfo> commLockInfos = mLockInfoManager.getAllCommLockInfos();
            Iterator<CommLockInfo> infoIterator = commLockInfos.iterator();
            int favoriteNum = 0;
            int sysAppNum = 0;
            int userAppNum = 0;

            while (infoIterator.hasNext()) {
                CommLockInfo info = infoIterator.next();
                try {
                    ApplicationInfo appInfo = mPackageManager.getApplicationInfo(info.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
                    if (appInfo == null || mPackageManager.getApplicationIcon(appInfo) == null) {
                        infoIterator.remove(); //将有错的app移除
                        continue;
                    } else {
                        info.setAppInfo(appInfo); //给列表ApplicationInfo赋值
                        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) { //判断是否是系统应用 ApplicationInfo#isSystemApp()
                            info.setSysApp(true);
                            info.setTopTitle("系统应用");
                        } else {
                            info.setSysApp(false);
                            info.setTopTitle("用户应用");
                        }
                    }
                    //获取推荐应用总数
                    if (info.isLocked()) {
                        favoriteNum++;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    infoIterator.remove();
                }
            }
            SpUtil.getInstance().putInt(Constants.LOCK_FAVITER_NUM, favoriteNum);

            if (isSort) {
                List<CommLockInfo> sysList = new ArrayList<>();
                List<CommLockInfo> userList = new ArrayList<>();
                for (CommLockInfo info : commLockInfos) {
                    if (info.isSysApp()) {
                        sysList.add(info);
                        sysAppNum++;
                    } else {
                        userList.add(info);
                        userAppNum++;
                    }
                }
                SpUtil.getInstance().putInt(Constants.LOCK_SYS_APP_NUM, sysAppNum);
                SpUtil.getInstance().putInt(Constants.LOCK_USER_APP_NUM, userAppNum);
                commLockInfos.clear();
                commLockInfos.addAll(sysList);
                commLockInfos.addAll(userList);
            }
            return commLockInfos;
        }

        @Override
        protected void onPostExecute(List<CommLockInfo> commLockInfos) {
            super.onPostExecute(commLockInfos);
            mView.loadAppInfoSuccess(commLockInfos);
        }
    }

    private class LoadLockAsyncTask extends AsyncTask<String, Void, List<CommLockInfo>> {

        @Override
        protected List<CommLockInfo> doInBackground(String... params) {
            List<CommLockInfo> commLockInfos = mLockInfoManager.getAllCommLockInfos();
            Iterator<CommLockInfo> infoIterator = commLockInfos.iterator();

            while (infoIterator.hasNext()) {
                CommLockInfo info = infoIterator.next();
                try {
                    ApplicationInfo appInfo = mPackageManager.getApplicationInfo(info.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
                    if (appInfo == null || mPackageManager.getApplicationIcon(appInfo) == null) {
                        infoIterator.remove(); //将有错的app移除
                        continue;
                    } else {
                        info.setAppInfo(appInfo); //给列表ApplicationInfo赋值
                        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) { //判断是否是系统应用 ApplicationInfo#isSystemApp()
                            info.setSysApp(true);
                            info.setTopTitle("系统应用");
                        } else {
                            info.setSysApp(false);
                            info.setTopTitle("用户应用");
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    infoIterator.remove();
                }
            }

            List<CommLockInfo> list = new ArrayList<>();
            for (CommLockInfo info : commLockInfos) {
                if (info.isLocked()) {
                    list.add(info);
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<CommLockInfo> commLockInfos) {
            super.onPostExecute(commLockInfos);
            mView.loadAppInfoSuccess(commLockInfos);
        }

    }

}
