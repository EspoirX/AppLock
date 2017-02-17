package com.lzx.lock.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 自动锁屏时间
 * Created by lzx on 2017/1/10.
 *
 * 15秒=15000，30秒=30000 1分钟=60000  3分钟=180000 5分钟=300000
 * 10分钟=600000 30分钟=1800000
 *
 */

public class LockAutoTime implements Parcelable {
    private String title; //标题
    private long time;  //对应时间（毫秒）

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeLong(this.time);
    }

    public LockAutoTime() {
    }

    protected LockAutoTime(Parcel in) {
        this.title = in.readString();
        this.time = in.readLong();
    }

    public static final Parcelable.Creator<LockAutoTime> CREATOR = new Parcelable.Creator<LockAutoTime>() {
        @Override
        public LockAutoTime createFromParcel(Parcel source) {
            return new LockAutoTime(source);
        }

        @Override
        public LockAutoTime[] newArray(int size) {
            return new LockAutoTime[size];
        }
    };
}