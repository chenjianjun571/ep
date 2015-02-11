package com.cdsf.ep.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;

import java.io.Serializable;

/**
 * Created by chenjianjun on 14-12-30.
 * <p/>
 * Beyond their own, let the future
 */
@Table(name = "PersonnelInfo")
public class PersonnelInfo implements Serializable {

    private int id;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // 项目ID
    @Column(column = "projectID")
    public int projectID;

    // 人员ID
    @Column(column = "userID")
    public String userID;

    // 人员名称
    @Column(column = "userName")
    public String userName;

    // 人员头像
    @Column(column = "userPic")
    public String userPic;

    // 人员签名
    @Column(column = "userSign")
    public String userSign;

    // EP值 参考CommonDef.EP_VALUE_*
    @Column(column = "epValue")
    public int epValue;

    // 人员联系方式
    @Column(column = "userPhone")
    public String userPhone;

    // 标记最后一个
    @Transient
    public boolean isEnd;
    // 标记是否删除
    @Transient
    public boolean isDel;
    public PersonnelInfo() {
        isEnd = false;
        isDel = false;
    }
}
