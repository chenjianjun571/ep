package com.cdsf.ep.process;

import android.util.Log;

import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.common.CommonDef;
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
 * Created by chenjianjun on 14-12-4.
 * <p/>
 * Beyond their own, let the future
 */
public class MatterInfoProcess {

    public static final String TAG = MatterInfoProcess.class.getSimpleName();

    public interface OnResultListener {

        public static final int SUCCESS = 0;// 成功
        public static final int FAILURE = 1;// 失败

        /**
         * 发送消息，让外部来处理逻辑
         * type CommonDef.REPLAY_LOAD_TYPE_INIT
         */
        <T> void onResult(int result, List<T> listinfo, int type);

        void onReplayReqResult(int result, MatterInfo matterInfo, MatterReplyInfo matterReplyInfo);

        void onSoundUploadResult(int result, String soundUrl);

        void onPicUploadResult(int result, String picUrl);
    }

    OnResultListener mOnSendMsgListener;

    public void setResultListener(OnResultListener l) {
        mOnSendMsgListener = l;
    }

    /**
     * 回复请求
     * @param matterInfo 事项信息
     * @param type 0:是加载更多历史数据 1:是加载更多新数据 2:加载最新的数据 3:加载最新的一条数据 当type是2或者3时，replay_id字段没有值
     * @param replayID 加载的(1的时候是起始 0的时候是结束）回复ID
     */
    public void matterInfoReplayReq(final MatterInfo matterInfo, final int type, int replayID) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.MATTER_INFO_REPLAY_MSG_QUERY_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("item_id", matterInfo.matterID);
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
            jsonStr.put("flag", type);
            if (type != CommonDef.REPLAY_LOAD_TYPE_INIT && type != CommonDef.REPLAY_LOAD_TYPE_NEW_ONE) {
                jsonStr.put("reply_id", replayID);
                Log.e("-----", "请求回复信息:replay_id["+replayID+"]");
            }

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

                        Log.e("-----", "请求回复信息:"+responseInfo.result);

                        ArrayList<MatterReplyInfo> matterReplyInfoArrayList = JsonParse.parseMatterReplyInfoQueryResp(responseInfo.result, matterInfo);
                        if (matterReplyInfoArrayList == null) {
                            mOnSendMsgListener.onResult(OnResultListener.FAILURE, null, type);
                        } else {
                            mOnSendMsgListener.onResult(OnResultListener.SUCCESS, matterReplyInfoArrayList, type);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onResult(OnResultListener.FAILURE, null, type);
                    }
                });
    }

    /**
     * 声音上传接口
     * @param filepath
     */
    public void soundUploadReq(final String filepath) {

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

                        mOnSendMsgListener.onSoundUploadResult(OnResultListener.SUCCESS, responseInfo.result);

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onSoundUploadResult(OnResultListener.FAILURE, null);
                    }
                });
    }

    /**
     * 回复
     * @param matterInfo 事项信息
     * @param matterReplyInfo 回复内容
     */
    public void replayReq(final MatterInfo matterInfo, final MatterReplyInfo matterReplyInfo) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.MATTER_REPLAY_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("item_id", matterInfo.matterID);
            jsonStr.put("flag", "1");
            jsonStr.put("reply_type", matterReplyInfo.replyType);
            jsonStr.put("reply_content", matterReplyInfo.replyContent);
            jsonStr.put("reply_uid", APPDataInfo.getInstance().accountInfo.userID);
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

                        ComResp resp = new ComResp();
                        // 解析应答
                        if (JsonParse.parseResp(responseInfo.result, resp)) {

                            if (resp.result == 0) {
                                mOnSendMsgListener.onReplayReqResult(OnResultListener.SUCCESS, matterInfo, matterReplyInfo);
                            }
                            else {
                                mOnSendMsgListener.onReplayReqResult(OnResultListener.FAILURE, matterInfo, matterReplyInfo);
                            }

                        } else {
                            mOnSendMsgListener.onReplayReqResult(OnResultListener.FAILURE, matterInfo, matterReplyInfo);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onReplayReqResult(OnResultListener.FAILURE, matterInfo, matterReplyInfo);
                    }
                });
    }

    /**
     * 图片上传接口
     * @param filepath
     */
    public void picUploadReq(final String filepath) {

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

                        mOnSendMsgListener.onPicUploadResult(OnResultListener.SUCCESS, responseInfo.result);

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onPicUploadResult(OnResultListener.FAILURE, null);
                    }
                });
    }
}
