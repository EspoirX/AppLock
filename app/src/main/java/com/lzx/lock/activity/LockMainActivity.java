package com.lzx.lock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.adapter.LockMainAdapter;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.mvp.contract.LockMainContract;
import com.lzx.lock.mvp.p.LockMainPresenter;
import com.lzx.lock.utils.SpUtil;

import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */
public class LockMainActivity extends BaseActivity implements LockMainContract.View, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private LinearLayout mTopItemLayout;
    private TextView mLockAppType;
    private CheckBox mCheckBoxSys, mCheckBoxApp;
    private String titleSystem, titleApps;
    private LockMainAdapter mLockMainAdapter;
    private LockMainPresenter mLockMainPresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_lock_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mTopItemLayout = (LinearLayout) findViewById(R.id.top_item_layout);
        mLockAppType = (TextView) findViewById(R.id.lock_app_type);
        mCheckBoxSys = (CheckBox) findViewById(R.id.checkbox_sys);
        mCheckBoxApp = (CheckBox) findViewById(R.id.checkbox_app);
        mTopItemLayout.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mLockMainAdapter = new LockMainAdapter(this);
        mRecyclerView.setAdapter(mLockMainAdapter);
        mLockMainPresenter = new LockMainPresenter(this, this);
        mLockMainPresenter.loadAppInfo(this, true);
    }

    @Override
    protected void initAction() {
        titleSystem = "系统应用";
        titleApps = "用户应用";
        mCheckBoxSys.setVisibility(View.VISIBLE);
        mCheckBoxSys.setChecked(SpUtil.getInstance().getBoolean(Constants.LOCK_IS_SELECT_ALL_SYS, false));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View stickyInfoView = recyclerView.findChildViewUnder(mTopItemLayout.getMeasuredWidth() / 2, 5);
                if (stickyInfoView != null && stickyInfoView.getContentDescription() != null) {
                    String title = String.valueOf(stickyInfoView.getContentDescription());
                    mCheckBoxApp.setVisibility(title.equals(titleSystem) ? View.GONE : View.VISIBLE);
                    mCheckBoxSys.setVisibility(title.equals(titleSystem) ? View.VISIBLE : View.GONE);
                    if (mCheckBoxApp.getVisibility() == View.VISIBLE) {
                        boolean isSelectAll = SpUtil.getInstance().getBoolean(Constants.LOCK_IS_SELECT_ALL_APP, false);
                        if (isSelectAll) {
                            mCheckBoxApp.setChecked(true);
                        } else {
                            mCheckBoxApp.setChecked(false);
                        }
                    }
                    mLockAppType.setText(title);
                }
                View transInfoView = recyclerView.findChildViewUnder(mTopItemLayout.getMeasuredWidth() / 2, mTopItemLayout.getMeasuredHeight() + 1);
                if (transInfoView != null && transInfoView.getTag() != null) {
                    int transViewStatus = (int) transInfoView.getTag();
                    int dealtY = transInfoView.getTop() - mTopItemLayout.getMeasuredHeight();
                    if (transViewStatus == LockMainAdapter.HAS_STICKY_VIEW) {
                        if (transInfoView.getTop() > 0) {
                            mTopItemLayout.setTranslationY(dealtY);
                        } else {
                            mTopItemLayout.setTranslationY(0);
                        }
                    } else if (transViewStatus == LockMainAdapter.NONE_STICKY_VIEW) {
                        mTopItemLayout.setTranslationY(0);
                    }
                }
            }
        });
        mCheckBoxSys.setOnClickListener(this);
        mCheckBoxApp.setOnClickListener(this);
    }

    @Override
    public void loadAppInfoSuccess(List<CommLockInfo> list) {
        mTopItemLayout.setVisibility(View.VISIBLE);
        mLockMainAdapter.setLockInfos(list);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkbox_sys:
                SpUtil.getInstance().putBoolean(Constants.LOCK_IS_SELECT_ALL_SYS, mCheckBoxSys.isChecked());
                mLockMainAdapter.changeSysLockStatus(mCheckBoxSys);
                break;
            case R.id.checkbox_app:
                mLockMainAdapter.changeUserLockStatus(mCheckBoxApp);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_setting) {
            Intent intent = new Intent(LockMainActivity.this, LockSettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
