package com.lzx.lock.module.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.activity.AboutMeActivity;
import com.lzx.lock.activity.lock.GestureCreateActivity;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.bean.LockAutoTime;
import com.lzx.lock.utils.SpUtil;
import com.lzx.lock.utils.SystemBarHelper;
import com.lzx.lock.utils.ToastUtil;
import com.lzx.lock.widget.SelectLockTimeDialog;


/**
 * Created by xian on 2017/2/17.
 */

public class LockSettingActivity extends BaseActivity implements View.OnClickListener
    /*    , CompoundButton.OnCheckedChangeListener,*/
        , DialogInterface.OnDismissListener {

    //    private TextView mCbLockTypeNum, mCbLockTypePattern;
//    private TextView mBtnChangePwd, mBtnSelectTime, mLockTime;
//    private SwitchCompat mSwitchAllowExit, mSwitchRelockScreen;
    private TextView mBtnAbout, mLockTime, mBtnChangePwd, mIsShowPath;
    private CheckBox mLockSwitch;
    private RelativeLayout mLockWhen;

    //    private Drawable drawableSelect;
//    private Drawable drawableNormal;
//    private String currLockType; //当前的锁屏模式
//    private String TYPE_GESTURE = "type_Gesture";
//    private String TYPE_NUMBER = "type_Number";
//
    private LockSettingReceiver mLockSettingReceiver;
    public static final String ON_ITEM_CLICK_ACTION = "on_item_click_action";
    //    public static final String FINISH_ACTION = "finish_action";
    private SelectLockTimeDialog dialog;
    //
//    private Intent intent;
//    private static final int REQUEST_GESTRUE_CREATE = 0;
//    private static final int REQUEST_NUMBER_CREATE = 1;
//    private static final int REQUEST_CHANGE_EMAIL = 2;
    private static final int REQUEST_CHANGE_PWD = 3;
    private RelativeLayout mTopLayout;

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
//        mCbLockTypeNum = (TextView) findViewById(R.id.lock_type_num);
//        mCbLockTypePattern = (TextView) findViewById(R.id.lock_type_pattern);
        mBtnChangePwd = (TextView) findViewById(R.id.btn_change_pwd);
//        mBtnSelectTime = (TextView) findViewById(R.id.btn_select_time);
        mLockTime = (TextView) findViewById(R.id.lock_time);
//        mSwitchAllowExit = (SwitchCompat) findViewById(R.id.switch_allow_exit);
//        mSwitchRelockScreen = (SwitchCompat) findViewById(R.id.switch_relock_screen);
        mBtnAbout = (TextView) findViewById(R.id.about_me);
        mLockSwitch = (CheckBox) findViewById(R.id.switch_compat);
        mLockWhen = (RelativeLayout) findViewById(R.id.lock_when);
        mIsShowPath = (TextView) findViewById(R.id.is_show_path);

        mTopLayout = (RelativeLayout) findViewById(R.id.top_layout);
        mTopLayout.setPadding(0, SystemBarHelper.getStatusBarHeight(this), 0, 0);
    }

    @Override
    protected void initData() {
        mLockSettingReceiver = new LockSettingReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ON_ITEM_CLICK_ACTION);
//        filter.addAction(FINISH_ACTION);
        registerReceiver(mLockSettingReceiver, filter);
//
        dialog = new SelectLockTimeDialog(this, "");
        dialog.setOnDismissListener(this);
