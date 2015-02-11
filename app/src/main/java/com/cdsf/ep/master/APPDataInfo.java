package com.cdsf.ep.master;

import android.os.Environment;

import com.cdsf.ep.bean.AccountInfo;
import com.cdsf.ep.bean.SrvInfo;
import com.cdsf.ep.db.DBProcMgr;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjianjun on 14-11-20.
 * <p/>
 * Beyond their own, let the future
 */
public class APPDataInfo {

    private static APPDataInfo ourInstance = new APPDataInfo();

    public static APPDataInfo getInstance() {
        return ourInstance;
    }

    private APPDataInfo() {
    }

    /**
     * sd卡路径
     */
    public static String SDCART_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

    public static final String CRASH_LOCAL_DIR = SDCART_PATH + "ep/crash/";

    // 语音文件存放路径
    public static final String SOUND_LOCAL_DIR = SDCART_PATH + "ep/sound/";
    // 语音文件下载存放路径
    public static final String DOWN_SOUND_LOCAL_DIR = SDCART_PATH + "ep/down/sound/";
    // 事项声音文件临时存放文件名
    public static final String MATTER_SOUND_LOCAL_PATH = SOUND_LOCAL_DIR + "matter.amr";
    // 回复声音临时存放文件名
    public static final String REPLAY_SOUND_LOCAL_PATH = SOUND_LOCAL_DIR + "replay.amr";

    /**头像图片文件位置 */
    public static final String PIC_LOCAL_DIR = SDCART_PATH + "ep/picture/";
    // 图片回复临时存放文件目录
    public static final String REPLAY_PIC_DIR = PIC_LOCAL_DIR + "reply/";
    // 更新目录
    public static final String LOCAL_UPDATE_APK_DIR = SDCART_PATH + "ep/apk/ep.apk";



    // http基本路径
    public static String BASE_URL = "http://";
    // 登录请求URL
    public static final String LOGIN_REQ_URL = "ep/interface/login";
    // 项目查询请求URL
    public static final String PROGECT_QUERY_REQ_URL = "ep/interface/projectlist";
    // 项目添加请求URL
    public static final String PROGECT_ADD_REQ_URL = "ep/interface/addproject";
    // 项目修改请求URL
    public static final String PROGECT_MOD_REQ_URL = "ep/interface/modifyproject";
    // 项目删除请求URL
    public static final String PROGECT_DEL_REQ_URL = "ep/interface/deleteproject";
    // 人员查询请求URL
    public static final String USER_QUERY_REQ_URL = "ep/interface/getprojectusers";

    // 通道同步
    public static final String CHANNEL_SYNC_REQ_URL = "ep/interface/freshchannelcfg";

    // 事项请求URL
    public static final String MATTER_REQ_URL = "ep/interface/projectitemlist";
    // 事项回复消息查询请求
    public static final String MATTER_INFO_REPLAY_MSG_QUERY_REQ_URL = "ep/interface/itemreplylist";
    // 事项添加请求
    public static final String MATTER_ADD_REQ_URL = "ep/interface/addprojectitem";
    // 事项删除请求
    public static final String MATTER_DEL_REQ_URL = "ep/interface/deleteprojectitem";
    // 事项状态修改请求
    public static final String MATTER_MOD_STATUS_REQ_URL = "ep/interface/modifyprojectitem";
    // 事项进度修改请求
    public static final String MATTER_MOD_PROCESS_REQ_URL = "ep/interface/modifyprojectitem";
    // 回复请求
    public static final String MATTER_REPLAY_REQ_URL = "ep/interface/itemreply";
    // 图像上传接口
    public static final String PIC_UPLOAD_REQ_URL = "ep/interface/fileUpload";
    // 个人信息修改接口
    public static final String USER_INFO_MOD_REQ_URL = "ep/interface/modiyfuserinfo";
    // 注销请求
    public static final String LOGOUT_REQ_URL = "ep/interface/logoff";

    // 登录账号信息
    public AccountInfo accountInfo;

    public boolean init() {

        // 加载数据库信息
        accountInfo = DBProcMgr.getInstance().queryAccountInfo();

        SrvInfo srvInfo = DBProcMgr.getInstance().querySrvInfo();
        if (srvInfo == null) {
            APPDataInfo.BASE_URL = null;
        } else {
            APPDataInfo.BASE_URL = "http://"+ srvInfo.srvIP + ":" + srvInfo.srvPort + "/";
        }

        return true;
    }
}
