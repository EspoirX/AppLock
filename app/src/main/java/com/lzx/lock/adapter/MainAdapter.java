package com.lzx.lock.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.db.CommLockInfoManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/3/1.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private List<CommLockInfo> mLockInfos = new ArrayList<>();
    private Context mContext;
    private PackageManager packageManager;
    private CommLockInfoManager mLockInfoManager;

    public MainAdapter(Context mContext) {
        this.mContext = mContext;
        packageManager = mContext.getPackageManager();
        mLockInfoManager = new CommLockInfoManager(mContext);
    }

    public void setLockInfos(List<CommLockInfo> lockInfos) {
        mLockInfos.clear();
        mLockInfos.addAll(lockInfos);
        notifyDataSetChanged();
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_list, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MainViewHolder holder, final int position) {
        final CommLockInfo lockInfo = mLockInfos.get(position);
        initData(holder.mAppName, holder.mSwitchCompat, holder.mAppIcon, lockInfo);
        holder.mSwitchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeItemLockStatus(holder.mSwitchCompat, lockInfo, position);
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData(TextView tvAppName, CheckBox switchCompat, ImageView mAppIcon, CommLockInfo lockInfo) {
        tvAppName.setText(packageManager.getApplicationLabel(lockInfo.getAppInfo()));
        switchCompat.setChecked(lockInfo.isLocked());
        ApplicationInfo appInfo = lockInfo.getAppInfo();
        mAppIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo));
    }

    public void changeItemLockStatus(CheckBox checkBox, CommLockInfo info, int position) {
        if (checkBox.isChecked()) {
            info.setLocked(true);
            mLockInfoManager.lockCommApplication(info.getPackageName());
        } else {
            info.setLocked(false);
            mLockInfoManager.unlockCommApplication(info.getPackageName());
        }
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mLockInfos.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {
        private ImageView mAppIcon;
        private TextView mAppName;
        private CheckBox mSwitchCompat;

        public MainViewHolder(View itemView) {
            super(itemView);
            mAppIcon = (ImageView) itemView.findViewById(R.id.app_icon);
            mAppName = (TextView) itemView.findViewById(R.id.app_name);
            mSwitchCompat = (CheckBox) itemView.findViewById(R.id.switch_compat);
        }
    }
}
