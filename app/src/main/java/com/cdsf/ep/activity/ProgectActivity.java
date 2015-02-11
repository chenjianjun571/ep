package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.tool.widget.ListViewCompat;
import com.tool.widget.LoadingDialog;
import com.tool.widget.SlideView;

import java.util.ArrayList;
import java.util.List;

public class ProgectActivity extends Activity implements
        AdapterView.OnItemClickListener,
        SlideView.OnSlideListener,
        View.OnClickListener,
        ProgectProcess.OnResultListener {

    private static final String TAG = ProgectActivity.class.getSimpleName();

    // 上下文
    private Context mContext;

    // 项目列表
    private ListViewCompat projectLists;
    private SlideView mLastSlideViewWithStatusOn;
    private SlideAdapter mSlideAdapter;
    private List<ProjectInfo> mMessageItems;

    // 添加按钮
    private ImageView imgAdd;
    // 菜单按钮
    private ImageView imgRefresh;

    // 逻辑处理过程
    private ProgectProcess progectProcess;
    // 数据加载对话框
    LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progect);

        mContext = this;
        progectProcess = new ProgectProcess();
        progectProcess.setResultListener(this);

        mLoadingDialog = new LoadingDialog(mContext);
        mMessageItems = new ArrayList<ProjectInfo>();

        initView();

        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.setMessage("数据加载中,请稍等...");
            mLoadingDialog.show();
        }
        progectProcess.progectQueryReq();

        registerBoradcastReceiver();
    }

    private void initView() {

        imgAdd = (ImageView) findViewById(R.id.img_add);
        imgRefresh = (ImageView) findViewById(R.id.img_refresh);

        imgAdd.setOnClickListener(this);
        imgRefresh.setOnClickListener(this);

        projectLists = (ListViewCompat) findViewById(R.id.projectLists);
        mSlideAdapter = new SlideAdapter();
        projectLists.setAdapter(mSlideAdapter);
        projectLists.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mLastSlideViewWithStatusOn != null) {
            mLastSlideViewWithStatusOn.shrink();
            mLastSlideViewWithStatusOn = null;
            return;
        }

        ProjectInfo projectInfo = mMessageItems.get(position);
        if (projectInfo != null) {

            // 启动项目事项页面
            Intent intent = new Intent(mContext, MatterActivity.class);
            intent.putExtra("projectInfo", projectInfo);
            mContext.startActivity(intent);

            projectInfo.isMsg = false;
            mSlideAdapter.notifyDataSetChanged();

        }

    }

    @Override
    public void onSlide(View view, int status) {

        if (mLastSlideViewWithStatusOn != null && mLastSlideViewWithStatusOn != view) {
            mLastSlideViewWithStatusOn.shrink();
        }

        mLastSlideViewWithStatusOn = (SlideView) view;
        if (status != SLIDE_STATUS_ON) {
            mLastSlideViewWithStatusOn.shrink();
            mLastSlideViewWithStatusOn = null;
        }

    }

    // item数据适配器
    private class SlideAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        SlideAdapter() {
            super();
            mInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mMessageItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessageItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            SlideView slideView = (SlideView) convertView;
            if (slideView == null) {
                View itemView = mInflater.inflate(R.layout.progect_list_item, null);

                slideView = new SlideView(mContext);
                slideView.setContentView(itemView);

                holder = new ViewHolder(slideView);
                slideView.setOnSlideListener(ProgectActivity.this);
                slideView.setTag(holder);
            } else {
                holder = (ViewHolder) slideView.getTag();
            }

            ProjectInfo item = mMessageItems.get(position);

            Picasso.with(mContext).load(item.projectPic)
                    .placeholder(R.drawable.def_pic)
                    .error(R.drawable.def_pic)
                    .into(holder.projectPic);
            holder.projectName.setText(item.projectName);

            holder.projectIntroduction.setText(item.projectIntroduction);

            holder.projectDate.setText(item.projectCreateTime);
            if (item.isMsg) {
                holder.msgHint.setVisibility(View.VISIBLE);
            } else {
                holder.msgHint.setVisibility(View.GONE);
            }

            holder.projectInfo.setTag(position);
            holder.projectDel.setTag(position);
            holder.projectInfo.setOnClickListener(ProgectActivity.this);
            holder.projectDel.setOnClickListener(ProgectActivity.this);

            return slideView;
        }

    }

    private static class ViewHolder {

        // 项目头像
        public ImageView projectPic;
        // 项目名称
        public TextView projectName;
        // 项目简介
        public TextView projectIntroduction;
        // 项目时间
        public TextView projectDate;

        // 详情按钮
        public ImageView projectInfo;
        // 删除按钮
        public ImageView projectDel;

        // 是否有消息提示
        public ImageView msgHint;

        ViewHolder(View view) {
            projectPic = (ImageView) view.findViewById(R.id.imgProjectPic);
            projectName = (TextView) view.findViewById(R.id.imgProjectName);
            projectIntroduction = (TextView) view.findViewById(R.id.imgProjectIntroduction);
            projectDate = (TextView) view.findViewById(R.id.imgProjectCreateDate);
            projectInfo = (ImageView) view.findViewById(R.id.projectInfo);
            projectDel = (ImageView) view.findViewById(R.id.projectDel);
            msgHint = (ImageView) view.findViewById(R.id.imgProjectMsgHint);
        }
    }

    @Override
    public void onQueryResult(int result, ArrayList<ProjectInfo> progectInfoList) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                // 查询成功
                if (progectInfoList != null) {
                    mMessageItems.clear();
                    mMessageItems.addAll(progectInfoList);
                    mSlideAdapter.notifyDataSetChanged();
                }

                break;
            }
            case FAILURE: {

                // 查询失败
                Toast.makeText(getApplicationContext(),
                        "数据加载失败",
                        Toast.LENGTH_SHORT).show();

                if (mMessageItems.size() == 0) {
                    // 直接从数据库加载数据
                    List<ProjectInfo> projectInfos = DBProcMgr.getInstance().queryProjectInfo(null);
                    if (projectInfos == null) {
                        break;
                    }

                    mMessageItems.addAll(projectInfos);
                    mSlideAdapter.notifyDataSetChanged();
                }

                break;
            }
        }
    }

    @Override
    public void onDelResult(int result, ProjectInfo progectInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (mLastSlideViewWithStatusOn != null) {
            mLastSlideViewWithStatusOn.shrink();
            mLastSlideViewWithStatusOn = null;
        }

        switch (result) {

            case SUCCESS: {

                Toast.makeText(getApplicationContext(),
                        "数据删除成功",
                        Toast.LENGTH_SHORT).show();

                DBProcMgr.getInstance().delOneProjectInfo(progectInfo);

                mMessageItems.remove(progectInfo);
                mSlideAdapter.notifyDataSetChanged();

                break;
            }
            case FAILURE: {

                // 删除失败
                Toast.makeText(getApplicationContext(),
                        "删除失败",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }
    }

    @Override
    public void onModResult(int result, ProjectInfo progectInfo) {
    }

    @Override
    public void onAddResult(int result, ProjectInfo progectInfo) {
    }

    @Override
    public void onQueryPersonalsResult(int result, ArrayList<PersonnelInfo> personnelInfos) {
    }

    @Override
    public void onQueryPersonalsListResult(int result, ArrayList<PersonnelInfo> personalInfoArrayList) {
    }

    @Override
    public void onPicUploadResult(int result, String picID) {
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.projectInfo: {

                int i = (Integer) v.getTag();
                ProjectInfo projectInfo = mMessageItems.get(i);
                // 启动事项详细页面
                Intent intent = new Intent(mContext, ProgectInfoActivity.class);
                intent.putExtra("projectInfo", projectInfo);
                mContext.startActivity(intent);

                mLastSlideViewWithStatusOn.shrink();
                mLastSlideViewWithStatusOn = null;

                break;
            }
            case R.id.projectDel: {

                int i = (Integer) v.getTag();
                final ProjectInfo projectInfo = mMessageItems.get(i);

                new AlertDialog.Builder(mContext)
                        .setTitle("提示").setMessage("确认要删除项目？")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // 发送删除请求
                                if (!mLoadingDialog.isShowing()) {
                                    mLoadingDialog.setMessage("数据请求中,请稍等...");
                                    mLoadingDialog.show();
                                }
                                progectProcess.projectDelReq(projectInfo);

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", null).show();

                break;
            }
            case R.id.img_add: {

                Intent intent = new Intent(mContext, ProgectAddActivity.class);
                mContext.startActivity(intent);

                break;
            }
            case R.id.img_refresh: {

                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.setMessage("数据加载中,请稍等...");
                    mLoadingDialog.show();
                }

                progectProcess.progectQueryReq();

                break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(CommonDef.ACTION_PROJECT)) {
                // 去刷新项目
                progectProcess.progectQueryReq();
            } else if (action.equals(CommonDef.ACTION_MATTER) || action.equals(CommonDef.ACTION_MATTER_REPLAY)) {

                // 事项有变动
                String projectID = (String) intent.getSerializableExtra("project_id");
                for (int i = 0; i < mMessageItems.size(); ++i) {
                    if (mMessageItems.get(i).projectID == Integer.valueOf(projectID)) {
                        mMessageItems.get(i).isMsg = true;
                    }
                }

                mSlideAdapter.notifyDataSetChanged();
            }
        }

    };

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(CommonDef.ACTION_PROJECT);
        myIntentFilter.addAction(CommonDef.ACTION_MATTER);
        myIntentFilter.addAction(CommonDef.ACTION_MATTER_REPLAY);
        myIntentFilter.setPriority(10);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }
}
