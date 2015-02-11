package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.bean.ProjectInfo;
import com.cdsf.ep.common.CommonDef;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.ProgectProcess;
import com.squareup.picasso.Picasso;
import com.tool.unit.BaseAdapterHelper;
import com.tool.unit.QuickAdapter;
import com.tool.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProgectInfoActivity extends Activity implements View.OnClickListener,
        ProgectProcess.OnResultListener {

    private static final String TAG = ProgectInfoActivity.class.getSimpleName();
    private Context context;

    // 返回按钮
    private Button btnBack;
    // 保存按钮
    private Button btnSave;
    // 是否有修改
    private Boolean isModFlg = false;

    // 人员列表
    private GridView gvPersonals;
    // 视频网格列表数据源
    protected QuickAdapter<PersonnelInfo> mVideoPersonalsAdapter;
    // 用户人员剔重
    private HashMap<String, PersonnelInfo> personalInfoHashMap;

    // 项目数据
    private ProjectInfo mProjectInfo;

    // 项目图标
    private ImageView imgProjectPic;
    // 项目名称
    private TextView txProjectNmae;

    // 项目创建人
    private TextView txProjectCreater;
    // 项目创建日期
    private TextView txProjectCreateTime;
    // 项目简介
    private TextView txProjectContent;

    // 逻辑处理过程
    private ProgectProcess progectInfoProcess;

    // 数据加载对话框
    LoadingDialog mLoadingDialog;

    // 是否创建人查看
    private boolean isCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progect_info);

        context = this;

        mProjectInfo = (ProjectInfo) getIntent().getSerializableExtra("projectInfo");
        progectInfoProcess = new ProgectProcess();
        progectInfoProcess.setResultListener(this);
        mLoadingDialog = new LoadingDialog(context);

        // 如果是项目创建人看详情界面，会展示人员添加和保存按钮
        if (!mProjectInfo.projectCreater.equals(APPDataInfo.getInstance().accountInfo.userName)) {
            isCreate = false;
        } else {
            isCreate = true;
        }

        initView();

        initData();
    }

    private void initView() {

        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        // 项目头像URL
        imgProjectPic = (ImageView) findViewById(R.id.ic_pic);
        Picasso.with(context).load(mProjectInfo.projectPic)
                .placeholder(R.drawable.def_project_pic)
                .error(R.drawable.def_project_pic)
                .into(imgProjectPic);

        // 项目名称
        txProjectNmae = (TextView) findViewById(R.id.tx_project_name);
        txProjectNmae.setText(mProjectInfo.projectName);

        // 创建人
        txProjectCreater = (TextView) findViewById(R.id.tx_project_creater);
        txProjectCreater.setText(mProjectInfo.projectCreater);

        // 创建日期
        txProjectCreateTime = (TextView) findViewById(R.id.tx_project_create_time);
        txProjectCreateTime.setText(mProjectInfo.projectCreateTime);

        // 项目简介
        txProjectContent = (TextView) findViewById(R.id.tx_project_content);
        txProjectContent.setText(mProjectInfo.projectIntroduction);

        gvPersonals = (GridView) findViewById(R.id.gv_progect_info_personnels);
        personalInfoHashMap = new HashMap<String, PersonnelInfo>();
        mVideoPersonalsAdapter = new QuickAdapter<PersonnelInfo>(this, R.layout.personal_grid_item) {
            @Override
            protected void convert(BaseAdapterHelper helper, PersonnelInfo item) {

                if (item.isEnd) {
                    // 是最后一个
                    helper.setImageLoaclResID(R.id.cimg_personal_pic, R.drawable.add_user);
                    helper.setText(R.id.tx_personal_name, "");
                    helper.setVisible(R.id.img_item_del, false);
                } else {
                    // 人员头像
                    helper.setImageUrl(R.id.cimg_personal_pic,
                            item.userPic,
                            R.drawable.def_pic,
                            R.drawable.def_pic);

                    // 人员名称
                    helper.setText(R.id.tx_personal_name, item.userName);

                    if (item.isDel) {

                        if (!isCreate) {
                            helper.setVisible(R.id.img_item_del, false);
                        } else {
                            helper.setVisible(R.id.img_item_del, true);
                        }

                    } else {
                        helper.setVisible(R.id.img_item_del, false);
                    }
                }

                if (isCreate) {

                    final ImageView delView = helper.getView(R.id.img_item_del);
                    delView.setTag(helper);
                    delView.setTag(R.id.project_grid_view_item_user_info, item);

                    delView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 删除
                            PersonnelInfo personnelInfo = (PersonnelInfo) v.getTag(R.id.project_grid_view_item_user_info);

                            remove(personnelInfo);
                            personalInfoHashMap.remove(personnelInfo.userID);

                            // 设置修改标志
                            isModFlg = true;
                        }
                    });

                }

            }
        };
        gvPersonals.setAdapter(mVideoPersonalsAdapter);

        // 如果是项目创建人看详情界面，会展示人员添加和保存按钮,以及人员的添加删除功能
        if (!isCreate) {
            btnSave.setVisibility(View.GONE);
        } else {
            btnSave.setVisibility(View.VISIBLE);
            // 设置单击监听事件
            gvPersonals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    PersonnelInfo personalInfo = (PersonnelInfo) mVideoPersonalsAdapter.getItem(position);
                    if (!personalInfo.isEnd) {

                        // 如果之前是删除，用户点击其他区域取消删除状态
                        if (personalInfo.isDel) {
                            personalInfo.isDel = false;
                            mVideoPersonalsAdapter.notifyDataSetChanged();
                        }

                        // 人员不支持点击
                        return;

                    } else {
                        // 添加人员
                        if (!mLoadingDialog.isShowing()) {
                            mLoadingDialog.setMessage("数据请求中，请稍后....");
                            mLoadingDialog.show();
                        }

                        // 请求人员列表
                        progectInfoProcess.projectQueryPersonalsReq();
                    }

                }
            });

            // 设置长按监听事件
            gvPersonals.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    PersonnelInfo projectUserInfo = (PersonnelInfo) mVideoPersonalsAdapter.getItem(position);
                    if (projectUserInfo.isEnd) {
                        // 最后一个是功能按钮，不支持长按
                        return false;
                    }

                    projectUserInfo.isDel = true;
                    mVideoPersonalsAdapter.notifyDataSetChanged();

                    return true;

                }
            });
        }
    }

    private void initData() {

        do {
            personalInfoHashMap.clear();
            mVideoPersonalsAdapter.clear();

            if(isCreate) {
                // 如果是创建人进来，可以添加删除人员
                PersonnelInfo personnelInfo = new PersonnelInfo();
                personnelInfo.isEnd = true;
                mVideoPersonalsAdapter.addFront(personnelInfo);
            }

            // 添加人员
            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.setMessage("数据请求中，请稍后....");
                mLoadingDialog.show();
            }

            // 请求人员列表
            progectInfoProcess.projectQueryPersonalsListReq(mProjectInfo);

        } while (false);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_back: {
                ProgectInfoActivity.this.finish();
                break;
            }
            case R.id.btn_save: {

                if (!isModFlg) {
                    break;
                }

                isModFlg = false;

                // 添加人员
                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.setMessage("数据请求中，请稍后....");
                    mLoadingDialog.show();
                }

                ArrayList<PersonnelInfo> projectUserInfoArrayList = new ArrayList<PersonnelInfo>();
                for (int i = 0; i < mVideoPersonalsAdapter.getCount(); ++i) {
                    if (mVideoPersonalsAdapter.getItem(i).isEnd) {
                        continue;
                    }

                    projectUserInfoArrayList.add(mVideoPersonalsAdapter.getItem(i));
                }

                progectInfoProcess.projectModReq(mProjectInfo, projectUserInfoArrayList);

                break;
            }
        }
    }

    @Override
    public void onQueryResult(int result, ArrayList<ProjectInfo> projectInfoArrayList) {
    }

    @Override
    public void onDelResult(int result, ProjectInfo progectInfo) {
    }

    @Override
    public void onModResult(int result, ProjectInfo progectInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                Toast.makeText(getApplicationContext(),
                        "项目修改成功",
                        Toast.LENGTH_SHORT).show();

                break;

            }
            case FAILURE: {

                Toast.makeText(getApplicationContext(),
                        "项目修改失败",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }
    }

    @Override
    public void onAddResult(int result, ProjectInfo progectInfo) {
    }

    @Override
    public void onQueryPersonalsResult(int result, final ArrayList<PersonnelInfo> personalInfoArrayList) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                final String[] arryPersonals = new String[personalInfoArrayList.size()];
                final boolean[] arryPersonalsSelected = new boolean[personalInfoArrayList.size()];
                for (int i = 0; i < personalInfoArrayList.size(); ++i) {
                    arryPersonals[i] = personalInfoArrayList.get(i).userName;
                    arryPersonalsSelected[i] = false;
                }

                Dialog alertDialog = new AlertDialog.Builder(this).
                        setTitle("人员列表").
                        setIcon(R.drawable.login_user)
                        .setMultiChoiceItems(arryPersonals, arryPersonalsSelected, new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                arryPersonalsSelected[which] = isChecked;
                            }
                        }).setPositiveButton("确认", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                for (int i = 0; i < arryPersonalsSelected.length; i++) {
                                    if (arryPersonalsSelected[i] == true) {

                                        if (personalInfoHashMap.get(personalInfoArrayList.get(i).userID) != null) {
                                            continue;
                                        }

                                        isModFlg = true;
                                        mVideoPersonalsAdapter.addFront(personalInfoArrayList.get(i));
                                        personalInfoHashMap.put(personalInfoArrayList.get(i).userID, personalInfoArrayList.get(i));
                                    }
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();

                alertDialog.show();

                break;
            }
            case FAILURE: {

                Toast.makeText(getApplicationContext(),
                        "人员列表查询失败",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }

    }

    @Override
    public void onQueryPersonalsListResult(int result, ArrayList<PersonnelInfo> personalInfoArrayList) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                if (personalInfoArrayList != null) {

                    // 查询成功直接显示，同时更新数据库
                    for (PersonnelInfo personnelInfo : personalInfoArrayList) {

                        if (personalInfoHashMap.get(personnelInfo.userID) != null) {
                            continue;
                        }

                        personalInfoHashMap.put(personnelInfo.userID, personnelInfo);
                        mVideoPersonalsAdapter.addFront(personnelInfo);
                    }

                    DBProcMgr.getInstance().updatePersonnelInfo(mProjectInfo.projectID, personalInfoArrayList);

                }

                break;
            }
            case FAILURE: {

                List<PersonnelInfo> personnelInfos = DBProcMgr.getInstance().queryPersonnelInfo("projectID == " + mProjectInfo.projectID);
                if (personnelInfos != null) {

                    for (PersonnelInfo personnelInfo : personnelInfos) {

                        if (personalInfoHashMap.get(personnelInfo.userID) != null) {
                            continue;
                        }

                        personalInfoHashMap.put(personnelInfo.userID, personnelInfo);
                        mVideoPersonalsAdapter.addFront(personnelInfo);
                    }

                }

                // 直接从数据库加载
                Toast.makeText(getApplicationContext(),
                        "人员列表查询失败",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }
    }

    @Override
    public void onPicUploadResult(int result, String picID) {
    }

}
