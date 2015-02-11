package com.cdsf.ep.protocol;

import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.bean.ProjectInfo;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by chenjianjun on 14-11-20.
 * <p/>
 * Beyond their own, let the future
 */
public class JsonParse {

    /**
     * // 普通Json数据解析
     * private void parseJson(String strResult) {
     * try {
     * JSONObject jsonObj = new JSONObject(strResult).getJSONObject("singer");
     * int id = jsonObj.getInt("id");
     * String name = jsonObj.getString("name");
     * String gender = jsonObj.getString("gender");
     * tvJson.setText("ID号"+id + ", 姓名：" + name + ",性别：" + gender);
     * } catch (JSONException e) {
     * System.out.println("Json parse error");
     * e.printStackTrace();
     * }
     * }
     * //解析多个数据的Json
     * private void parseJsonMulti(String strResult) {
     * try {
     * JSONArray jsonObjs = new JSONObject(strResult).getJSONArray("singers");
     * String s = "";
     * for(int i = 0; i < jsonObjs.length() ; i++){
     * JSONObject jsonObj = ((JSONObject)jsonObjs.opt(i))
     * .getJSONObject("singer");
     * int id = jsonObj.getInt("id");
     * String name = jsonObj.getString("name");
     * String gender = jsonObj.getString("gender");
     * s +=  "ID号"+id + ", 姓名：" + name + ",性别：" + gender+ "\n" ;
     * }
     * tvJson.setText(s);
     * } catch (JSONException e) {
     * System.out.println("Jsons parse error !");
     * e.printStackTrace();
     * }
     * }
     */

