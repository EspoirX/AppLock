package com.lzx.lock.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.lzx.lock.R;
import com.lzx.lock.activity.unlock.GestureSelfUnlockActivity;
import com.lzx.lock.activity.unlock.NumberSelfUnlockActivity;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.service.LoadAppListService;
import com.lzx.lock.service.LockService;
import com.lzx.lock.utils.AppUtils;
import com.lzx.lock.utils.SpUtil;

/**
 * Created by xian on 2017/2/17.
 */

public class SplashActivity extends BaseActivity {

    private ImageView mImgSplash;
    private ObjectAnimator animator;

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        AppUtils.hideStatusBar(getWindow(), true);
        mImgSplash = (ImageView) findViewById(R.id.img_splash);
    }

    @Override
    protected void initData() {
        startService(new Intent(this, LoadAppListService.class));
        if (SpUtil.getInstance().getBoolean(Constants.LOCK_STATE, false)) {
            startService(new Intent(this, LockService.class));
        }

        animator = ObjectAnimator.ofFloat(mImgSplash, "alpha", 0.5f, 1);
        animator.setDuration(1500);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                boolean isFirstLock = SpUtil.getInstance().getBoolean(Constants.LOCK_IS_FIRST_LOCK, true);
                int lockType = SpUtil.getInstance().getInt(Constants.LOCK_TYPE);
                Intent intent;
                if (isFirstLock) { //如果第一次
                    intent = new Intent(SplashActivity.this, FirstMainActivity.class);
                } else {
                    //判断是什么类型的锁屏
                    if (lockType == 0) { //图形
                        intent = new Intent(SplashActivity.this, GestureSelfUnlockActivity.class);
                    } else { //数字
                        intent = new Intent(SplashActivity.this, NumberSelfUnlockActivity.class);
                    }
                }
                intent.putExtra(Constants.LOCK_PACKAGE_NAME, Constants.APP_PACKAGE_NAME); //传自己的包名
                intent.putExtra(Constants.LOCK_FROM, Constants.LOCK_FROM_LOCK_MAIN_ACITVITY);
                startActivity(intent);

                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    protected void initAction() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animator = null;
    }
}
