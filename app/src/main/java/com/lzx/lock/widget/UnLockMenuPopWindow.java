package com.lzx.lock.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.activity.unlock.GestureSelfUnlockActivity;
import com.lzx.lock.activity.unlock.NumberSelfUnlockActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.utils.ScreenUtil;
import com.lzx.lock.utils.SpUtil;

/**
 * Created by xian on 2017/2/17.
 */

public class UnLockMenuPopWindow extends PopupWindow implements View.OnClickListener {

    private View mContentView;
    private Activity mContext;
    private TextView menuNotLock, menuSetting, checkboxPattern;
    private String pkgName;
    private Intent intent;
    private Drawable drawableSelect;
    private Drawable drawableNormal;
    private Drawable drawInvisible;
    public static final String UPDATE_LOCK_VIEW = "update_lock_view";


    public UnLockMenuPopWindow(final Activity context, String pkgName, boolean isShowCheckboxPattern) {
        super(context);
        this.mContext = context;
        this.pkgName = pkgName;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(R.layout.pop_unlock_menu, null);
        menuNotLock = (TextView) mContentView.findViewById(R.id.menu_not_lock);
        menuSetting = (TextView) mContentView.findViewById(R.id.menu_setting);
        checkboxPattern = (TextView) mContentView.findViewById(R.id.checkbox_pattern);
        checkboxPattern.setVisibility(isShowCheckboxPattern ? View.VISIBLE : View.GONE);
        menuNotLock.setOnClickListener(this);
        menuSetting.setOnClickListener(this);
        checkboxPattern.setOnClickListener(this);
        mContentView.setFocusableInTouchMode(true);
        setFocusable(true);
        this.setContentView(mContentView);
        this.setWidth(ScreenUtil.getPhoneWidth(context) * 55 / 100);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(new BitmapDrawable());

        drawableSelect = mContext.getResources().getDrawable(R.drawable.menu_pattern_selected);
        drawableNormal = mContext.getResources().getDrawable(R.drawable.menu_pattern_select);
        drawInvisible = mContext.getResources().getDrawable(R.drawable.menu_pattern_invisible);
        drawableSelect.setBounds(0, 0, drawableSelect.getMinimumWidth(), drawableSelect.getMinimumHeight());
        drawableNormal.setBounds(0, 0, drawableNormal.getMinimumWidth(), drawableNormal.getMinimumHeight());
        drawInvisible.setBounds(0, 0, drawableNormal.getMinimumWidth(), drawableNormal.getMinimumHeight());

    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        boolean ishideline = SpUtil.getInstance().getBoolean(Constants.LOCK_IS_HIDE_LINE, false);
        if (ishideline) {
            checkboxPattern.setCompoundDrawables(drawInvisible, null, drawableSelect, null);
        } else {
            checkboxPattern.setCompoundDrawables(drawInvisible, null, drawableNormal, null);
        }
    }

    @Override
    public void onClick(View v) {
        int lockType = SpUtil.getInstance().getInt(Constants.LOCK_TYPE);
        switch (v.getId()) {
            case R.id.menu_not_lock:
                if (lockType == 0) {
                    intent = new Intent(mContext, GestureSelfUnlockActivity.class);
                } else {
                    intent = new Intent(mContext, NumberSelfUnlockActivity.class);
                }
                intent.putExtra(Constants.LOCK_PACKAGE_NAME, pkgName);
                intent.putExtra(Constants.LOCK_FROM, Constants.LOCK_FROM_UNLOCK);
                mContext.startActivity(intent);
                mContext.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.menu_setting:
                if (lockType == 0) {
                    intent = new Intent(mContext, GestureSelfUnlockActivity.class);
                } else {
                    intent = new Intent(mContext, NumberSelfUnlockActivity.class);
                }
                intent.putExtra(Constants.LOCK_PACKAGE_NAME, pkgName);
                intent.putExtra(Constants.LOCK_FROM, Constants.LOCK_FROM_SETTING);
                mContext.startActivity(intent);
                mContext.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.checkbox_pattern:
                boolean ishideline = SpUtil.getInstance().getBoolean(Constants.LOCK_IS_HIDE_LINE, false);
                if (ishideline) {
                    checkboxPattern.setCompoundDrawables(drawInvisible, null, drawableSelect, null);
                    SpUtil.getInstance().putBoolean(Constants.LOCK_IS_HIDE_LINE, false);
                } else {
                    checkboxPattern.setCompoundDrawables(drawInvisible, null, drawableNormal, null);
                    SpUtil.getInstance().putBoolean(Constants.LOCK_IS_HIDE_LINE, true);
                }
                mContext.sendBroadcast(new Intent(UPDATE_LOCK_VIEW));
                break;
        }
        dismiss();
    }
}