//
//        int lockType = SpUtil.getInstance().getInt(Constants.LOCK_TYPE);
//        drawableSelect = getResources().getDrawable(R.drawable.lock_select);
//        drawableNormal = getResources().getDrawable(R.drawable.lock_unselect);
//        drawableSelect.setBounds(0, 0, drawableSelect.getMinimumWidth(), drawableSelect.getMinimumHeight());
//        drawableNormal.setBounds(0, 0, drawableNormal.getMinimumWidth(), drawableNormal.getMinimumHeight());
//        mCbLockTypePattern.setCompoundDrawables(null, null, lockType == 0 ? drawableSelect : drawableNormal, null);
//        mCbLockTypeNum.setCompoundDrawables(null, null, lockType != 0 ? drawableSelect : drawableNormal, null);
//        currLockType = lockType == 0 ? TYPE_GESTURE : TYPE_NUMBER;
//
//        boolean isLockAutoScreenTime = SpUtil.getInstance().getBoolean(Constants.LOCK_AUTO_SCREEN_TIME, false);
//        boolean isLockAutoScreen = SpUtil.getInstance().getBoolean(Constants.LOCK_AUTO_SCREEN, false);
//        if (isLockAutoScreenTime) {
//            mSwitchAllowExit.setChecked(true);
//        }
//        if (isLockAutoScreen) {
//            mSwitchRelockScreen.setChecked(true);
//        }
//        String apartTitle = SpUtil.getInstance().getString(Constants.LOCK_APART_TITLE, "");
//        mLockTime.setText(apartTitle);

        boolean isLockOpen = SpUtil.getInstance().getBoolean(Constants.LOCK_STATE);
        mLockSwitch.setChecked(isLockOpen);
    }

    @Override
    protected void initAction() {
//        mCbLockTypeNum.setOnClickListener(this);
//        mCbLockTypePattern.setOnClickListener(this);
//
        mBtnChangePwd.setOnClickListener(this);
//        mBtnSelectTime.setOnClickListener(this);
//        mLockTime.setOnClickListener(this);
//        mSwitchAllowExit.setOnCheckedChangeListener(this);
//        mSwitchRelockScreen.setOnCheckedChangeListener(this);

        mBtnAbout.setOnClickListener(this);

        mLockWhen.setOnClickListener(this);
        mIsShowPath.setOnClickListener(this);
        mLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SpUtil.getInstance().putBoolean(Constants.LOCK_STATE, b);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_change_pwd:
                Intent intent = new Intent(LockSettingActivity.this, GestureCreateActivity.class);
                startActivityForResult(intent, REQUEST_CHANGE_PWD);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
//            case R.id.lock_time:
//            case R.id.btn_select_time:
//                String title = SpUtil.getInstance().getString(Constants.LOCK_APART_TITLE, "");
//                dialog.setTitle(title);
//                dialog.show();
//                break;
//            case R.id.lock_type_pattern:
//                if (!currLockType.equals(TYPE_GESTURE)) {
//                    intent = new Intent(LockSettingActivity.this, GestureCreateActivity.class);
//                    startActivityForResult(intent, REQUEST_GESTRUE_CREATE);
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                }
//                break;
//            case R.id.lock_type_num:
//                if (!currLockType.equals(TYPE_NUMBER)) {
//                    intent = new Intent(LockSettingActivity.this, NumberCreateActivity.class);
//                    startActivityForResult(intent, REQUEST_NUMBER_CREATE);
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                }
//                break;
            case R.id.about_me:
                intent = new Intent(LockSettingActivity.this, AboutMeActivity.class);
                startActivity(intent);
                break;
            case R.id.lock_when:
                String title = SpUtil.getInstance().getString(Constants.LOCK_APART_TITLE, "");
                dialog.setTitle(title);
                dialog.show();
                break;
            case R.id.is_show_path:
                boolean ishideline = SpUtil.getInstance().getBoolean(Constants.LOCK_IS_HIDE_LINE, false);
                if (ishideline) {
                    SpUtil.getInstance().putBoolean(Constants.LOCK_IS_HIDE_LINE, false);
                    ToastUtil.showToast("路径已显示");
                } else {
                    SpUtil.getInstance().putBoolean(Constants.LOCK_IS_HIDE_LINE, true);
                    ToastUtil.showToast("路径已隐藏");
                }
              //  sendBroadcast(new Intent(UPDATE_LOCK_VIEW));
                break;
        }
    }

    //    @Override
//    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//        switch (compoundButton.getId()) {
//            case R.id.switch_allow_exit:
//                if (isChecked && !dialog.isShowing()) {
//                    dialog.show();
//                }
//                if (!isChecked) {
//                    mLockTime.setText("");
//                    SpUtil.getInstance().putString(Constants.LOCK_APART_TITLE, "");
//                    SpUtil.getInstance().putLong(Constants.LOCK_APART_MILLISENCONS, 0L);
//                }
//                SpUtil.getInstance().putBoolean(Constants.LOCK_AUTO_SCREEN_TIME, isChecked);
//                break;
//            case R.id.switch_relock_screen:
//                SpUtil.getInstance().putBoolean(Constants.LOCK_AUTO_SCREEN, isChecked);
//                break;
//        }
//    }
//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
//                case REQUEST_GESTRUE_CREATE:
//                    mCbLockTypePattern.setCompoundDrawables(null, null, drawableSelect, null);
//                    mCbLockTypeNum.setCompoundDrawables(null, null, drawableNormal, null);
//                    SpUtil.getInstance().putInt(Constants.LOCK_TYPE, 0);
//                    currLockType = TYPE_GESTURE;
//                    break;
//                case REQUEST_NUMBER_CREATE:
//                    mCbLockTypePattern.setCompoundDrawables(null, null, drawableNormal, null);
//                    mCbLockTypeNum.setCompoundDrawables(null, null, drawableSelect, null);
//                    SpUtil.getInstance().putInt(Constants.LOCK_TYPE, 1);
//                    currLockType = TYPE_NUMBER;
//                    break;
                case REQUEST_CHANGE_PWD:
                    ToastUtil.showToast("密码重置成功");
                    break;
            }
        }
    }


    @Override
    public void onDismiss(DialogInterface dialogInterface) {
//        if (TextUtils.isEmpty(mLockTime.getText().toString())) {
//            mSwitchAllowExit.setChecked(false);
//        }
    }

    //
    private class LockSettingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ON_ITEM_CLICK_ACTION)) {
                LockAutoTime info = intent.getParcelableExtra("info");
                mLockTime.setText(info.getTitle());
                SpUtil.getInstance().putString(Constants.LOCK_APART_TITLE, info.getTitle());
                SpUtil.getInstance().putLong(Constants.LOCK_APART_MILLISENCONS, info.getTime());
                SpUtil.getInstance().putBoolean(Constants.LOCK_AUTO_SCREEN_TIME, true);
                dialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLockSettingReceiver);
    }

}
