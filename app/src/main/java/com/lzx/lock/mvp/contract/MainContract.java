package com.lzx.lock.mvp.contract;

import android.content.Context;

import com.lzx.lock.base.BasePresenter;
import com.lzx.lock.base.BaseView;
import com.lzx.lock.bean.CommLockInfo;

import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public interface MainContract {
    interface View extends BaseView<Presenter> {
        void loadAppInfoSuccess(List<CommLockInfo> list);
    }

    interface Presenter extends BasePresenter {
        void loadAppInfo(Context context, boolean isSort);

        void loadLockAppInfo(Context context);

        void onDestroy();
    }
}
