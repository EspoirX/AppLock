package com.lzx.lock.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.bean.LockStage;
import com.lzx.lock.db.CommLockInfoManager;
import com.lzx.lock.mvp.contract.GestureCreateContract;
import com.lzx.lock.mvp.contract.NumberCreateContract;
import com.lzx.lock.mvp.p.GestureCreatePresenter;
import com.lzx.lock.mvp.p.NumberCreatePresenter;
import com.lzx.lock.service.LockService;
import com.lzx.lock.utils.LockPatternUtils;
import com.lzx.lock.utils.LockUtil;
import com.lzx.lock.utils.SpUtil;
import com.lzx.lock.utils.ToastUtil;
import com.lzx.lock.widget.LockPatternView;
import com.lzx.lock.widget.LockPatternViewPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class CreatePwdActivity extends BaseActivity implements View.OnClickListener,
        GestureCreateContract.View, NumberCreateContract.View {

    private ArrayList<CommLockInfo> mLockList; //保存的加锁应用
    private ArrayList<CommLockInfo> mUnLockList; //保存的没加锁应用

    private TextView mStepOne, mStepTwo, mStepThree, mLockTip;
    private View mLineOneToTwo, mLineTwoToThree;
    private LockPatternView mLockPatternView;
    private LinearLayout mNumLockLayout;
    private ImageView mNumPoint_1, mNumPoint_2, mNumPoint_3, mNumPoint_4;
    private TextView mNumber_0, mNumber_1, mNumber_2, mNumber_3, mNumber_4, mNumber_5, mNumber_6, mNumber_7, mNumber_8, mNumber_9;
    private ImageView mNumberDel;
    private TextView mSwitchLock;
    private TextView mBtnDone;

    private Bundle savedInstanceState;

    private int currLocType = 0;//当前锁类型 0 图案 1 数字

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

    //数字锁相关
    private static final int COUNT = 4; //4个点
    private List<String> numInput; //存储输入数字的列表
    private List<ImageView> pointList;
    private NumberCreateContract.Presenter mPresenter;

    private int RESULT_ACTION_USAGE_ACCESS_SETTINGS = 1;
    private CommLockInfoManager mLockInfoManager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_pwd;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        mStepOne = (TextView) findViewById(R.id.step_one);
        mStepTwo = (TextView) findViewById(R.id.step_two);
        mStepThree = (TextView) findViewById(R.id.step_three);
        mLineOneToTwo = findViewById(R.id.one_to_two);
        mLineTwoToThree = findViewById(R.id.two_to_three);
        mLockPatternView = (LockPatternView) findViewById(R.id.lock_pattern_view);
        mNumLockLayout = (LinearLayout) findViewById(R.id.num_lock_layout);
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
        mSwitchLock = (TextView) findViewById(R.id.switch_lock);
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

        // 初始化数字锁
        initNumLayout();
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

    /**
     * 初始化数据
     */
    private void initNumLayout() {
        mPresenter = new NumberCreatePresenter(this);
        numInput = new ArrayList<>();
        pointList = new ArrayList<>(COUNT);
        pointList.add(mNumPoint_1);
        pointList.add(mNumPoint_2);
        pointList.add(mNumPoint_3);
        pointList.add(mNumPoint_4);
        for (ImageView iv : pointList) {
            iv.setImageResource(R.drawable.num_point);
        }
    }

    @Override
    protected void initAction() {
        mSwitchLock.setText("数字锁");
        mSwitchLock.setOnClickListener(this);
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
        mBtnDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switch_lock:
                if (mSwitchLock.getText().toString().equals("数字锁")) {
                    currLocType = 1;
                    mLockPatternView.setVisibility(View.GONE);
                    mNumLockLayout.setVisibility(View.VISIBLE);
                    mSwitchLock.setText("图案锁");
                    mLockTip.setText(R.string.num_create_text_01);
                    SpUtil.getInstance().putInt(Constants.LOCK_TYPE, 1);
                } else if (mSwitchLock.getText().toString().equals("图案锁")) {
                    currLocType = 0;
                    mLockPatternView.setVisibility(View.VISIBLE);
                    mNumLockLayout.setVisibility(View.GONE);
                    mSwitchLock.setText("数字锁");

                    SpUtil.getInstance().putInt(Constants.LOCK_TYPE, 0);
                } else if (mSwitchLock.getText().toString().equals("重置")) {
                    //恢复到第一步
                    clearPattern();
                    setStepOne();
                    initNumLayout();
                }
                break;
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
            case R.id.btn_done:
                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { //如果大于21
                    if (!LockUtil.isStatAccessPermissionSet(CreatePwdActivity.this)) { //如果没权限
                        if (LockUtil.isNoOption(CreatePwdActivity.this)) { //如果有设置界面
                            gotoPermissionActivity(); //小于23转跳到授权界面
                        } else {
                            gotoLockMainActivity(); //没设置界面 直接转跳
                        }
                    } else {
                        gotoLockMainActivity(); //有权限直接转跳
                    }
                } else {
                    gotoLockMainActivity(); //小于21直接转跳
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACTION_USAGE_ACCESS_SETTINGS) {
            if (LockUtil.isStatAccessPermissionSet(CreatePwdActivity.this)) {
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
     * 恢复到第一步
     */
    private void setStepOne() {
        mGestureCreatePresenter.updateStage(LockStage.Introduction);
        mStepOne.setBackgroundResource(R.drawable.bg_white_round);
        mStepOne.setText("1");
        mLineOneToTwo.setBackgroundColor(ContextCompat.getColor(this, R.color.white_80));
        mStepTwo.setBackgroundResource(R.drawable.bg_white80_round);
        if (currLocType == 1) {
            mSwitchLock.setText("图案锁");
        } else {
            mSwitchLock.setText("数字锁");
        }
    }

    /**
     * 点击数字
     */
    private void clickNumber(TextView btn) {
        mPresenter.clickNumber(numInput, pointList, btn.getText().toString().trim());
    }

    /**
     * 删除按钮
     */
    private void deleteNumber() {
        if (numInput.size() == 0) {
            return;
        }
        pointList.get(numInput.size() - 1).setImageResource(R.drawable.num_point);
        numInput.remove(numInput.size() - 1);
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
        mStepOne.setText("");
        mStepOne.setBackgroundResource(R.drawable.ic_lock_first_nav_suc);
        mStepTwo.setBackgroundResource(R.drawable.bg_white_round);
        mLineOneToTwo.setBackgroundColor(Color.WHITE);
        mSwitchLock.setText("重置");
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
        mStepTwo.setText("");
        mStepOne.setBackgroundResource(R.drawable.ic_lock_first_nav_suc);
        mStepTwo.setBackgroundResource(R.drawable.ic_lock_first_nav_suc);
        mStepThree.setBackgroundResource(R.drawable.bg_white_round);
        mLineOneToTwo.setBackgroundColor(Color.WHITE);
        mLineTwoToThree.setBackgroundColor(Color.WHITE);
        mLockPatternUtils.saveLockPattern(mChosenPattern); //保存密码
        SpUtil.getInstance().putInt(Constants.LOCK_TYPE, 0);
        mLockPatternView.setVisibility(View.GONE);
        mSwitchLock.setVisibility(View.GONE);
        clearPattern();
        mBtnDone.setVisibility(View.VISIBLE);
    }

    /**==========================================================================**/

    /**
     * 设置密码
     */
    @Override
    public void setNumberPointImageResource(ImageView iv, int resId) {
        iv.setImageResource(resId);
    }

    /**
     * 更新提示
     */
    @Override
    public void updateLockTipString(int resId, boolean isToast) {
        if (isToast) {
            ToastUtil.showToast(getString(resId));
        } else {
            mLockTip.setText(resId);
        }
        mLockTip.postDelayed(showImageRunnable, 500);
        if (resId == R.string.num_create_text_03) {

        }
    }

    private Runnable showImageRunnable = new Runnable() {
        @Override
        public void run() {
            for (ImageView iv : pointList) {
                iv.setImageResource(R.drawable.num_point);
            }
        }
    };

    /**
     * 成功了
     */
    @Override
    public void createLockSuccess() {
        mStepTwo.setText("");
        mStepOne.setBackgroundResource(R.drawable.ic_lock_first_nav_suc);
        mStepTwo.setBackgroundResource(R.drawable.ic_lock_first_nav_suc);
        mStepThree.setBackgroundResource(R.drawable.bg_white_round);
        mLineOneToTwo.setBackgroundColor(Color.WHITE);
        mLineTwoToThree.setBackgroundColor(Color.WHITE);

        SpUtil.getInstance().putInt(Constants.LOCK_TYPE, 1);

        mNumLockLayout.setVisibility(View.GONE);
        mLockPatternView.setVisibility(View.GONE);
        mSwitchLock.setVisibility(View.INVISIBLE);
        mSwitchLock.setVisibility(View.GONE);

        mBtnDone.setVisibility(View.VISIBLE);
    }

    /**
     * 第一步到第二步
     */
    @Override
    public void completedFirstTime() {
        moveToStatusTwo();
        mLockTip.postDelayed(showImageRunnable, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGestureCreatePresenter.onDestroy();
        mPresenter.onDestroy();
    }
}
