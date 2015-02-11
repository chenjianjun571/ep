package com.cdsf.ep.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by chenjianjun on 14-12-30.
 * <p/>
 * Beyond their own, let the future
 */
@Table(name = "SrvInfo")
public class SrvInfo {

    // IP
    @Id(column = "srvIP")
    @Column(column = "srvIP")
    public String srvIP;

    // 端口
    @Column(column = "srvPort")
    public String srvPort;

}
