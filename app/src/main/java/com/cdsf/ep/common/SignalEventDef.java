package com.cdsf.ep.common;

/**
 * Created by chenjianjun on 14-11-20.
 * <p/>
 * Beyond their own, let the future
 */
public class SignalEventDef {

    /* 登录界面事件 */
    public static final int SE_LOGIN = 0x10000002;// 登录
    public static final int SE_LOGIN_SUSESS = 0x10000003;// 登录成功
    public static final int SE_LOGIN_FAILURE = 0x10000004;// 登录失败

    /* 事项页面事件 */
    public static final int SE_MATTER_REQ = 0x20000000;// 事项请求
    public static final int SE_MATTER_REQ_SUSESS = 0x20000001;// 事项请求成功
    public static final int SE_MATTER_REQ_FAILURE = 0x20000002;// 事项请求失败
}
