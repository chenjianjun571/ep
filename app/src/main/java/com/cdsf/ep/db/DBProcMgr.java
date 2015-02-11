package com.cdsf.ep.db;

import android.content.Context;
import android.util.Log;

import com.cdsf.ep.bean.AccountInfo;
import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.bean.ProjectInfo;
import com.cdsf.ep.bean.SrvInfo;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

import java.util.List;

/**
 * Created by chenjianjun on 14-11-21.
 * <p/>
 * Beyond their own, let the future
 */
public class DBProcMgr {

    public static final String TAG = DBProcMgr.class.getSimpleName();
    private static DBProcMgr ourInstance = new DBProcMgr();
    private static DbUtils _mdb;
    private String nullStr = "";

    /**
     * 初期化环境
     *
     * @param context 上下文
     * @return false:初期化失败 true:初期化成功
     */
    public static boolean initEnv(Context context) {
        _mdb = DbUtils.create(context);
        return _mdb != null;
    }

    /**
     * 返回单例
     *
     * @return 返回单例
     */
    public static DBProcMgr getInstance() {
        return ourInstance;
    }

    /**
     * 构造函数
     */
    private DBProcMgr() {}

    /**
     * 删除数据库，在版本更新时使用
     */
    public void dropDB() {

        try {

            //_mdb.dropTable(AccountInfo.class);
            _mdb.dropTable(ProjectInfo.class);
            _mdb.dropTable(MatterInfo.class);
            _mdb.dropTable(MatterReplyInfo.class);
            _mdb.dropTable(PersonnelInfo.class);

            //_mdb.dropDb();

        } catch (DbException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public boolean saveAccountInfo(AccountInfo accountInfo) {

        try {

            // 清除数据库数据
            _mdb.dropTable(AccountInfo.class);

            // 存储新数据
            _mdb.saveOrUpdate(accountInfo);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public boolean delAccountInfo(AccountInfo accountInfo) {

        try {

            // 清除数据库数据
            _mdb.delete(accountInfo);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public AccountInfo queryAccountInfo() {

        try {

            com.lidroid.xutils.db.sqlite.Selector selector;
            selector = Selector.from(AccountInfo.class);

            return _mdb.findFirst(selector);

        } catch (DbException e) {
            Log.e(TAG, e.getMessage());

            return null;
        }

    }

    public boolean saveOneProjectInfo(ProjectInfo projectInfo) {

        try {

            if(projectInfo == null) {
                return true;
            }

            // 存储新数据
            _mdb.replace(projectInfo);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public boolean saveProjectInfos(List<ProjectInfo> projectInfos) {

        try {

            // 清除数据库数据
            _mdb.dropTable(ProjectInfo.class);

            if(projectInfos == null) {
                return true;
            }

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public List<ProjectInfo> queryProjectInfo(String sqlCondition) {

        try {

            com.lidroid.xutils.db.sqlite.Selector selector;
            if (sqlCondition == null || sqlCondition.equals(nullStr)) {
                selector = Selector.from(ProjectInfo.class);
            } else {
                selector = Selector.from(ProjectInfo.class).expr(sqlCondition);
            }

            return _mdb.findAll(selector);

        } catch (DbException e) {
            Log.e(TAG, e.getMessage());

            return null;
        }

    }

//    public ProjectInfo queryOneProjectInfo(String sqlCondition) {
//
//        try {
//
//            com.lidroid.xutils.db.sqlite.Selector selector;
//            if (sqlCondition == null || sqlCondition.equals(nullStr)) {
//                selector = Selector.from(ProjectInfo.class);
//            } else {
//                selector = Selector.from(ProjectInfo.class).expr(sqlCondition);
//            }
//
//            return _mdb.findFirst(selector);
//
//        } catch (DbException e) {
//            Log.e(TAG, e.getMessage());
//
//            return null;
//        }
//
//    }

    public boolean delOneProjectInfo(ProjectInfo projectInfo) {

        try {

            if(projectInfo == null) {
                return true;
            }

            _mdb.delete(ProjectInfo.class, WhereBuilder.b("projectID", "==", projectInfo.projectID));
            _mdb.delete(PersonnelInfo.class, WhereBuilder.b("projectID", "==", projectInfo.projectID));

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }


    public boolean savePersonnelInfo(List<PersonnelInfo> personnelInfos) {

        try {

            if(personnelInfos == null) {
                return true;
            }

            _mdb.saveAll(personnelInfos);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }

    }

    public boolean updatePersonnelInfo(int projectid, List<PersonnelInfo> personnelInfos) {

        try {

            if(personnelInfos == null) {
                return true;
            }

            _mdb.delete(PersonnelInfo.class, WhereBuilder.b("projectID", "==", projectid));
            _mdb.saveAll(personnelInfos);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }

    }

    public List<PersonnelInfo> queryPersonnelInfo(String sqlCondition) {

        try {
            com.lidroid.xutils.db.sqlite.Selector selector;
            if (sqlCondition == null || sqlCondition.equals(nullStr)) {
                selector = Selector.from(PersonnelInfo.class);
            } else {
                selector = Selector.from(PersonnelInfo.class).expr(sqlCondition);
            }

            return _mdb.findAll(selector);

        } catch (DbException e) {
            Log.e(TAG, e.getMessage());

            return null;
        }

    }

    public boolean saveMatterInfo(int projectID, List<MatterInfo> matterInfos, int type) {

        try {

            if (type == 0) {
                // 全部更新
                _mdb.delete(MatterInfo.class, WhereBuilder.b("projectID", "==", projectID));
                if(matterInfos != null) {
                    // 存储新数据
                    _mdb.saveAll(matterInfos);
                }
            }
            else {
                if(matterInfos != null) {
                    // 加载更多
                    _mdb.saveOrUpdateAll(matterInfos);
                }
            }

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public boolean saveOneMatterInfo(MatterInfo matterInfo ) {

        try {

            _mdb.saveOrUpdate(matterInfo);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public boolean updateMatterInfo(MatterInfo matterInfo) {

        try {

            _mdb.saveOrUpdate(matterInfo);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    /**
     *
     * @param sqlCondition 条件，比如时间段在00:00到23:00之间，版本号=“1.0”，node=“test1”，
     *                     可以这样传输参数：“
     *                     periodStart <= strftime('%H:%M','now') AND
     *                     periodEnd >= strftime('%H:%M','now') AND
     *                     version = ‘1.0’
     *                     AND node = ‘test’
     *                     ”
     * @return 失败返回null
     */
    public List<MatterInfo> queryMatterInfo(String sqlCondition) {

        try {
            com.lidroid.xutils.db.sqlite.Selector selector;
            if (sqlCondition == null || sqlCondition.equals(nullStr)) {
                selector = Selector.from(MatterInfo.class);
            } else {
                selector = Selector.from(MatterInfo.class).expr(sqlCondition);
            }

            List<MatterInfo> matterInfos = _mdb.findAll(selector);
            if (matterInfos == null) {
                return null;
            }

            for (MatterInfo matterInfo:matterInfos) {

                // 取最新的一条
                matterInfo.matterReplyInfo = _mdb.findFirst(
                        Selector.from(MatterReplyInfo.class)
                                .where("matterID", "=", matterInfo.matterID).orderBy("replayID", true));

            }

            return matterInfos;

        } catch (DbException e) {
            Log.e(TAG, e.getMessage());

            return null;
        }

    }

    public boolean delOneMatterInfo(MatterInfo matterInfo) {

        try {

            if(matterInfo == null) {
                return true;
            }

            _mdb.delete(MatterInfo.class, WhereBuilder.b("matterID", "==", matterInfo.matterID));
            _mdb.delete(MatterReplyInfo.class, WhereBuilder.b("matterID", "==", matterInfo.matterID));

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public boolean saveMatterReplyInfo(String matterID, List<MatterReplyInfo> matterReplyInfoList) {

        try {

            if(matterReplyInfoList == null) {
                return true;
            }

//            for (MatterReplyInfo matterReplyInfo:matterReplyInfoList) {
//                // 存储新数据
//                _mdb.saveOrUpdate(matterReplyInfo);
//            }
            _mdb.saveOrUpdateAll(matterReplyInfoList);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public boolean saveMatterReplyInfo(MatterReplyInfo matterReplyInfo) {

        try {

            // 存储新数据
            _mdb.save(matterReplyInfo);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    /**
     *
     * @param sqlCondition 条件，比如时间段在00:00到23:00之间，版本号=“1.0”，node=“test1”，
     *                     可以这样传输参数：“
     *                     periodStart <= strftime('%H:%M','now') AND
     *                     periodEnd >= strftime('%H:%M','now') AND
     *                     version = ‘1.0’
     *                     AND node = ‘test’
     *                     ”
     * @return 失败返回null
     */
    public List<MatterReplyInfo> queryMatterReplyInfo(String sqlCondition) {

        try {
            com.lidroid.xutils.db.sqlite.Selector selector;
            if (sqlCondition == null || sqlCondition.equals(nullStr)) {
                selector = Selector.from(MatterReplyInfo.class).orderBy("replayID");
            } else {
                selector = Selector.from(MatterReplyInfo.class).expr(sqlCondition).orderBy("replayID");
            }

            return _mdb.findAll(selector);

        } catch (DbException e) {
            Log.e(TAG, e.getMessage());

            return null;
        }

    }

//    public boolean savePersonalInfos(List<PersonalInfo> personalInfos) {
//
//        try {
//
//            // 清除数据库数据
//            _mdb.dropTable(PersonalInfo.class);
//
//            if(personalInfos == null) {
//                return true;
//            }
//
//            // 存储新数据
//            for (PersonalInfo personalInfo : personalInfos) {
//                _mdb.saveOrUpdate(personalInfo);
//            }
//
//            return true;
//
//        } catch (DbException e) {
//
//            Log.e(TAG, e.getMessage());
//
//            return false;
//        }
//    }
//
//    public List<PersonalInfo> queryPersonalInfo(String sqlCondition) {
//
//        try {
//
//            com.lidroid.xutils.db.sqlite.Selector selector;
//            if (sqlCondition == null || sqlCondition.equals(nullStr)) {
//                selector = Selector.from(PersonalInfo.class);
//            } else {
//                selector = Selector.from(PersonalInfo.class).expr(sqlCondition);
//            }
//
//            return _mdb.findAll(selector);
//
//        } catch (DbException e) {
//            Log.e(TAG, e.getMessage());
//
//            return null;
//        }
//
//    }

    public boolean saveSrvInfo(SrvInfo srvInfo) {

        try {

            _mdb.dropTable(SrvInfo.class);
            _mdb.saveOrUpdate(srvInfo);

            return true;

        } catch (DbException e) {

            Log.e(TAG, e.getMessage());

            return false;
        }
    }

    public SrvInfo querySrvInfo() {

        try {

            com.lidroid.xutils.db.sqlite.Selector selector;
            selector = Selector.from(SrvInfo.class);

            return _mdb.findFirst(selector);

        } catch (DbException e) {
            Log.e(TAG, e.getMessage());

            return null;
        }

    }
}
