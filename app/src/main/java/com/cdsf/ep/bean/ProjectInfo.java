package com.cdsf.ep.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.NoAutoIncrement;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chenjianjun on 14-11-25.
 * <p/>
 * Beyond their own, let the future
 */
@Table(name = "ProjectInfo")
public class ProjectInfo implements Serializable {

    // 是否有新消息
    @Transient
    public Boolean isMsg;

    public ProjectInfo() {
        isMsg = false;
    }

    // 项目ID
    @Id(column = "projectID")
    @Column(column = "projectID")
    @NoAutoIncrement
    public int projectID;

    // 项目头像
    @Column(column = "projectPic")
    public String projectPic;

    // 项目名称
    @Column(column = "projectName")
    public String projectName;

    // 项目创建者
    @Column(column = "projectCreater")
    public String projectCreater;

    // 项目创建者EP值 参考CommonDef.EP_VALUE_*
    @Column(column = "projectCreaterEpValue")
    public int projectCreaterEpValue;

    // 项目简介
    @Column(column = "projectIntroduction")
    public String projectIntroduction;

    // 项目创建时间
    @Column(column = "projectCreateTime")
    public String projectCreateTime;
}
