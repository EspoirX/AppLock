package com.lzx.lock.mvp.contract;

import android.content.Context;

import com.lzx.lock.base.BasePresenter;
import com.lzx.lock.base.BaseView;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.mvp.p.LockMainPresenter;

import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public interface LockMainContract {
    interface View extends BaseView<Presenter> {

        void loadAppInfoSuccess(List<CommLockInfo> list);
    }

    interface Presenter extends BasePresenter {
        void loadAppInfo(Context context);

        void searchAppInfo(String search, LockMainPresenter.ISearchResultListener listener);

        void onDestroy();
    }
}
