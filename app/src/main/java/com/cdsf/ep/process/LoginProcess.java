package com.cdsf.ep.process;

import android.os.Message;
import android.util.Log;

import com.cdsf.ep.bean.AccountInfo;
import com.cdsf.ep.common.SignalEventDef;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.protocol.JsonParse;
import com.cdsf.ep.protocol.LoginResp;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by chenjianjun on 14-11-20.
 * <p/>
 * Beyond their own, let the future
 */
public class LoginProcess {

    public static final String TAG = LoginProcess.class.getSimpleName();

    public interface OnSendMsgListener {
        /**
         * 发送消息，让外部来处理逻辑
         *
         * @param msg 消息.
         */
        void onSendNextMsg(Message msg);
    }

    OnSendMsgListener mOnSendMsgListener;
    public void setOnClickListener(OnSendMsgListener l) {
        mOnSendMsgListener = l;
    }

    /**
     * 登录
     * @param userName 用户名
     * @param passWord 密码
     */
    public void login(final String userName, final String passWord) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.LOGIN_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("user_name", userName);
            jsonStr.put("passwd", passWord);

            params.setBodyEntity(new StringEntity(jsonStr.toString(),"utf-8"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 10);// 配置超时时间?
        http.send(HttpRequest.HttpMethod.POST,
                url,
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        Log.e("登录","登录应答:"+responseInfo.result);

                        LoginResp resp = new LoginResp();
                        Message msg = new Message();

                        // 解析应答
                        if(JsonParse.parseLoginResp(responseInfo.result, resp))
                        {
                            if (resp.result != 0) {

                                msg.what = SignalEventDef.SE_LOGIN_FAILURE;
                                msg.obj = "登录失败,原因:"+resp.errorMsg;

                            }
                            else
                            {
                                msg.what = SignalEventDef.SE_LOGIN_SUSESS;
                                msg.obj = "登录成功";

                                // 保存数据库信息
                                if (APPDataInfo.getInstance().accountInfo == null) {
                                    APPDataInfo.getInstance().accountInfo = new AccountInfo();
                                }

                                APPDataInfo.getInstance().accountInfo.userName = userName;
                                APPDataInfo.getInstance().accountInfo.passWord = passWord;
                                APPDataInfo.getInstance().accountInfo.userID = resp.userID;
                                APPDataInfo.getInstance().accountInfo.userPic = resp.userPic;
                                APPDataInfo.getInstance().accountInfo.userPthone = resp.user_phone;
                                APPDataInfo.getInstance().accountInfo.userSign = resp.user_signature;
                                APPDataInfo.getInstance().accountInfo.version = resp.version;
                                APPDataInfo.getInstance().accountInfo.apkURL = resp.apk_url;
                                APPDataInfo.getInstance().accountInfo.epValue = resp.epValue;

                                DBProcMgr.getInstance().saveAccountInfo(APPDataInfo.getInstance().accountInfo);
                            }

                            mOnSendMsgListener.onSendNextMsg(msg);
                        }
                        else
                        {
                            msg.what = SignalEventDef.SE_LOGIN_FAILURE;
                            msg.obj = "登录失败";
                            mOnSendMsgListener.onSendNextMsg(msg);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        Message msg = new Message();
                        msg.what = SignalEventDef.SE_LOGIN_FAILURE;
                        msg.obj = "服务器异常";
                        mOnSendMsgListener.onSendNextMsg(msg);
                    }
                });
    }
}
