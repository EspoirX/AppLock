package com.lzx.lock.widget;

import java.util.List;

/**
 * Created by lzx on 2017/1/8.
 * 图形解锁监听实现类
 */

public class LockPatternViewPattern implements LockPatternView.OnPatternListener {

    private LockPatternView mLockPatternView;
    private onPatternListener mPatternListener;

    public interface onPatternListener {
        void onPatternDetected(List<LockPatternView.Cell> pattern);
    }

    public void setPatternListener(onPatternListener patternListener) {
        mPatternListener = patternListener;
    }

    public LockPatternViewPattern(LockPatternView lockPatternView) {
        mLockPatternView = lockPatternView;
    }

    @Override
    public void onPatternStart() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        patternInProgress();
    }

    private void patternInProgress() {

    }

    @Override
    public void onPatternCleared() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
    }

    @Override
    public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

    }

    @Override
    public void onPatternDetected(List<LockPatternView.Cell> pattern) {
        if (pattern == null)
            return;
        if (mPatternListener != null) {
            mPatternListener.onPatternDetected(pattern);
        }
    }

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

}
