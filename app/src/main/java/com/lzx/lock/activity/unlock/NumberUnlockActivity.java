package com.lzx.lock.activity.unlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.activity.LockMainActivity;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.db.CommLockInfoManager;
import com.lzx.lock.mvp.contract.NumberUnLockContract;
import com.lzx.lock.mvp.p.NumberUnLockPresenter;
import com.lzx.lock.service.LockService;
import com.lzx.lock.utils.LockUtil;
import com.lzx.lock.utils.SpUtil;
import com.lzx.lock.utils.StatusBarUtil;
import com.lzx.lock.widget.UnLockMenuPopWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class NumberUnlockActivity extends BaseActivity implements View.OnClickListener,NumberUnLockContract.View{

    private ImageView mIconMore, mAppLogo;
    private RelativeLayout mUnLockLayout;
    private TextView mLockTip, mUnLockText, mAppLabel;
    private ImageView mNumPoint_1, mNumPoint_2, mNumPoint_3, mNumPoint_4;
    private TextView mNumber_0, mNumber_1, mNumber_2, mNumber_3, mNumber_4, mNumber_5, mNumber_6, mNumber_7, mNumber_8, mNumber_9;
    private ImageView mNumberDel;
    private ImageView mUnLockIcon;

    private static final int COUNT = 4; //4个点
    private List<String> numInput; //存储输入数字的列表
    private List<ImageView> pointList;
    private String lockPwd = "";//密码
    private String pkgName; //解锁应用的包名
    private String actionFrom;//按返回键的操作
    private Handler mHandler = new Handler();
    private PackageManager packageManager;
    private CommLockInfoManager mLockInfoManager;
    private UnLockMenuPopWindow mPopWindow;
    private NumberUnLockPresenter mPresenter;
    private NumberUnlockReceiver mNumberUnlockReceiver;
    private Drawable iconDrawable;
    private String appLabel;

    @Override
    public int getLayoutId() {
        return R.layout.activity_num_unlock;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        StatusBarUtil.setTransparent(this); //状态栏透明

        mIconMore = (ImageView) findViewById(R.id.btn_more);
        mUnLockLayout = (RelativeLayout) findViewById(R.id.unlock_layout);
        mLockTip = (TextView) findViewById(R.id.tv_lock_tip);
        mUnLockText = (TextView) findViewById(R.id.unlock_text);
        mNumPoint_1 = (ImageView) findViewById(R.id.num_point_1);
        mNumPoint_2 = (ImageView) findViewById(R.id.num_point_2);
        mNumPoint_3 = (ImageView) findViewById(R.id.num_point_3);
        mNumPoint_4 = (ImageView) findViewById(R.id.num_point_4);
        mNumber_0 = (TextView) findViewById(R.id.number_0);
        mNumber_1 = (TextView) findViewById(R.id.number_1);
        mNumber_2 = (TextView) findViewById(R.id.number_2);
        mNumber_3 = (TextView) findViewById(R.id.number_3);
        mNumber_4 = (TextView) findViewById(R.id.number_4);
        mNumber_5 = (TextView) findViewById(R.id.number_5);
        mNumber_6 = (TextView) findViewById(R.id.number_6);
        mNumber_7 = (TextView) findViewById(R.id.number_7);
        mNumber_8 = (TextView) findViewById(R.id.number_8);
        mNumber_9 = (TextView) findViewById(R.id.number_9);
        mNumberDel = (ImageView) findViewById(R.id.number_del);
        mUnLockIcon = (ImageView) findViewById(R.id.unlock_icon);
        mAppLogo = (ImageView) findViewById(R.id.app_logo);
        mAppLabel = (TextView) findViewById(R.id.app_label);
    }

    @Override
    protected void initData() {
        //获取解锁应用的包名
        pkgName = getIntent().getStringExtra(Constants.LOCK_PACKAGE_NAME);
        //获取按返回键的操作
        actionFrom = getIntent().getStringExtra(Constants.LOCK_FROM);
        //获取密码
        lockPwd = SpUtil.getInstance().getString(Constants.LOCK_PWD, "");
        packageManager = getPackageManager();
        mLockInfoManager = new CommLockInfoManager(this);
        mPopWindow = new UnLockMenuPopWindow(this, pkgName, false);
        mPresenter = new NumberUnLockPresenter(this);
        numInput = new ArrayList<>();
        pointList = new ArrayList<>(COUNT);

        IntentFilter filter = new IntentFilter();
        filter.addAction(GestureUnlockActivity.FINISH_UNLOCK_THIS_APP);
        mNumberUnlockReceiver = new NumberUnlockReceiver();
        registerReceiver(mNumberUnlockReceiver, filter);

        pointList.add(mNumPoint_1);
        pointList.add(mNumPoint_2);
        pointList.add(mNumPoint_3);
        pointList.add(mNumPoint_4);
        for (ImageView iv : pointList) {
            iv.setImageResource(R.drawable.num_point);
        }
        initImageRes();
    }

    /**
     * 给应用Icon和背景赋值
     */
    private void initImageRes() {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (appInfo != null) {
                iconDrawable = packageManager.getApplicationIcon(appInfo);
                appLabel = packageManager.getApplicationLabel(appInfo).toString();
                mUnLockIcon.setImageDrawable(iconDrawable);
                mUnLockText.setText(appLabel);
                mLockTip.setText(getString(R.string.num_create_text_01));
                final Drawable icon = packageManager.getApplicationIcon(appInfo);
                mUnLockLayout.setBackgroundDrawable(icon);
                mUnLockLayout.getViewTreeObserver().addOnPreDrawListener(
                        new ViewTreeObserver.OnPreDrawListener() {

                            @Override
                            public boolean onPreDraw() {
                                mUnLockLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                                mUnLockLayout.buildDrawingCache();
                                Bitmap bmp = LockUtil.drawableToBitmap(icon, mUnLockLayout);
                                LockUtil.blur(NumberUnlockActivity.this, LockUtil.big(bmp), mUnLockLayout);
                                return true;
                            }
                        });
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initAction() {
        mNumber_0.setOnClickListener(this);
        mNumber_1.setOnClickListener(this);
        mNumber_2.setOnClickListener(this);
        mNumber_3.setOnClickListener(this);
        mNumber_4.setOnClickListener(this);
        mNumber_5.setOnClickListener(this);
        mNumber_6.setOnClickListener(this);
        mNumber_7.setOnClickListener(this);
        mNumber_8.setOnClickListener(this);
        mNumber_9.setOnClickListener(this);
        mNumberDel.setOnClickListener(this);
        mIconMore.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.number_0:
            case R.id.number_1:
            case R.id.number_2:
            case R.id.number_3:
            case R.id.number_4:
            case R.id.number_5:
            case R.id.number_6:
            case R.id.number_7:
            case R.id.number_8:
            case R.id.number_9:
                clickNumber((TextView) view);
                break;
            case R.id.number_del:
                deleteNumber();
                break;
            case R.id.btn_more:
                mPopWindow.showAsDropDown(mIconMore);
                break;
        }
    }

    private void clickNumber(TextView btn) {
        mPresenter.clickNumber(numInput, pointList, btn.getText().toString(), lockPwd);
    }

    private void deleteNumber() {
        if (numInput.size() == 0) {
            return;
        }
        pointList.get(numInput.size() - 1).setImageResource(R.drawable.num_point);
        numInput.remove(numInput.size() - 1);
    }

    @Override
    public void onBackPressed() {
        if (actionFrom.equals(Constants.LOCK_FROM_FINISH)) {
            LockUtil.goHome(this);
        } else if (actionFrom.equals(Constants.LOCK_FROM_LOCK_MAIN_ACITVITY)) {
            finish();
        } else {
            startActivity(new Intent(this, LockMainActivity.class));
        }
    }

    @Override
    public void unLockSuccess() {
        if (actionFrom.equals(Constants.LOCK_FROM_LOCK_MAIN_ACITVITY)) {
            startActivity(new Intent(NumberUnlockActivity.this, LockMainActivity.class));
        } else {
            SpUtil.getInstance().putString(Constants.LOCK_LAST_LOAD_PKG_NAME, pkgName);//记录解锁包名
            SpUtil.getInstance().putLong(Constants.LOCK_CURR_MILLISENCONS, System.currentTimeMillis()); //记录解锁时间

            Intent intent = new Intent(LockService.UNLOCK_ACTION);
            intent.putExtra(LockService.LOCK_SERVICE_LASTTIME, System.currentTimeMillis());
            intent.putExtra(LockService.LOCK_SERVICE_LASTAPP, pkgName);
            sendBroadcast(intent);

            mLockInfoManager.unlockCommApplication(pkgName);
        }
        finish();
    }

    @Override
    public void unLockError(int retryNum) {
        clearPassword();
        for (ImageView iv : pointList) {
            iv.setImageResource(R.drawable.number_point_error);
        }
    }

    @Override
    public void clearPassword() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (ImageView iv : pointList) {
                    iv.setImageResource(R.drawable.num_point);
                }
            }
        }, 2000);
    }

    @Override
    public void setNumberPointImageResource(List<String> numInput) {
        int index = 0;
        for (ImageView iv : pointList) {
            if (index++ < numInput.size()) {
                iv.setImageResource(R.drawable.num_point_check);
            } else {
                iv.setImageResource(R.drawable.num_point);
            }
        }
    }

    private class NumberUnlockReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GestureUnlockActivity.FINISH_UNLOCK_THIS_APP)) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNumberUnlockReceiver);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }


}
