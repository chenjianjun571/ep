package com.cdsf.ep.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.NoAutoIncrement;
import com.lidroid.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by chenjianjun on 14-12-3.
 * <p/>
 * Beyond their own, let the future
 */
@Table(name = "MatterReplyInfo")
public class MatterReplyInfo implements Serializable {

    // 回复ID
    @Id(column = "replayID")
    @Column(column = "replayID")
    @NoAutoIncrement
    public int replayID;

    // 事项ID
    @Column(column = MatterInfo.TABLE_COLUMN_MATTER_INFO_MATTER_ID)
    public String matterID;

    // 用户头像url
    @Column(column = "replyUserPic")
    public String replyUserPic;

    // 回复人名称
    @Column(column = "replyUserNmae")
    public String replyUserNmae;

    // 回复人EP值 参考CommonDef.EP_VALUE_*
    @Column(column = "replyEpValue")
    public int replyEpValue;

    // 回复类型 CommonDef.MATTER_TYPE_TEXT CommonDef.MATTER_TYPE_SOUND
    @Column(column = "replyType")
    public int replyType;

    // 回复内容 如果回复的是文本，则存放的是文本内容，如果是声音，存放的是一个URL
    @Column(column = "replyContent")
    public String replyContent;

    // 回复时间
    @Column(column = "replyDate")
    public String replyDate;
}

