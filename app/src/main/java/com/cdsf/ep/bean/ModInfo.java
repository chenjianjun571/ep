package com.cdsf.ep.bean;

import java.io.Serializable;

/**
 * Created by chenjianjun on 14-12-16.
 * <p/>
 * Beyond their own, let the future
 */
public class ModInfo implements Serializable {

    public static final int TY_TEXT = 1;
    public static final int TY_DATE = 2;
    public static final int TY_TIME = 3;
    public static final int TY_DATEANDTIME = 4;

    // 类型 文本修改 日期修改 时间修改 日期和时间修改
    public int type;

    // 标题
    public String title;

    // 现有内容
    public String content;
}
