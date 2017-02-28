package com.lzx.lock.module.pwd;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.activity.FirstMainActivity;
import com.lzx.lock.activity.LockMainActivity;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.bean.LockStage;
import com.lzx.lock.db.CommLockInfoManager;
import com.lzx.lock.mvp.contract.GestureCreateContract;
import com.lzx.lock.mvp.p.GestureCreatePresenter;
import com.lzx.lock.service.LockService;
import com.lzx.lock.utils.LockPatternUtils;
import com.lzx.lock.utils.LockUtil;
import com.lzx.lock.utils.SpUtil;
import com.lzx.lock.utils.ToastUtil;
import com.lzx.lock.widget.DialogPermission;
import com.lzx.lock.widget.LockPatternView;
import com.lzx.lock.widget.LockPatternViewPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class CreatePwdActivity extends BaseActivity implements View.OnClickListener,
        GestureCreateContract.View  {

    private ArrayList<CommLockInfo> mLockList; //保存的加锁应用
    private ArrayList<CommLockInfo> mUnLockList; //保存的没加锁应用

    private TextView   mLockTip;
   // private View mLineOneToTwo, mLineTwoToThree;
    private LockPatternView mLockPatternView;
    private TextView mBtnDone;

    private Bundle savedInstanceState;



    //图案锁相关
    private LockStage mUiStage = LockStage.Introduction;
    public static final int ID_EMPTY_MESSAGE = -1;
    protected List<LockPatternView.Cell> mChosenPattern = null; //密码
    private static final String KEY_PATTERN_CHOICE = "chosenPattern";
    private static final String KEY_UI_STAGE = "uiStage";
    private final List<LockPatternView.Cell> mAnimatePattern = new ArrayList<>();
    private LockPatternUtils mLockPatternUtils;
    private LockPatternViewPattern mPatternViewPattern;
    private GestureCreatePresenter mGestureCreatePresenter;



    private int RESULT_ACTION_USAGE_ACCESS_SETTINGS = 1;
    private int RESULT_ACTION_NOTIFICATION_LISTENER_SETTINGS = 2;
    private CommLockInfoManager mLockInfoManager;



    @Override
    public int getLayoutId() {
        return R.layout.activity_create_pwd;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        mLockPatternView = (LockPatternView) findViewById(R.id.lock_pattern_view);
        mLockTip = (TextView) findViewById(R.id.lock_tip);
        mBtnDone = (TextView) findViewById(R.id.btn_done);
    }

    @Override
    protected void initData() {
        mLockList = getIntent().getParcelableArrayListExtra("lock_list");
        mUnLockList = getIntent().getParcelableArrayListExtra("unlock_list");
        mLockInfoManager = new CommLockInfoManager(this);
        mGestureCreatePresenter = new GestureCreatePresenter(this, this);
        initLockPatternView();
        if (savedInstanceState == null) {
            mGestureCreatePresenter.updateStage(LockStage.Introduction);
        } else {
            final String patternString = savedInstanceState.getString(KEY_PATTERN_CHOICE);
            if (patternString != null) {
                mChosenPattern = LockPatternUtils.stringToPattern(patternString);
            }
            mGestureCreatePresenter.updateStage(LockStage.values()[savedInstanceState.getInt(KEY_UI_STAGE)]);
        }
    }

    /**
     * 初始化锁屏控件
     */
    private void initLockPatternView() {
        mLockPatternView.setLineColorRight(0x66ffffff);
        mLockPatternUtils = new LockPatternUtils(this);
        mPatternViewPattern = new LockPatternViewPattern(mLockPatternView);
        mPatternViewPattern.setPatternListener(new LockPatternViewPattern.onPatternListener() {
            @Override
            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                mGestureCreatePresenter.onPatternDetected(pattern, mChosenPattern, mUiStage);
            }
        });
        mLockPatternView.setOnPatternListener(mPatternViewPattern);
        mLockPatternView.setTactileFeedbackEnabled(true);
    }

    @Override
    protected void initAction() {
        mBtnDone.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_done:
                actionDown();
                break;
        }
    }

    private DialogPermission dialog;

    private void actionDown() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { //如果大于21
            if (!LockUtil.isStatAccessPermissionSet(CreatePwdActivity.this)) { //如果没权限
                if (LockUtil.isNoOption(CreatePwdActivity.this)) { //如果有设置界面
                    showDialog();
                } else {
                    gotoLockMainActivity(); //没设置界面 直接转跳
                }
            } else {
                gotoLockMainActivity(); //有权限直接转跳
            }
        } else {
            gotoLockMainActivity(); //小于21直接转跳
        }
    }

    private void showDialog() {
        dialog = new DialogPermission(CreatePwdActivity.this);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACTION_USAGE_ACCESS_SETTINGS) {
            if (LockUtil.isStatAccessPermissionSet(CreatePwdActivity.this)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (!LockUtil.isNotificationSettingOn(CreatePwdActivity.this)) {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                        startActivityForResult(intent, RESULT_ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    } else {
                        gotoLockMainActivity();
                        finish();
                    }
                } else {
                    gotoLockMainActivity();
                    finish();
                }
            }
        } else if (requestCode == RESULT_ACTION_NOTIFICATION_LISTENER_SETTINGS) {
            if (LockUtil.isNotificationSettingOn(CreatePwdActivity.this)){
                gotoLockMainActivity();
                finish();
            }
        }
    }

    private void gotoPermissionActivity() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivityForResult(intent, RESULT_ACTION_USAGE_ACCESS_SETTINGS);
    }

    private void gotoLockMainActivity() {
        sendBroadcast(new Intent(FirstMainActivity.ACTION_FINISH));

        for (CommLockInfo pro : mLockList) {
            mLockInfoManager.lockCommApplication(pro.getPackageName());
        }
        for (CommLockInfo pro : mUnLockList) {
            mLockInfoManager.unlockCommApplication(pro.getPackageName());
        }
        SpUtil.getInstance().putBoolean(Constants.LOCK_STATE, true); //开启应用锁开关

        startService(new Intent(this, LockService.class));
        SpUtil.getInstance().putBoolean(Constants.LOCK_IS_FIRST_LOCK, false);
        startActivity(new Intent(this, LockMainActivity.class));
        finish();
    }




    /**
     * 更新当前锁的状态
     */
    @Override
    public void updateUiStage(LockStage stage) {
        mUiStage = stage;
    }

    /**
     * 更新当前密码
     */
    @Override
    public void updateChosenPattern(List<LockPatternView.Cell> mChosenPattern) {
        this.mChosenPattern = mChosenPattern;
    }

    /**
     * 更新提示信息
     */
    @Override
    public void updateLockTip(String text, boolean isToast) {
        if (isToast) {
            ToastUtil.showToast(text);
        } else {
            mLockTip.setText(text);
        }
    }

    /**
     * 更新提示信息
     */
    @Override
    public void setHeaderMessage(int headerMessage) {
        mLockTip.setText(headerMessage);
    }

    /**
     * LockPatternView的一些配置
     */
    @Override
    public void lockPatternViewConfiguration(boolean patternEnabled, LockPatternView.DisplayMode displayMode) {
        if (patternEnabled) {
            mLockPatternView.enableInput();
        } else {
            mLockPatternView.disableInput();
        }
        mLockPatternView.setDisplayMode(displayMode);
    }

    /**
     * 初始化
     */
    @Override
    public void Introduction() {
        clearPattern();
    }

    @Override
    public void HelpScreen() {

    }

    /**
     * 路径太短
     */
    @Override
    public void ChoiceTooShort() {
        mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);  //路径太短
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, 500);
    }

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    /**
     * 画完第一步转到第二步
     */
    @Override
    public void moveToStatusTwo() {

    }

    /**
     * 清空控件路径
     */
    @Override
    public void clearPattern() {
        mLockPatternView.clearPattern();
    }

    /**
     * 第一次和第二次画得不一样
     */
    @Override
    public void ConfirmWrong() {
        mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);  //路径太短
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, 500);
    }

    /**
     * 画成功了
     */
    @Override
    public void ChoiceConfirmed() {

        mLockPatternUtils.saveLockPattern(mChosenPattern); //保存密码
        SpUtil.getInstance().putInt(Constants.LOCK_TYPE, 0);
        mLockPatternView.setVisibility(View.GONE);

        clearPattern();
        mBtnDone.setVisibility(View.VISIBLE);
    }

    /**==========================================================================**/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGestureCreatePresenter.onDestroy();

    }
}
