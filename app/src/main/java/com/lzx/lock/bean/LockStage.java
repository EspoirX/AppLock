package com.lzx.lock.bean;


import com.lzx.lock.R;


/**
 * Created by lzx on 2017/1/8.
 * 图案锁的状态
 */

public enum LockStage {
    Introduction(R.string.lock_recording_intro_header, -1, true),

    HelpScreen(R.string.lock_settings_help_how_to_record, -1, false),

    ChoiceTooShort(R.string.lock_recording_incorrect_too_short, -1, true),

    FirstChoiceValid(R.string.lock_pattern_entered_header, -1, false),

    NeedToConfirm(R.string.lock_need_to_confirm, -1, true),

    ConfirmWrong(R.string.lock_need_to_unlock_wrong, -1, true),

    ChoiceConfirmed(R.string.lock_pattern_confirmed_header, -1, false);

    /**
     * @param headerMessage  The message displayed at the top.
     * @param footerMessage  The footer message.
     * @param patternEnabled Whether the pattern widget is enabled.
     */
    LockStage(int headerMessage, int footerMessage,
              boolean patternEnabled) {
        this.headerMessage = headerMessage;
        this.footerMessage = footerMessage;
        this.patternEnabled = patternEnabled;
    }

    public final int headerMessage;
    public final int footerMessage;
    public final boolean patternEnabled;
}
