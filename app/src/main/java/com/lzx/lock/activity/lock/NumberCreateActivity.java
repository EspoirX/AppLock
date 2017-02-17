package com.lzx.lock.activity.lock;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.mvp.contract.NumberCreateContract;
import com.lzx.lock.mvp.p.NumberCreatePresenter;
import com.lzx.lock.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class NumberCreateActivity extends BaseActivity implements View.OnClickListener, NumberCreateContract.View{

    private TextView mLockTip;
    private ImageView mNumPoint_1, mNumPoint_2, mNumPoint_3, mNumPoint_4;
    private TextView mNumber_0, mNumber_1, mNumber_2, mNumber_3, mNumber_4, mNumber_5, mNumber_6, mNumber_7, mNumber_8, mNumber_9;
    private ImageView mNumberDel;

    private static final int COUNT = 4; //4个点
    private List<String> numInput; //存储输入数字的列表
    private List<ImageView> pointList;
    private NumberCreateContract.Presenter mPresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_num_create;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mLockTip = (TextView) findViewById(R.id.tv_lock_tip);
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
        initNumLayout();
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

    @Override
    public void setNumberPointImageResource(ImageView iv, int resId) {
        iv.setImageResource(resId);
    }

    @Override
    public void updateLockTipString(int resId, boolean isToast) {
        if (isToast)
            ToastUtil.showToast(getString(resId));
        else {
            mLockTip.setText(resId);
        }
    }

    @Override
    public void createLockSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void completedFirstTime() {
        for (ImageView iv : pointList) {
            iv.setImageResource(R.drawable.num_point);
        }
    }
}
