package com.lzx.lock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.adapter.MainLockAdapter;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.mvp.contract.MainContract;
import com.lzx.lock.mvp.p.MainPresenter;
import com.lzx.lock.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;

public class FirstMainActivity extends BaseActivity implements MainContract.View, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private TextView mLockBtn;
    private MainPresenter mPresenter;
    private MainLockAdapter mAdapter;
    private ArrayList<CommLockInfo> mLockList; //保存的加锁应用
    private ArrayList<CommLockInfo> mUnLockList; //保存的没加锁应用
    private int index = 0;
    public static final String ACTION_FINISH = "action_finish";
    private MainReceiver mReceiver;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLockBtn = (TextView) findViewById(R.id.lock_btn);

    }

    @Override
    protected void initData() {
        mLockList = new ArrayList<>();
        mUnLockList = new ArrayList<>();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_FINISH);
        mReceiver = new MainReceiver();
        registerReceiver(mReceiver, filter);

        mPresenter = new MainPresenter(this, this);
        mAdapter = new MainLockAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mPresenter.loadAppInfo(this, false);
    }

    @Override
    protected void initAction() {
        mLockBtn.setOnClickListener(this);
        mAdapter.setOnItemClickListener(new MainLockAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(CommLockInfo info, int position) {
                if (info.isLocked()) {
                    info.setLocked(false);
                    index--;
                    if (index == 0) {
                        mLockBtn.setEnabled(false);
                    }
                    mLockList.remove(info);
                    mUnLockList.add(info);
                } else {
                    info.setLocked(true);
                    index++;
                    if (!mLockBtn.isEnabled()) {
                        mLockBtn.setEnabled(true);
                    }
                    mLockList.add(info);
                    mUnLockList.remove(info);
                }
                setHeaderTipString(index);
                mAdapter.notifyItemChanged(position);
            }
        });
    }

    private void setHeaderTipString(int index) {
        String num = String.valueOf(index);
        String format = getResources().getString(R.string.lock_first_btn);
        String str = String.format(format, num);
        mLockBtn.setText(str);
    }

    @Override
    public void loadAppInfoSuccess(List<CommLockInfo> list) {
        int index = SpUtil.getInstance().getInt(Constants.LOCK_FAVITER_NUM);
        this.index = index;
        setHeaderTipString(index);
        mAdapter.setLockInfos(list);
        for (CommLockInfo commLockInfo : list) {
            if (commLockInfo.isLocked()) {
                mLockList.add(commLockInfo);
            }
        }
        if (list.size() == 0) {
            mPresenter.loadAppInfo(FirstMainActivity.this, false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lock_btn:
                if (mAdapter.getItemCount() != 0) {
                    Intent intent = new Intent(this, CreatePwdActivity.class);
                    intent.putParcelableArrayListExtra("lock_list", mLockList);
                    intent.putParcelableArrayListExtra("unlock_list", mUnLockList);
                    startActivity(intent);
                }
                break;
        }
    }

    private class MainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_FINISH)) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
