package com.lzx.lock.module.main;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lzx.lock.R;
import com.lzx.lock.adapter.MainAdapter;
import com.lzx.lock.base.BaseFragment;
import com.lzx.lock.bean.CommLockInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/3/1.
 */

public class SysAppFragment extends BaseFragment {

    public static SysAppFragment newInstance(List<CommLockInfo> list) {
        SysAppFragment sysAppFragment = new SysAppFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("data", (ArrayList<? extends Parcelable>) list);
        sysAppFragment.setArguments(bundle);
        return sysAppFragment;
    }

    private RecyclerView mRecyclerView;
    private List<CommLockInfo> data, list;
    private MainAdapter mMainAdapter;

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_app_list;
    }

    @Override
    protected void init(View rootView) {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        data = getArguments().getParcelableArrayList("data");
        mMainAdapter = new MainAdapter(getContext());
        mRecyclerView.setAdapter(mMainAdapter);
        list = new ArrayList<>();
        for (CommLockInfo info : data) {
            if (info.isSysApp()) {
                list.add(info);
            }
        }
        mMainAdapter.setLockInfos(list);
    }
}
