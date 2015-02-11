package com.cdsf.ep.process;

import android.util.Log;

import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.bean.ProjectInfo;
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
 * Created by chenjianjun on 14-12-2.
 * <p/>
 * Beyond their own, let the future
 */
public class ProgectProcess {

    public static final String TAG = MatterProcess.class.getSimpleName();

    public interface OnResultListener {

        public static final int SUCCESS = 0;// 成功
        public static final int FAILURE = 1;// 失败

        void onQueryResult(int result, ArrayList<ProjectInfo> projectInfoArrayList);

        void onDelResult(int result, ProjectInfo progectInfo);

        void onModResult(int result, ProjectInfo progectInfo);

        void onAddResult(int result, ProjectInfo progectInfo);

        // 人员查询结果列表
        void onQueryPersonalsResult(int result, ArrayList<PersonnelInfo> personalInfoArrayList);

        // 人员列表查询结果列表
        void onQueryPersonalsListResult(int result, ArrayList<PersonnelInfo> personalInfoArrayList);

        // 头像上传结果回调
        void onPicUploadResult(int result, String picID);
    }

    OnResultListener onResultListener;

    public void setResultListener(OnResultListener l) {
        onResultListener = l;
    }

    /**
     * 项目信息查询
     */
    public void progectQueryReq() {

        String url = APPDataInfo.BASE_URL + APPDataInfo.PROGECT_QUERY_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
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

                        ArrayList<ProjectInfo> progectInfos = JsonParse.parseProgectQueryResp(responseInfo.result);
                        // 解析应答
                        if (progectInfos != null) {
                            onResultListener.onQueryResult(OnResultListener.SUCCESS, progectInfos);
                        } else {
                            onResultListener.onQueryResult(OnResultListener.FAILURE, null);
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onQueryResult(OnResultListener.FAILURE, null);
                    }
                });
    }

    /**
     * 项目删除请求
     *
     * @param progectInfo
     */
    public void projectDelReq(final ProjectInfo progectInfo) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.PROGECT_DEL_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("pids", progectInfo.projectID);
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
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
                                onResultListener.onDelResult(OnResultListener.SUCCESS, progectInfo);
                                // 删除数据库信息
                                DBProcMgr.getInstance().delOneProjectInfo(progectInfo);
                            }
                            else {
                                onResultListener.onDelResult(OnResultListener.FAILURE, null);
                            }

                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onDelResult(OnResultListener.FAILURE, null);
                    }
                });
    }

    /**
     * 项目修改请求
     *
     * @param progectInfo
     */
    public void projectModReq(final ProjectInfo progectInfo, final List<PersonnelInfo> projectUserInfoArrayList) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.PROGECT_MOD_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("project_id", progectInfo.projectID);
            String userIDs = "";
            for (PersonnelInfo personnelInfo:projectUserInfoArrayList) {
                if (!userIDs.equals("")) {
                    userIDs += ",";
                }
                userIDs += personnelInfo.userID;
            }
            jsonStr.put("ref_user_ids", userIDs);
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
                                // 更新数据库信息
                                DBProcMgr.getInstance().saveOneProjectInfo(progectInfo);
                                DBProcMgr.getInstance().updatePersonnelInfo(progectInfo.projectID, projectUserInfoArrayList);
                                onResultListener.onModResult(OnResultListener.SUCCESS, progectInfo);
                            }
                            else {
                                onResultListener.onModResult(OnResultListener.FAILURE, null);
                            }

                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onModResult(OnResultListener.FAILURE, null);
                    }

                });
    }


    /**
     * 项目添加请求
     *
     * @param progectInfo
     */
    public void projectAddReq(final ProjectInfo progectInfo, List<PersonnelInfo> personnelInfoList) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.PROGECT_ADD_REQ_URL;
        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("pname", progectInfo.projectName);
            jsonStr.put("pcontent", progectInfo.projectIntroduction);
            jsonStr.put("user_id", APPDataInfo.getInstance().accountInfo.userID);
            if (progectInfo.projectPic.equals("")) {
                jsonStr.put("pimgid", "-1");
            } else {
                jsonStr.put("pimgid", progectInfo.projectPic);
            }

            String ids = "";
            for (PersonnelInfo personnelInfo:personnelInfoList) {

                if (!ids.equals("")) {
                    ids += ",";
                }

                ids += personnelInfo.userID;
            }
            jsonStr.put("ref_user_ids", ids);

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
                                onResultListener.onAddResult(OnResultListener.SUCCESS, progectInfo);
                            }
                            else {
                                onResultListener.onAddResult(OnResultListener.FAILURE, null);
                            }

                        } else {
                            onResultListener.onAddResult(OnResultListener.FAILURE, null);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onAddResult(OnResultListener.FAILURE, null);
                    }
                });
    }

    /**
     * 人员查询
     */
    public void projectQueryPersonalsReq() {

        String url = APPDataInfo.BASE_URL + APPDataInfo.USER_QUERY_REQ_URL;

        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 5);// 配置超时时间?
        http.send(HttpRequest.HttpMethod.POST,
                url,
                null,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        ArrayList<PersonnelInfo> projectUserInfos = JsonParse.parseQueryUserInfoResp(responseInfo.result);
                        if (projectUserInfos != null) {
                            onResultListener.onQueryPersonalsResult(OnResultListener.SUCCESS, projectUserInfos);
                        } else {
                            onResultListener.onQueryPersonalsResult(OnResultListener.FAILURE, null);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onQueryPersonalsResult(OnResultListener.FAILURE, null);
                    }
                });
    }

    /**
     * 人员查询
     */
    public void projectQueryPersonalsListReq(final ProjectInfo projectInfo) {

        String url = APPDataInfo.BASE_URL + APPDataInfo.USER_QUERY_REQ_URL;

        RequestParams params = new RequestParams();
        JSONObject jsonStr = new JSONObject();

        try {
            jsonStr.put("project_id", projectInfo.projectID);
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

                        ArrayList<PersonnelInfo> projectUserInfos = JsonParse.parseQueryUserInfoResp(responseInfo.result, projectInfo);
                        if (projectUserInfos != null) {
                            onResultListener.onQueryPersonalsListResult(OnResultListener.SUCCESS, projectUserInfos);
                        } else {
                            onResultListener.onQueryPersonalsListResult(OnResultListener.FAILURE, null);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String errormsg) {
                        onResultListener.onQueryPersonalsListResult(OnResultListener.FAILURE, null);
                    }
                });
    }

    /**
     * 头像上传接口
     */
    public void projectPicUploadReq(final String filepath) {

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
