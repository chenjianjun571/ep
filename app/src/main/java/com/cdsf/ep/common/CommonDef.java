package com.cdsf.ep.common;

/**
 * Created by chenjianjun on 14-12-3.
 * <p/>
 * Beyond their own, let the future
 */
public class CommonDef {

    public static final int MATTER_STATUS_INIT = 1;// 待处理
    public static final int MATTER_STATUS_RECV = 2;// 接收
    public static final int MATTER_STATUS_RE_FUSE = 3;// 拒绝
    public static final int MATTER_STATUS_ALRM = 4;// 告警
    public static final int MATTER_STATUS_FINAL = 5;// 完成
    public static final int MATTER_STATUS_TIMEOUT = 6;// 超时

    // 项目信息
    public static final String ACTION_PROJECT = "com.cdsf.ep.common.CommonDef.projectinfo";
    public static final String ACTION_MATTER = "com.cdsf.ep.common.CommonDef.matter";
    public static final String ACTION_MATTER_REPLAY = "com.cdsf.ep.common.CommonDef.matter.replay";

    // 事项类型
    public static final int MATTER_TYPE_TEXT = 1;// 文本
    public static final int MATTER_TYPE_SOUND = 2;// 语音
    public static final int MATTER_TYPE_PIC = 3;// 图片

    // 回复类型
    public static final int REPLAY_TYPE_TEXT = 1;// 文本
    public static final int REPLAY_TYPE_SOUND = 2;// 语音
    public static final int REPLAY_TYPE_PIC = 3;// 图片

    // 回复加载类型
    public static final int REPLAY_LOAD_TYPE_INIT = 3;
    public static final int REPLAY_LOAD_TYPE_FRONT = 1;
    public static final int REPLAY_LOAD_TYPE_BACK = 2;
    public static final int REPLAY_LOAD_TYPE_NEW_ONE = 4;

    // EP等级
    public static final int EP_VALUE_1 = 1;
    public static final int EP_VALUE_2 = 2;
    public static final int EP_VALUE_3 = 3;
    public static final int EP_VALUE_4 = 4;
    public static final int EP_VALUE_5 = 5;
    public static final int EP_VALUE_6 = 6;
    public static final int EP_VALUE_7 = 7;
    public static final int EP_VALUE_8 = 8;
    public static final String EP_VALUE_STR_1 = "沟通无法评价";
    public static final String EP_VALUE_STR_2 = "沟通小白";
    public static final String EP_VALUE_STR_3 = "沟通小菜";
    public static final String EP_VALUE_STR_4 = "沟通达人";
    public static final String EP_VALUE_STR_5 = "乐于沟通";
    public static final String EP_VALUE_STR_6 = "事事畅通";
    public static final String EP_VALUE_STR_7 = "无事不通";
    public static final String EP_VALUE_STR_8 = "通天大神";

}
