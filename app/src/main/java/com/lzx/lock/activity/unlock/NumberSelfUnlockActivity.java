package com.lzx.lock.activity.unlock;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.activity.LockMainActivity;
import com.lzx.lock.activity.LockSettingActivity;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.db.CommLockInfoManager;
import com.lzx.lock.mvp.contract.NumberUnLockContract;
import com.lzx.lock.mvp.p.NumberUnLockPresenter;
import com.lzx.lock.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class NumberSelfUnlockActivity extends BaseActivity implements View.OnClickListener, NumberUnLockContract.View {

    private TextView mLockTitle, mLockTip;
    private ImageView mNumPoint_1, mNumPoint_2, mNumPoint_3, mNumPoint_4;
    private TextView mNumber_0, mNumber_1, mNumber_2, mNumber_3, mNumber_4, mNumber_5, mNumber_6, mNumber_7, mNumber_8, mNumber_9;
    private ImageView mNumberDel;

    private static final int COUNT = 4; //4个点
    private List<String> numInput; //存储输入数字的列表
    private List<ImageView> pointList;
    private String lockPwd = "";//密码
    private String pkgName; //解锁应用的包名
    private String actionFrom;//按返回键的操作
    private NumberUnLockPresenter mPresenter;
    private Handler mHandler = new Handler();
    private TextView[] numberArray;
    private CommLockInfoManager mManager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_num_self_unlock;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mLockTitle = (TextView) findViewById(R.id.lock_title);
        mLockTip = (TextView) findViewById(R.id.lock_tip);
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
    }

    @Override
    protected void initData() {
        numberArray = new TextView[]{mNumber_0, mNumber_1, mNumber_2, mNumber_3, mNumber_4, mNumber_5, mNumber_6, mNumber_7, mNumber_8, mNumber_9};
        for (TextView textView : numberArray) {
            textView.setOnClickListener(this);
            textView.setTextColor(Color.parseColor("#595959"));
            textView.setBackgroundResource(R.drawable.bg_num_cycle_gray);
        }
        mNumberDel.setImageResource(R.drawable.number_del_gray);
        initNumLayout();
    }

    @Override
    protected void initAction() {
        mNumberDel.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initNumLayout() {
        //获取解锁应用的包名
        pkgName = getIntent().getStringExtra(Constants.LOCK_PACKAGE_NAME);
        //获取按返回键的操作
        actionFrom = getIntent().getStringExtra(Constants.LOCK_FROM);
        //获取密码
        lockPwd = SpUtil.getInstance().getString(Constants.LOCK_PWD, "");
        mPresenter = new NumberUnLockPresenter(this);
        mManager = new CommLockInfoManager(this);
        numInput = new ArrayList<>();
        pointList = new ArrayList<>(COUNT);
        pointList.add(mNumPoint_1);
        pointList.add(mNumPoint_2);
        pointList.add(mNumPoint_3);
        pointList.add(mNumPoint_4);
        for (ImageView iv : pointList) {
            iv.setImageResource(R.drawable.ic_lock_pin_2);
        }
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
        }
    }

    private void clickNumber(TextView textView) {
        mPresenter.clickNumber(numInput, pointList, textView.getText().toString(), lockPwd);
    }

    private void deleteNumber() {
        if (numInput.size() == 0) {
            return;
        }
        pointList.get(numInput.size() - 1).setImageResource(R.drawable.ic_lock_pin_2);
        numInput.remove(numInput.size() - 1);
    }

    @Override
    public void unLockSuccess() {
        if (actionFrom.equals(Constants.LOCK_FROM_LOCK_MAIN_ACITVITY)) {
            Intent intent = new Intent(NumberSelfUnlockActivity.this, LockMainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else if (actionFrom.equals(Constants.LOCK_FROM_FINISH)) {
            mManager.unlockCommApplication(pkgName);
            finish();
        } else if (actionFrom.equals(Constants.LOCK_FROM_UNLOCK)) {
            mManager.setIsUnLockThisApp(pkgName, true);
            mManager.unlockCommApplication(pkgName);
            sendBroadcast(new Intent(GestureUnlockActivity.FINISH_UNLOCK_THIS_APP));
            finish();
        } else if (actionFrom.equals(Constants.LOCK_FROM_SETTING)) {
            startActivity(new Intent(NumberSelfUnlockActivity.this, LockSettingActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    public void unLockError(int retryNum) {
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
                    iv.setImageResource(R.drawable.ic_lock_pin_2);
                }
            }
        }, 1000);
    }

    @Override
    public void setNumberPointImageResource(List<String> numInput) {
        int index = 0;
        for (ImageView iv : pointList) {
            if (index++ < numInput.size()) {
                iv.setImageResource(R.drawable.ic_lock_pin);
            } else {
                iv.setImageResource(R.drawable.ic_lock_pin_2);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }


}
