package com.lzx.lock.base;

/**
 * Created by xian on 2017/2/17.
 */

public class AppConstants {

    public static final String LOCK_STATE = "app_lock_state"; //应用锁开关(状态，true开，false关)
    public static final String LOCK_FAVITER_NUM = "lock_faviter_num"; //推荐加锁应用个数
    public static final String LOCK_SYS_APP_NUM = "lock_sys_app_num"; //系统应用个数
    public static final String LOCK_USER_APP_NUM = "lock_user_app_num"; //非系统应用个数
    public static final String LOCK_IS_INIT_FAVITER = "lock_is_init_faviter"; //是否初始化了faviter数据表
    public static final String LOCK_IS_INIT_DB = "lock_is_init_db"; //是否初始化了数据库表
    public static final String APP_PACKAGE_NAME = "com.lzx.lock"; //包名
    public static final String LOCK_IS_HIDE_LINE = "lock_is_hide_line"; //是否隐藏路径
    public static final String LOCK_PWD = "lock_pwd";//应用锁密码
    public static final String LOCK_IS_FIRST_LOCK = "is_lock"; //是否加过锁
    public static final String LOCK_AUTO_SCREEN = "lock_auto_screen"; //是否在手机屏幕关闭后再次锁定
    public static final String LOCK_AUTO_SCREEN_TIME = "lock_auto_screen_time"; //是否在手机屏幕关闭后一段时间再次锁定
    public static final String LOCK_CURR_MILLISENCONS = "lock_curr_milliseconds"; //记录当前的时间（毫秒）
    public static final String LOCK_APART_MILLISENCONS = "lock_apart_milliseconds"; //记录相隔的时间（毫秒）
    public static final String LOCK_APART_TITLE = "lock_apart_title"; ///记录相隔的时间对应的标题
    public static final String LOCK_LAST_LOAD_PKG_NAME = "last_load_package_name";
    public static final String LOCK_PACKAGE_NAME = "lock_package_name"; //点开的锁屏应用的包名
    public static final String LOCK_FROM = "lock_from"; //解锁后转跳的action
    public static final String LOCK_FROM_FINISH = "lock_from_finish"; //解锁后转跳的action是finish
    public static final String LOCK_FROM_SETTING = "lock_from_setting"; //解锁后转跳的action是setting
    public static final String LOCK_FROM_UNLOCK = "lock_from_unlock"; //解锁后转跳的action
    public static final String LOCK_FROM_LOCK_MAIN_ACITVITY = "lock_from_lock_main_activity";
    public static final String LOCK_AUTO_RECORD_PIC = "AutoRecordPic"; //是否开启自动拍照
}
