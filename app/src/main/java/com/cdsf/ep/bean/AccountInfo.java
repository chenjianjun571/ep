package com.cdsf.ep.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;

import java.io.Serializable;

/**
 * Created by chenjianjun on 14-12-12.
 * <p/>
 * Beyond their own, let the future
 */
@Table(name = "AccountInfo")
public class AccountInfo implements Serializable {

    // 用户名
    @Id(column = "userName")
    @Column(column = "userName")
    public String userName;

    // 用户密码
    @Column(column = "passWord")
    public String passWord;

    // 用户头像
    @Column(column = "userPic")
    public String userPic;

    // 用户ID
    @Column(column = "userID")
    public int userID;

    // 电话号码
    @Column(column = "userPthone")
    public String userPthone;

    // 个人签名
    @Column(column = "userSign")
    public String userSign;

    // EP值 参考CommonDef.EP_VALUE_*
    @Column(column = "epValue")
    public int epValue;

    // 服务器上的版本号
    @Transient
    public String version;

    // 版本下载地址
    @Transient
    public String apkURL;
}
