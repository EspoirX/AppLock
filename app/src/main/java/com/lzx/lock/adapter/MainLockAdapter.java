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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class MainLockAdapter extends RecyclerView.Adapter<MainLockAdapter.LockFirstViewHolder> {

    private OnItemClickListener mOnItemClickListener;
    private List<CommLockInfo> mLockInfos;
    private PackageManager packageManager;

    public MainLockAdapter(Context context) {
        packageManager = context.getPackageManager();
        mLockInfos = new ArrayList<>();
    }

    public void setLockInfos(List<CommLockInfo> lockInfos) {
        mLockInfos.clear();
        mLockInfos.addAll(lockInfos);
        notifyDataSetChanged();
    }

    @Override
    public LockFirstViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_lock_list, parent, false);
        return new LockFirstViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LockFirstViewHolder holder, final int position) {
        final CommLockInfo info = mLockInfos.get(position);
        holder.mAppName.setText(packageManager.getApplicationLabel(info.getAppInfo()));
        holder.mCheckBox.setChecked(info.isLocked());

        String appName = (String) packageManager.getApplicationLabel(info.getAppInfo());
        if (info.getPackageName().equals("com.android.gallery3d")) { //相册
            holder.mAppDescribe.setText("保护你的隐私照片");
        } else if (info.getPackageName().equals("com.android.mms") || info.getPackageName().equals("com.tencent.mm")) {//微信，信息
            holder.mAppDescribe.setText("保护你的信息安全");
        } else if (appName.contains("相册") || appName.contains("Gallery") || appName.contains("gallery")) {
            holder.mAppDescribe.setText("保护你的隐私照片");
        } else { //其他
            holder.mAppDescribe.setText("保护你的个人信息");
        }
        holder.mAppDescribe.setVisibility(info.isFaviterApp() ? View.VISIBLE : View.GONE);
        ApplicationInfo appInfo = info.getAppInfo();
        if (appInfo != null) {
            holder.mAppIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.OnItemClick(info, position);
                }
            }
        });
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.OnItemClick(info, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLockInfos.size();
    }

    public class LockFirstViewHolder extends RecyclerView.ViewHolder {

        private ImageView mAppIcon;
        private TextView mAppName, mAppDescribe;
        private CheckBox mCheckBox;

        public LockFirstViewHolder(View itemView) {
            super(itemView);
            mAppIcon = (ImageView) itemView.findViewById(R.id.app_icon);
            mAppName = (TextView) itemView.findViewById(R.id.app_name);
            mAppDescribe = (TextView) itemView.findViewById(R.id.app_describe);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClick(CommLockInfo info, int position);
    }
}
