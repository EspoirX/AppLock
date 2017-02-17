package com.lzx.lock.mvp.contract;

import android.widget.ImageView;

import com.lzx.lock.base.BasePresenter;
import com.lzx.lock.base.BaseView;

import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public interface NumberUnLockContract {
    interface View extends BaseView<Presenter> {
        void unLockSuccess();
        void unLockError(int retryNum);
        void clearPassword();
        void setNumberPointImageResource(List<String> numInput); //设置点的图片
    }

    interface Presenter extends BasePresenter {

        void clickNumber(List<String> numInput, List<ImageView> pointList, String number, String lockPwd);

        void onDestroy();
    }
}
