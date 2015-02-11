package com.cdsf.ep.process;

import android.util.Log;

import com.cdsf.ep.master.APPDataInfo;
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
 * Created by chenjianjun on 14-12-31.
 * <p/>
 * Beyond their own, let the future
 */
public class ChannelIdSyncProcess {

    public static final String TAG = MatterProcess.class.getSimpleName();

    public interface OnResultListener {

        public static final int SUCCESS = 0;// 成功
        public static final int FAILURE = 1;// 失败

        void onSyncResult(int result);
    }

    OnResultListener onResultListener;

    public void setResultListener(OnResultListener l) {
        onResultListener = l;
    }

    /**
     * 项目信息查询
     */
    public void syncReq(String appid, String userId, String channelId, String requestId) {

        Log.e(TAG, "发送通道同步请求........");

        String url = APPDataInfo.BASE_URL + APPDataInfo.CHANNEL_SYNC_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
            jsonStr.put("app_id", appid);
            jsonStr.put("push_user_id", userId);
            jsonStr.put("channel_id", channelId);
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
                        onResultListener.onSyncResult(OnResultListener.SUCCESS);
                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onSyncResult(OnResultListener.FAILURE);
                    }
                });
    }

}
