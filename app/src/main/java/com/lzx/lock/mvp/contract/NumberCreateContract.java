package com.lzx.lock.mvp.contract;

import android.widget.ImageView;

import com.lzx.lock.base.BasePresenter;
import com.lzx.lock.base.BaseView;

import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public interface NumberCreateContract {
    interface View extends BaseView<MainContract.Presenter> {
        void setNumberPointImageResource(ImageView iv, int resId); //设置点的图片

        void updateLockTipString(int resId, boolean isToast);//更新提示信息

        void createLockSuccess(); //创建密码成功

        void completedFirstTime(); //输入第一次后
    }

    interface Presenter extends BasePresenter {

        void clickNumber(List<String> numInput, List<ImageView> pointList, String number);

        void onDestroy();
    }
}
