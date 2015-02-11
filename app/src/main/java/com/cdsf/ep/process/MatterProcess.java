package com.cdsf.ep.process;

import android.util.Log;

import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.bean.ProjectInfo;
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
 * Created by chenjianjun on 14-11-27.
 * <p/>
 * Beyond their own, let the future
 */
public class MatterProcess {

    public static final String TAG = MatterProcess.class.getSimpleName();

    public interface OnResultListener {

        public static final int SUCCESS = 0;// 成功
        public static final int FAILURE = 1;// 失败

        /**
         * 事项请求结果回调
         */
        void onMatterReqResult(int result, int reqType, List<MatterInfo> matterInfoArrayList);

        /**
         * 事项修改状态请求结果回调
         */
        void onMatterStatusReqResult(int result, MatterInfo matterInfo);

        /**
         * 事项修改进度请求结果回调
         */
        void onMatterProcessReqResult(int result, MatterInfo matterInfo);

        /**
         * 回复结果数据提交结果回调
         */
        void onMatterReplayReqResult(int result, MatterInfo matterInfo, MatterReplyInfo matterReplyInfo);

        // 人员查询结果列表
        void onQueryPersonalsResult(int result, ArrayList<PersonnelInfo> personalInfoArrayList);

        /**
         * 事项添加请求结果
         * @param result
         * @param matterInfo
         */
        void onMatterAddReqResult(int result, MatterInfo matterInfo);

        /**
         * 事项删除结果回调
         * @param result
         * @param matterInfo
         */
        void onMatterDelReqResult(int result, MatterInfo matterInfo);

        /**
         * 事项添加的时候声音上传接口
         * @param result
         * @param soundID
         */
        void onSoundUploadResult(int result, String soundID, final MatterInfo matterInfo, final String users);

        /**
         * 事项添加的时候图片上传接口
         * @param result
         * @param soundID
         */
        void onPicUploadResult(int result, String picID, final MatterInfo matterInfo, final String users);
    }

    OnResultListener mOnSendMsgListener;

    public void setResultListener(OnResultListener l) {
        mOnSendMsgListener = l;
    }

