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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.base.Constants;
import com.lzx.lock.bean.CommLockInfo;
import com.lzx.lock.db.CommLockInfoManager;
import com.lzx.lock.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class LockMainAdapter  extends RecyclerView.Adapter<LockMainAdapter.MainLockViewHolder>{

    private List<CommLockInfo> mLockInfos = new ArrayList<>();
    private Context mContext;
    private PackageManager packageManager;
    private CommLockInfoManager mLockInfoManager;


    public static final int FIRST_STICKY_VIEW = 1;
    public static final int HAS_STICKY_VIEW = 2;
    public static final int NONE_STICKY_VIEW = 3;
    private int userAppNum = 0;


    public LockMainAdapter(Context mContext) {
        this.mContext = mContext;
        packageManager = mContext.getPackageManager();
        mLockInfoManager = new CommLockInfoManager(mContext);
        userAppNum = SpUtil.getInstance().getInt(Constants.LOCK_USER_APP_NUM);

    }

    public void setLockInfos(List<CommLockInfo> lockInfos) {
        mLockInfos.clear();
        mLockInfos.addAll(lockInfos);
        notifyDataSetChanged();
    }

    @Override
    public MainLockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lock_main_list, parent, false);
        return new MainLockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MainLockViewHolder holder, final int position) {
        final CommLockInfo lockInfo = mLockInfos.get(position);

        if (position == 0) {
            holder.mHeaderLayout.setVisibility(View.VISIBLE);
            holder.mLockAppType.setText(lockInfo.getTopTitle());
            initData(holder.mAppName, holder.mCheckBox, holder.mAppIcon, lockInfo);
            holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeItemLockStatus(holder.mCheckBox, lockInfo, position);
                }
            });
            holder.itemView.setTag(FIRST_STICKY_VIEW);
        } else {
            if (lockInfo.isSysApp() != mLockInfos.get(position - 1).isSysApp()) {
                holder.mHeaderLayout.setVisibility(View.VISIBLE);
                holder.mLockAppType.setText(lockInfo.getTopTitle());
                initData(holder.mAppName, holder.mCheckBox, holder.mAppIcon, lockInfo);

                boolean isSelectAll = SpUtil.getInstance().getBoolean(Constants.LOCK_IS_SELECT_ALL_APP, false);
                holder.mCheckBoxAll.setChecked(isSelectAll);
                holder.mCheckBoxAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeUserLockStatus(holder.mCheckBoxAll);
                    }
                });
                holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeItemLockStatus(holder.mCheckBox, lockInfo, position);
                    }
                });
                holder.itemView.setTag(HAS_STICKY_VIEW);
            } else {
                holder.mHeaderLayout.setVisibility(View.GONE);
                initData(holder.mAppName, holder.mCheckBox, holder.mAppIcon, lockInfo);
                holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeItemLockStatus(holder.mCheckBox, lockInfo, position);
                    }
                });
                holder.itemView.setTag(NONE_STICKY_VIEW);
            }
        }
        holder.itemView.setContentDescription(lockInfo.getTopTitle());
    }

    /**
     * 初始化数据
     */
    private void initData(TextView tvAppName, CheckBox checkBox, ImageView mAppIcon, CommLockInfo lockInfo) {
        tvAppName.setText(packageManager.getApplicationLabel(lockInfo.getAppInfo()));
        checkBox.setChecked(lockInfo.isLocked());
        ApplicationInfo appInfo = lockInfo.getAppInfo();
        mAppIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo));
    }

    /**
     * 改变用户应用状态
     */
    public void changeUserLockStatus(CheckBox checkBoxAll) {
        for (int i = 0; i < mLockInfos.size(); i++) {
            CommLockInfo lockInfo = mLockInfos.get(i);
            if (checkBoxAll.isChecked() && !lockInfo.isSysApp()) {
                SpUtil.getInstance().putBoolean(Constants.LOCK_IS_SELECT_ALL_APP, true);
                lockInfo.setLocked(true);
                mLockInfoManager.setIsUnLockThisApp(lockInfo.getPackageName(), false);
                mLockInfoManager.lockCommApplication(lockInfo.getPackageName());
            } else if (!checkBoxAll.isChecked() && !lockInfo.isSysApp()) {
                SpUtil.getInstance().putBoolean(Constants.LOCK_IS_SELECT_ALL_APP, false);
                lockInfo.setLocked(false);
                mLockInfoManager.unlockCommApplication(lockInfo.getPackageName());
            }
        }
        notifyDataSetChanged();
    }


    /**
     * 改变系统应用状态
     */
    public void changeSysLockStatus(CheckBox checkBoxAll) {
        for (int i = 0; i < mLockInfos.size(); i++) {
            CommLockInfo lockInfo = mLockInfos.get(i);
            if (checkBoxAll.isChecked() && lockInfo.isSysApp()) {
                lockInfo.setLocked(true);
                mLockInfoManager.setIsUnLockThisApp(lockInfo.getPackageName(), false);
                mLockInfoManager.lockCommApplication(lockInfo.getPackageName());
            } else if (!checkBoxAll.isChecked() && lockInfo.isSysApp()) {
                lockInfo.setLocked(false);
                mLockInfoManager.unlockCommApplication(lockInfo.getPackageName());
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 改变普通item应用状态
     */
    public void changeItemLockStatus(CheckBox checkBox, CommLockInfo info, int position) {
        if (checkBox.isChecked()) {
            info.setLocked(true);
            mLockInfoManager.setIsUnLockThisApp(info.getPackageName(), false);
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


    public class MainLockViewHolder extends RecyclerView.ViewHolder {
        private ImageView mAppIcon;
        private TextView mAppName, mLockAppType;
        private CheckBox mCheckBox,mCheckBoxAll;
        private LinearLayout mHeaderLayout;

        public MainLockViewHolder(View itemView) {
            super(itemView);
            mHeaderLayout = (LinearLayout) itemView.findViewById(R.id.top_item_layout);
            mAppIcon = (ImageView) itemView.findViewById(R.id.app_icon);
            mAppName = (TextView) itemView.findViewById(R.id.app_name);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            mCheckBoxAll = (CheckBox) itemView.findViewById(R.id.checkbox_sys);
            mLockAppType = (TextView) itemView.findViewById(R.id.lock_app_type);
        }
    }
}
