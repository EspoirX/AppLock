package com.lzx.lock.mvp.p;

import android.widget.ImageView;

import com.lzx.lock.bean.InputResult;
import com.lzx.lock.mvp.contract.NumberUnLockContract;
import com.lzx.lock.utils.LockPatternUtils;
import com.lzx.lock.utils.StringUtil;

import java.util.List;

/**
 * Created by xian on 2017/2/17.
 */

public class NumberUnLockPresenter implements NumberUnLockContract.Presenter {

    private NumberUnLockContract.View mView;
    private static final int COUNT = 4; //4个点
    //private boolean numberDisable = false;
    private int mFailedPatternAttemptsSinceLastTimeout = 0;

    public NumberUnLockPresenter(NumberUnLockContract.View view) {
        mView = view;
    }

    @Override
    public void clickNumber(List<String> numInput, List<ImageView> pointList, String number, String lockPwd) {
        if (numInput.size() < COUNT) {
            numInput.add(number);
        }
        mView.setNumberPointImageResource(numInput);

        StringBuffer pBuffer = new StringBuffer();
        for (String s : numInput) {
            pBuffer.append(s);
        }
        doForResult(inputCheck(pBuffer.toString(), numInput, lockPwd));
    }

    private void doForResult(InputResult result) {
        switch (result) {
            case CONTINUE:
                break;
            case SUCCESS:
                //解锁成功
                mView.unLockSuccess();
                break;
            case ERROR:
                mFailedPatternAttemptsSinceLastTimeout++;
                int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mFailedPatternAttemptsSinceLastTimeout;
                if (retry >= 0) {
                    mView.unLockError(retry);
                }
                if (mFailedPatternAttemptsSinceLastTimeout >= 3) { //失败次数大于3次
                    mView.clearPassword();
                }
                if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) { //失败次数大于阻止用户前的最大错误尝试次数
                    mView.clearPassword();
                } else {
                    // numberDisable = true;
                    mView.clearPassword();
                }
                break;
            default:
                break;
        }
    }

    private InputResult inputCheck(String password, List<String> numInput, String lockPwd) {
        InputResult result;
        if (numInput.size() == COUNT) {
            numInput.clear();
            String md5 = StringUtil.toMD5(password);
            if (md5.equals(lockPwd)) {
                result = InputResult.SUCCESS;
            } else {
                result = InputResult.ERROR;
            }
        } else {
            result = InputResult.CONTINUE;
        }
        return result;
    }

    @Override
    public void onDestroy() {

    }
}