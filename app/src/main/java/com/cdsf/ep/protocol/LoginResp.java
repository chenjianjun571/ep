package com.cdsf.ep.protocol;

/**
 * Created by chenjianjun on 14-11-20.
 * <p/>
 * Beyond their own, let the future
 */
public class LoginResp {

    // 用户ID
    public int userID;
    // 用户头像URL
    public String userPic;
    // 联系方式
    public String user_phone;
    // 签名
    public String user_signature;
    // version
    public String version;
    // apk url
    public String apk_url;
    // ep值
    public int epValue;
    // 登录结果 0:成功 其他值是失败
    public int result;
    // 错误描述
    public String errorMsg;

}