    public static boolean parseLoginResp(String strResult, LoginResp resp) {

        try {

            JSONObject jsonObj = new JSONObject(strResult);

            resp.result = jsonObj.getInt("status");
            if (resp.result != 0) {
                resp.errorMsg = jsonObj.getString("description");
            } else {
                resp.userID = jsonObj.getInt("user_id");
                resp.userPic = jsonObj.getString("user_pic_url");
                resp.user_phone = jsonObj.getString("user_phone");
                resp.user_signature = jsonObj.getString("user_signature");
                resp.version = jsonObj.getString("version");
                resp.apk_url = jsonObj.getString("apk_url");
                resp.epValue = jsonObj.getInt("level");
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static ArrayList<ProjectInfo> parseProgectQueryResp(String result) {

        // 项目信息列表
        ArrayList<ProjectInfo> projectInfoArrayList = new ArrayList<ProjectInfo>();

        try {

            JSONArray jsonObjs = new JSONArray(result);
            for (int i = 0; i < jsonObjs.length(); i++) {

                JSONObject jsonObj = ((JSONObject) jsonObjs.opt(i));
                ProjectInfo progectInfo = new ProjectInfo();
                progectInfo.isMsg = false;

                // 项目ID
                progectInfo.projectID = jsonObj.getInt("project_id");
                // 项目图标URL
                progectInfo.projectPic = jsonObj.getString("pic_url");
                // 项目名称
                progectInfo.projectName = jsonObj.getString("project_name");
                // 项目创建日期
                progectInfo.projectCreateTime = jsonObj.getString("create_time");
                // 项目创建人
                progectInfo.projectCreater = jsonObj.getString("project_creater");
                // 项目创建人的EP值
                progectInfo.projectCreaterEpValue = jsonObj.getInt("level");
                // 项目简介
                progectInfo.projectIntroduction = jsonObj.getString("project_content");

                projectInfoArrayList.add(progectInfo);
            }

            // 把项目信息保存下来
            DBProcMgr.getInstance().saveProjectInfos(projectInfoArrayList);

            return projectInfoArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<MatterInfo> parseMatterQueryResp(String result, ProjectInfo projectInfo) {

        ArrayList<MatterInfo> matterInfoArrayList = new ArrayList<MatterInfo>();

        try {

            JSONArray jsonObjs = new JSONArray(result);
            for (int i = 0; i < jsonObjs.length(); i++) {

                JSONObject jsonObj = ((JSONObject) jsonObjs.opt(i));

                MatterInfo matterInfo = new MatterInfo();

                // 项目ID
                matterInfo.projectID = projectInfo.projectID;
                // 事项ID
                matterInfo.matterID = jsonObj.getString("item_id");
                // 事项类型
                matterInfo.matterType = jsonObj.getInt("item_content_type");
                // 事项内容
                matterInfo.matterContent = jsonObj.getString("item_content");
                // 事项状态 参考CommonDef.MATTER_STATUS_INIT
                matterInfo.matterStatus = jsonObj.getInt("item_status");
                // 事项进度
                matterInfo.matterProcess = jsonObj.getInt("item_progress");
                // 事项开始时间
                matterInfo.matterStartTime = jsonObj.getString("item_begin_time");
                // 事项结束时间
                matterInfo.matterEndTime = jsonObj.getString("item_end_time");
                // 事项创建人头像URL
                matterInfo.matterCreateUserPic = jsonObj.getString("item_creater_url");
                // 事项创建人名称
                matterInfo.matterCreateUserName = jsonObj.getString("item_creater");
                // 事项创建人的EP值
                matterInfo.matterCreateEpValue = jsonObj.getInt("level");

                // 事项接收人员
                JSONObject user = (JSONObject)jsonObj.getJSONObject("item_refuser");
                matterInfo.matterRecvPersonnels = user.getString("uname");

                if (!jsonObj.isNull("lastestReply")) {
                    JSONObject replay = (JSONObject) jsonObj.getJSONObject("lastestReply");
                    if (replay != null) {
                        matterInfo.matterReplyInfo = new MatterReplyInfo();
                        // 事项ID
                        matterInfo.matterReplyInfo.matterID = matterInfo.matterID;
                        // 用户头像url
                        matterInfo.matterReplyInfo.replyUserPic = replay.getString("back_user_pic_url");
                        // 回复人名称
                        matterInfo.matterReplyInfo.replyUserNmae = replay.getString("back_user_name");
                        // 回复内容
                        matterInfo.matterReplyInfo.replyContent = replay.getString("back_msg");
                        // 回复时间
                        matterInfo.matterReplyInfo.replyDate = replay.getString("back_time");
                        // 回复类型
                        matterInfo.matterReplyInfo.replyType = replay.getInt("back_type");
                    }
                }

                matterInfoArrayList.add(matterInfo);
            }

            return matterInfoArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<MatterReplyInfo> parseMatterReplyInfoQueryResp(String result, MatterInfo matterInfo) {

        ArrayList<MatterReplyInfo> matterReplyInfoArrayList = new ArrayList<MatterReplyInfo>();

        try {

            JSONArray jsonObjs = new JSONArray(result);
            for (int i = 0; i < jsonObjs.length(); i++) {

                JSONObject jsonObj = ((JSONObject) jsonObjs.opt(i));

                MatterReplyInfo matterReplyInfo = new MatterReplyInfo();

                // 事项ID
                matterReplyInfo.matterID = matterInfo.matterID;
                // 回复人名称
                matterReplyInfo.replyUserNmae = jsonObj.getString("back_user_name");
                // 回复人头像URL
                matterReplyInfo.replyUserPic = jsonObj.getString("back_user_pic_url");
                // 回复内容
                matterReplyInfo.replyContent = jsonObj.getString("back_msg");
                // 回复时间
                matterReplyInfo.replyDate = jsonObj.getString("back_time");
                // 回复类型
                matterReplyInfo.replyType = jsonObj.getInt("back_type");
                // 回复ID
                matterReplyInfo.replayID = jsonObj.getInt("reply_id");
                // 回复人的EP值
                matterReplyInfo.replyEpValue = jsonObj.getInt("level");

                matterReplyInfoArrayList.add(matterReplyInfo);
            }

            return matterReplyInfoArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static boolean parseResp(String strResult, ComResp resp) {

        try {

            JSONObject jsonObj = new JSONObject(strResult);

            resp.result = jsonObj.getInt("status");
            if (resp.result != 0) {
                resp.errorMsg = jsonObj.getString("description");
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static ArrayList<PersonnelInfo> parseQueryUserInfoResp(String result, ProjectInfo projectInfo) {

        ArrayList<PersonnelInfo> personnelInfoArrayList = new ArrayList<PersonnelInfo>();

        try {

            JSONArray jsonObjs = new JSONArray(result);
            for (int i = 0; i < jsonObjs.length(); i++) {

                JSONObject jsonObj = ((JSONObject) jsonObjs.opt(i));

                PersonnelInfo personnelInfo = new PersonnelInfo();

                personnelInfo.projectID = projectInfo.projectID;
                personnelInfo.userID = jsonObj.getString("user_id");
                personnelInfo.userName = jsonObj.getString("user_name");
                personnelInfo.userPic = jsonObj.getString("user_pic");
                personnelInfo.userPhone = jsonObj.getString("user_phone");
                personnelInfo.userSign = jsonObj.getString("user_sign");
                personnelInfo.epValue = jsonObj.getInt("level");

                personnelInfoArrayList.add(personnelInfo);
            }

            return personnelInfoArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<PersonnelInfo> parseQueryUserInfoResp(String result) {

        ArrayList<PersonnelInfo> personnelInfoArrayList = new ArrayList<PersonnelInfo>();

        try {

            JSONArray jsonObjs = new JSONArray(result);
            for (int i = 0; i < jsonObjs.length(); i++) {

                JSONObject jsonObj = ((JSONObject) jsonObjs.opt(i));

                PersonnelInfo personnelInfo = new PersonnelInfo();

                personnelInfo.projectID = -1;
                personnelInfo.userID = jsonObj.getString("user_id");
                personnelInfo.userName = jsonObj.getString("user_name");
                personnelInfo.userPic = jsonObj.getString("user_pic");
                personnelInfo.userPhone = "";//jsonObj.getString("user_phone");
                personnelInfo.userSign = "";//jsonObj.getString("user_sign");
                personnelInfo.epValue = jsonObj.getInt("level");

                personnelInfoArrayList.add(personnelInfo);
            }

            return personnelInfoArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
