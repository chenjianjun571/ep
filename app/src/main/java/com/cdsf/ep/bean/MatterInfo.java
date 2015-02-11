package com.cdsf.ep.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjianjun on 14-11-27.
 * <p/>
 * Beyond their own, let the future
 */
@Table(name = "MatterInfo")
public class MatterInfo implements Serializable {

    public static final String TABLE_COLUMN_MATTER_INFO_MATTER_ID = "matterID";

    // 事项ID
    @Id(column = TABLE_COLUMN_MATTER_INFO_MATTER_ID)
    @Column(column = TABLE_COLUMN_MATTER_INFO_MATTER_ID)
    public String matterID;

    // 项目ID
    @Column(column = "projectID")
    public int projectID;

    // 事项创建者头像url
    @Column(column = "matterCreateUserPic")
    public String matterCreateUserPic;

    // 事项创建者名称
    @Column(column = "matterCreateUserName")
    public String matterCreateUserName;

    // 事项创建者的EP值 参考CommonDef.EP_VALUE_*
    @Column(column = "matterCreateEpValue")
    public int matterCreateEpValue;

    // 事项类型参考CommonDef.MATTER_TYPE_TEXT CommonDef.MATTER_TYPE_SOUND
    @Column(column = "matterType")
    public int matterType;

    // 事项内容 如果是文本，存放的是内容，如果是语音，存放的语音文件下载路径
    @Column(column = "matterContent")
    public String matterContent;

    // 事项状态 参考CommonDef.MATTER_STATUS_INIT
    @Column(column = "matterStatus")
    public int matterStatus;

    // 事项进度
    @Column(column = "matterProcess")
    public int matterProcess;

    // 事项接收人员
    @Column(column = "matterRecvPersonnels")
    public String matterRecvPersonnels;

    // 事项开始时间
    @Column(column = "matterStartTime")
    public String matterStartTime;

    // 事项结束时间
    @Column(column = "matterEndTime")
    public String matterEndTime;

    // 最新的回复
    @Transient
    public MatterReplyInfo matterReplyInfo;
}