    /**
     * 事项请求
     * @param projectInfo 项目信息
     * @param start_num 0是表示重新加载 大于0是表示加载更多
     */
    public void matterReq(final ProjectInfo projectInfo, final int start_num) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.MATTER_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("project_id", projectInfo.projectID);
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
            if (start_num > 0) {
                jsonStr.put("row_num", start_num);
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

                        ArrayList<MatterInfo> matterInfoArrayList = JsonParse.parseMatterQueryResp(responseInfo.result, projectInfo);
                        if (matterInfoArrayList == null) {
                            mOnSendMsgListener.onMatterReqResult(OnResultListener.FAILURE, start_num, null);
                        } else {
                            mOnSendMsgListener.onMatterReqResult(OnResultListener.SUCCESS, start_num, matterInfoArrayList);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        if (start_num != 0) {
                            mOnSendMsgListener.onMatterReqResult(OnResultListener.FAILURE, 0, null);
                        }
                        else {
                            mOnSendMsgListener.onMatterReqResult(OnResultListener.FAILURE, 1, null);
                        }
                    }
                });
    }

    /**
     * 快速回复
     * @param matterInfo 事项信息
     * @param matterReplyInfo 回复内容
     */
    public void matterReplayReq(final MatterInfo matterInfo, final MatterReplyInfo matterReplyInfo) {

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
                                mOnSendMsgListener.onMatterReplayReqResult(OnResultListener.SUCCESS, matterInfo, matterReplyInfo);
                            }
                            else {
                                mOnSendMsgListener.onMatterReplayReqResult(OnResultListener.FAILURE, matterInfo, matterReplyInfo);
                            }

                        } else {
                            mOnSendMsgListener.onMatterReplayReqResult(OnResultListener.FAILURE, matterInfo, matterReplyInfo);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onMatterReplayReqResult(OnResultListener.FAILURE, matterInfo, matterReplyInfo);
                    }
                });
    }

    /**
     * 人员查询
     *
     * @param progectInfo
     */
    public void projectQueryPersonalsReq(final ProjectInfo progectInfo) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.USER_QUERY_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("project_id", progectInfo.projectID);
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

                        ArrayList<PersonnelInfo> projectUserInfos = JsonParse.parseQueryUserInfoResp(responseInfo.result,progectInfo);
                        if (projectUserInfos != null) {
                            mOnSendMsgListener.onQueryPersonalsResult(OnResultListener.SUCCESS, projectUserInfos);
                        } else {
                            mOnSendMsgListener.onQueryPersonalsResult(OnResultListener.FAILURE, null);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onQueryPersonalsResult(OnResultListener.FAILURE, null);
                    }
                });
    }


    /**
     * 事项添加
     */
    public void matterAddReq(final MatterInfo matterInfo, String users) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.MATTER_ADD_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("project_id", matterInfo.projectID);
            jsonStr.put("item_creater", APPDataInfo.getInstance().accountInfo.userID);
            jsonStr.put("item_content_type", matterInfo.matterType);
            jsonStr.put("item_progress", 0);
            jsonStr.put("item_status", 1);
            jsonStr.put("item_users", users);
            jsonStr.put("item_begin_time", matterInfo.matterStartTime);
            jsonStr.put("item_end_time", matterInfo.matterEndTime);
            jsonStr.put("item_content", matterInfo.matterContent);

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

                        Log.e("接口测试",responseInfo.result);

                        ComResp resp = new ComResp();
                        // 解析应答
                        if (JsonParse.parseResp(responseInfo.result, resp)) {

                            if (resp.result == 0) {
                                mOnSendMsgListener.onMatterAddReqResult(OnResultListener.SUCCESS, matterInfo);
                            }
                            else {
                                mOnSendMsgListener.onMatterAddReqResult(OnResultListener.FAILURE, matterInfo);
                            }

                        } else {
                            mOnSendMsgListener.onMatterAddReqResult(OnResultListener.FAILURE, null);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onQueryPersonalsResult(OnResultListener.FAILURE, null);
                    }
                });
    }


    /**
     * 事项删除请求
     */
    public void matterDelReq(final MatterInfo matterInfo) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.MATTER_DEL_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("item_ids", matterInfo.matterID);
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
                                mOnSendMsgListener.onMatterDelReqResult(OnResultListener.SUCCESS, matterInfo);
                            }
                            else {
                                mOnSendMsgListener.onMatterDelReqResult(OnResultListener.FAILURE, matterInfo);
                            }

                        } else {
                            mOnSendMsgListener.onMatterDelReqResult(OnResultListener.FAILURE, matterInfo);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onMatterDelReqResult(OnResultListener.FAILURE, matterInfo);
                    }
                });
    }

    /**
     * 状态修改请求
     * @param matterInfo 事项信息
     * @param status 事项状态
     */
    public void matterStatusReq(final MatterInfo matterInfo, final int status) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.MATTER_MOD_STATUS_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("item_id", matterInfo.matterID);
            jsonStr.put("item_status", status);
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
            jsonStr.put("flag", 1);
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
                                matterInfo.matterStatus = status;
                                if (status == CommonDef.MATTER_STATUS_INIT) {
                                    matterInfo.matterProcess = 0;
                                }
                                mOnSendMsgListener.onMatterStatusReqResult(OnResultListener.SUCCESS, matterInfo);
                            }
                            else {
                                mOnSendMsgListener.onMatterStatusReqResult(OnResultListener.FAILURE, matterInfo);
                            }

                        } else {
                            mOnSendMsgListener.onMatterStatusReqResult(OnResultListener.FAILURE, matterInfo);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onMatterStatusReqResult(OnResultListener.FAILURE, matterInfo);
                    }
                });
    }


    /**
     * 事项进度修改请求
     * @param matterInfo 事项信息
     * @param process 事项状态
     */
    public void matterProcessReq(final MatterInfo matterInfo, final int process) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.MATTER_MOD_PROCESS_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("item_id", matterInfo.matterID);
            jsonStr.put("item_progress", process);
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
            jsonStr.put("flag", 2);
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
                                matterInfo.matterProcess = process;
                                if (matterInfo.matterProcess == 100) {
                                    matterInfo.matterStatus = CommonDef.MATTER_STATUS_FINAL;
                                }
                                mOnSendMsgListener.onMatterProcessReqResult(OnResultListener.SUCCESS, matterInfo);
                            }
                            else {
                                mOnSendMsgListener.onMatterProcessReqResult(OnResultListener.FAILURE, matterInfo);
                            }

                        } else {
                            mOnSendMsgListener.onMatterProcessReqResult(OnResultListener.FAILURE, matterInfo);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onMatterProcessReqResult(OnResultListener.FAILURE, matterInfo);
                    }
                });
    }

    /**
     * 声音上传接口
     * @param filepath
     */
    public void soundUploadReq(final String filepath, final MatterInfo matterInfo, final String users) {

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
                        mOnSendMsgListener.onSoundUploadResult(OnResultListener.SUCCESS, responseInfo.result, matterInfo, users);
                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onSoundUploadResult(OnResultListener.FAILURE, null, null, null);
                    }
                });
    }

    public void picUploadReq(final String filepath, final MatterInfo matterInfo, final String users) {

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
                        mOnSendMsgListener.onPicUploadResult(OnResultListener.SUCCESS, responseInfo.result, matterInfo, users);
                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        mOnSendMsgListener.onPicUploadResult(OnResultListener.FAILURE, null, null, null);
                    }
                });
    }
}
