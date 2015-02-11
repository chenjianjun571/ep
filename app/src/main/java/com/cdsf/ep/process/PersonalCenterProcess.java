package com.cdsf.ep.process;

import android.util.Log;

import com.cdsf.ep.bean.AccountInfo;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.protocol.ComResp;
import com.cdsf.ep.protocol.JsonParse;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjianjun on 14-12-22.
 * <p/>
 * Beyond their own, let the future
 */
public class PersonalCenterProcess {

    public static final String TAG = PersonalCenterProcess.class.getSimpleName();

    public interface OnResultListener {

        public static final int SUCCESS = 0;// 成功
        public static final int FAILURE = 1;// 失败

        // 修改结果回调
        void onModResult(int result, AccountInfo accountInfo);

        // 头像上传结果回调
        void onPicUploadResult(int result, String picID);

        // 注销结果回调
        void onLogoutResult(int result, String msg);
    }

    OnResultListener onResultListener;

    public void setResultListener(OnResultListener l) {
        onResultListener = l;
    }

    /**
     * 注销
     */
    public void logout() {

        String url = APPDataInfo.BASE_URL + APPDataInfo.LOGOUT_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
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

                        ComResp resp = new ComResp();
                        // 解析应答
                        if (JsonParse.parseResp(responseInfo.result, resp)) {

                            if (resp.result != 0) {
                                onResultListener.onLogoutResult(OnResultListener.FAILURE, "注销失败");
                                return;
                            }

                        }

                        onResultListener.onLogoutResult(OnResultListener.SUCCESS, "");
                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onLogoutResult(OnResultListener.FAILURE, errormsg);
                    }
                });
    }

    public void userinfoModReq(final AccountInfo accountInfo) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.USER_INFO_MOD_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("user_id", accountInfo.userID);
            if (accountInfo.userPic != null && !accountInfo.userPic.equals("")) {
                jsonStr.put("user_pic_id", accountInfo.userPic);
            }
            jsonStr.put("user_phone", accountInfo.userPthone);
            jsonStr.put("user_signature", accountInfo.userSign);
            jsonStr.put("old_password", accountInfo.passWord);
            jsonStr.put("password", accountInfo.passWord);
            params.setBodyEntity(new StringEntity(jsonStr.toString(), "utf-8"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 5);// 配置超时时间?
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

                        Log.e("接口测试", responseInfo.result);

                        try {
                            JSONObject jsonObj = new JSONObject(responseInfo.result);

                            int result = jsonObj.getInt("status");
                            if (result == 0) {
                                String userPic = jsonObj.getString("user_pic_url");
                                if (userPic != null && !userPic.equals("")) {
                                    APPDataInfo.getInstance().accountInfo.userPic = userPic;
                                    accountInfo.userPic = userPic;
                                } else {
                                    accountInfo.userPic = APPDataInfo.getInstance().accountInfo.userPic;
                                }
                                // 保存到内存
                                APPDataInfo.getInstance().accountInfo.userPthone = accountInfo.userPthone;
                                APPDataInfo.getInstance().accountInfo.userSign = accountInfo.userSign;
                                APPDataInfo.getInstance().accountInfo.userName = accountInfo.userName;
                                APPDataInfo.getInstance().accountInfo.passWord = accountInfo.passWord;
                                APPDataInfo.getInstance().accountInfo.userID = accountInfo.userID;
                                // 数据库信息保存
                                DBProcMgr.getInstance().saveAccountInfo(accountInfo);

                                onResultListener.onModResult(OnResultListener.SUCCESS, accountInfo);
                            } else {
                                onResultListener.onModResult(OnResultListener.FAILURE, null);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            onResultListener.onModResult(OnResultListener.FAILURE, null);
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onModResult(OnResultListener.FAILURE, null);
                    }
                });
    }

    /**
     * 头像上传接口
     */
    public void userPicUploadReq(final String filepath) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.PIC_UPLOAD_REQ_URL;

        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", String.valueOf(APPDataInfo.getInstance().accountInfo.userID));
        params.addBodyParameter("file", new File(filepath));

        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 5);// 配置超时时间?
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
                        onResultListener.onPicUploadResult(OnResultListener.SUCCESS, responseInfo.result);
                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onPicUploadResult(OnResultListener.FAILURE, null);
                    }
                });
    }
}
