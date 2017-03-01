package com.lzx.lock.module.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lzx.lock.R;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.mvp.contract.LockMainContract;
import com.lzx.lock.mvp.p.LockMainPresenter;
import com.lzx.lock.utils.SystemBarHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/3/1.
 */

public class MainActivity extends BaseActivity implements LockMainContract.View {

    private RelativeLayout mTopLayout;
    private ImageView mBtnSetting;
    private EditText mEditSearch;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private CommentPagerAdapter mPagerAdapter;
    private LockMainPresenter mLockMainPresenter;

    private List<String> titles = new ArrayList<>();
    private List<Fragment> fragmentList = new ArrayList<>();

    @Override
    public int getLayoutId() {
        return R.layout.activity_main2;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mBtnSetting = (ImageView) findViewById(R.id.btn_setting);
        mEditSearch = (EditText) findViewById(R.id.edit_search);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTopLayout = (RelativeLayout) findViewById(R.id.top_layout);
        mTopLayout.setPadding(0, SystemBarHelper.getStatusBarHeight(this), 0, 0);

        mLockMainPresenter = new LockMainPresenter(this, this);
        mLockMainPresenter.loadAppInfo(this, true);

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initAction() {

    }

    @Override
    public void loadAppInfoSuccess(List<CommLockInfo> list) {
        int sysNum = 0;
        int userNum = 0;
        for (CommLockInfo info : list) {
            if (info.isSysApp()) {
                sysNum++;
            } else {
                userNum++;
            }
        }
        titles.add("系统应用" + " (" + sysNum + ")");
        titles.add("第三方应用" + " (" + userNum + ")");
        SysAppFragment sysAppFragment = SysAppFragment.newInstance(list);
        UserAppFragment userAppFragment = UserAppFragment.newInstance(list);
        fragmentList.add(sysAppFragment);
        fragmentList.add(userAppFragment);
        mPagerAdapter = new CommentPagerAdapter(getSupportFragmentManager(), fragmentList, titles);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public class CommentPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> titles = new ArrayList<>();


        public CommentPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> titles) {
            super(fm);
            this.fragmentList = fragmentList;
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return titles.size();
        }
    }

}
