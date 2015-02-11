package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.bean.ModInfoResult;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.bean.ProjectInfo;
import com.cdsf.ep.common.CommonDef;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.MatterProcess;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.tool.unit.BaseAdapterHelper;
import com.tool.unit.QuickAdapter;
import com.tool.widget.LoadingDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MatterActivity extends Activity implements View.OnClickListener,
        MatterProcess.OnResultListener {

    private static final String TAG = MatterActivity.class.getSimpleName();
    // 返回按钮
    private Button btnBack;
    // 添加
    private ImageView imgAddMatter;
    // 标题
    private TextView txTitle;
    // 传递过来的项目信息
    private ProjectInfo projectInfo;
    // 网络处理过程
    private MatterProcess matterProcess;

    //private RefreshableView refreshableView;
    PullToRefreshListView mPullRefreshListView;
    private ListView listViewMatterInfo;

    // 记录是否下拉刷新
    private boolean isRefresh = false;

    // 事项数据源
    protected QuickAdapter<MatterInfo> matterInfoAdapter;
    private Context mContext;

    // 数据加载对话框
    LoadingDialog mLoadingDialog;

    // 快速回复对话框
    Dialog alertDialog;

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    // 播放动画
    private AnimationDrawable anim;
    // 最后一个播放的view
    private View lastView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matter);

        matterProcess = new MatterProcess();
        matterProcess.setResultListener(this);
        mContext = this;
        projectInfo = (ProjectInfo) getIntent().getSerializableExtra("projectInfo");

        mLoadingDialog = new LoadingDialog(mContext);

        initView();

        // 注册广播
        registerBoradcastReceiver();

        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.setMessage("数据加载中,请稍等...");
            mLoadingDialog.show();
        }
        // 发送数据请求
        matterProcess.matterReq(projectInfo, 0);
    }

    private void initView() {

        // 返回
        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        // 添加事项
        imgAddMatter = (ImageView) findViewById(R.id.img_add);
        imgAddMatter.setOnClickListener(this);

        // 标题
        txTitle = (TextView) findViewById(R.id.tx_title);
        txTitle.setText(projectInfo.projectName);

        // 刷新加载list_view
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.matter_pull_refresh);
        listViewMatterInfo = mPullRefreshListView.getRefreshableView();

        matterInfoAdapter = new QuickAdapter<MatterInfo>(this, R.layout.matter_list_item) {

            @Override
            protected void convert(BaseAdapterHelper helper, MatterInfo item) {

                // 事项发起人名称
                helper.setText(R.id.tx_matter_list_item_user_name, item.matterCreateUserName+"->"+item.matterRecvPersonnels);
                // 事项发起人头像
                helper.setImageUrl(R.id.img_matter_list_item_pic,
                        item.matterCreateUserPic,
                        R.drawable.def_pic,
                        R.drawable.def_pic);

                // 事项发起人ep等级
                switch (item.matterCreateEpValue) {
                    case CommonDef.EP_VALUE_1: {
                        helper.setImageDrawable(R.id.img_matter_list_item_ep_value, R.drawable.ep1);
                        break;
                    }
                    case CommonDef.EP_VALUE_2: {
                        helper.setImageDrawable(R.id.img_matter_list_item_ep_value, R.drawable.ep2);
                        break;
                    }
                    case CommonDef.EP_VALUE_3: {
                        helper.setImageDrawable(R.id.img_matter_list_item_ep_value, R.drawable.ep3);
                        break;
                    }
                    case CommonDef.EP_VALUE_4: {
                        helper.setImageDrawable(R.id.img_matter_list_item_ep_value, R.drawable.ep4);
                        break;
                    }
                    case CommonDef.EP_VALUE_5: {
                        helper.setImageDrawable(R.id.img_matter_list_item_ep_value, R.drawable.ep5);
                        break;
                    }
                    case CommonDef.EP_VALUE_6: {
                        helper.setImageDrawable(R.id.img_matter_list_item_ep_value, R.drawable.ep6);
                        break;
                    }
                    case CommonDef.EP_VALUE_7: {
                        helper.setImageDrawable(R.id.img_matter_list_item_ep_value, R.drawable.ep7);
                        break;
                    }
                    case CommonDef.EP_VALUE_8: {
                        helper.setImageDrawable(R.id.img_matter_list_item_ep_value, R.drawable.ep8);
                        break;
                    }
                }

                // 事项内容
                if (item.matterType == CommonDef.MATTER_TYPE_SOUND) {
                    // 声音
                    helper.setVisible(R.id.tx_matter_list_item_content_sound, true);
                    helper.setVisible(R.id.imv_matter_list_item_content_pic, false);
                    helper.setVisible(R.id.tx_matter_list_item_content_text, false);

                    final ImageView playSound = (ImageView) helper.getView(R.id.tx_matter_list_item_content_sound);
                    playSound.setTag(R.id.matter_list_item_sound, item);
                    playSound.setOnClickListener(MatterActivity.this);

                } else if (item.matterType == CommonDef.MATTER_TYPE_TEXT) {
                    // 文字
                    helper.setVisible(R.id.tx_matter_list_item_content_sound, false);
                    helper.setVisible(R.id.imv_matter_list_item_content_pic, false);
                    helper.setVisible(R.id.tx_matter_list_item_content_text, true);
                    helper.setText(R.id.tx_matter_list_item_content_text, item.matterContent);
                } else {
                    // 图片
                    helper.setVisible(R.id.tx_matter_list_item_content_sound, false);
                    helper.setVisible(R.id.imv_matter_list_item_content_pic, true);
                    helper.setVisible(R.id.tx_matter_list_item_content_text, false);

                    helper.setImageUrl(R.id.imv_matter_list_item_content_pic,
                            item.matterContent,
                            R.drawable.pic_loading,
                            R.drawable.pic_load_error);
                }

                // 接收按钮
                final TextView btnRecv = (TextView) helper.getView(R.id.imgbtn_matter_list_item_recv);
                // 拒绝按钮
                final TextView btnReFuse = (TextView) helper.getView(R.id.imgbtn_matter_list_item_refuse);


                switch (item.matterStatus) {

                    case CommonDef.MATTER_STATUS_INIT: {// 待处理

                        // 如果是创建者查看，不展示接收拒绝按钮
                        if (!(item.matterCreateUserName.equals(APPDataInfo.getInstance().accountInfo.userName))) {

                            btnRecv.setVisibility(View.VISIBLE);
                            btnReFuse.setVisibility(View.VISIBLE);

                            btnRecv.setTag(R.id.matter_list_item_recv, item);
                            btnRecv.setOnClickListener(MatterActivity.this);

                            btnReFuse.setTag(R.id.matter_list_item_re_fuse, item);
                            btnReFuse.setOnClickListener(MatterActivity.this);
                        } else {
                            btnRecv.setVisibility(View.GONE);
                            btnReFuse.setVisibility(View.GONE);
                        }

                        helper.setText(R.id.tx_matter_list_item_status, "待处理");
                        ((TextView)helper.getView(R.id.tx_matter_list_item_status)).setTextColor(Color.parseColor("#ADFF2F"));

                        break;
                    }
                    case CommonDef.MATTER_STATUS_RECV: {// 已接收

                        btnRecv.setVisibility(View.GONE);
                        btnReFuse.setVisibility(View.GONE);

                        helper.setText(R.id.tx_matter_list_item_status, "已接收");
                        ((TextView)helper.getView(R.id.tx_matter_list_item_status)).setTextColor(Color.parseColor("#8FBC8F"));

                        break;
                    }
                    case CommonDef.MATTER_STATUS_RE_FUSE: {// 已拒绝

                        btnRecv.setVisibility(View.GONE);
                        btnReFuse.setVisibility(View.GONE);

                        helper.setText(R.id.tx_matter_list_item_status, "已拒绝");
                        ((TextView)helper.getView(R.id.tx_matter_list_item_status)).setTextColor(Color.parseColor("#B22222"));

                        break;
                    }
                    case CommonDef.MATTER_STATUS_ALRM: {// 告警

                        btnRecv.setVisibility(View.GONE);
                        btnReFuse.setVisibility(View.GONE);

                        helper.setText(R.id.tx_matter_list_item_status, "告警");
                        ((TextView)helper.getView(R.id.tx_matter_list_item_status)).setTextColor(Color.parseColor("#FF0000"));

                        break;
                    }
                    case CommonDef.MATTER_STATUS_TIMEOUT: {// 超时

                        btnRecv.setVisibility(View.GONE);
                        btnReFuse.setVisibility(View.GONE);

                        helper.setText(R.id.tx_matter_list_item_status, "超时");
                        ((TextView)helper.getView(R.id.tx_matter_list_item_status)).setTextColor(Color.parseColor("#FF0000"));

                        break;
                    }
                    case CommonDef.MATTER_STATUS_FINAL: {// 完成

                        btnRecv.setVisibility(View.GONE);
                        btnReFuse.setVisibility(View.GONE);

                        helper.setText(R.id.tx_matter_list_item_status, "完成");
                        ((TextView)helper.getView(R.id.tx_matter_list_item_status)).setTextColor(Color.parseColor("#8FBC8F"));

                        break;
                    }
                    default: {

                        btnRecv.setVisibility(View.GONE);
                        btnReFuse.setVisibility(View.GONE);

                        helper.setText(R.id.tx_matter_list_item_status, "未知");
                    }
                }

                // 回复按钮
                final LinearLayout btnReply = (LinearLayout) helper.getView(R.id.imgbtn_matter_list_item_replay);
                btnReply.setTag(R.id.matter_list_item_replay, item);
                btnReply.setOnClickListener(MatterActivity.this);

                // 回复区域
                // 回复按钮
                final RelativeLayout lyReplayArea = (RelativeLayout) helper.getView(R.id.relativeLayout);
                lyReplayArea.setTag(R.id.matter_list_item_replay, item);
                lyReplayArea.setOnClickListener(MatterActivity.this);

                if (item.matterReplyInfo != null) {

                    helper.setVisible(R.id.relativeLayout, true);

                    // 最新的回复内容
                    if (item.matterReplyInfo.replyType == CommonDef.REPLAY_TYPE_SOUND) {
                        SpannableStringBuilder style=new SpannableStringBuilder(item.matterReplyInfo.replyUserNmae + "：[语音]");
                        style.setSpan(new ForegroundColorSpan(Color.parseColor("#008080")), 0, item.matterReplyInfo.replyUserNmae.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                        ((TextView)helper.getView(R.id.tx_matter_replay_content)).setText(style);
                    } else if (item.matterReplyInfo.replyType == CommonDef.REPLAY_TYPE_PIC) {
                        SpannableStringBuilder style=new SpannableStringBuilder(item.matterReplyInfo.replyUserNmae + "：[图片]");
                        style.setSpan(new ForegroundColorSpan(Color.parseColor("#008080")), 0, item.matterReplyInfo.replyUserNmae.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                        ((TextView)helper.getView(R.id.tx_matter_replay_content)).setText(style);
                    } else {
                        SpannableStringBuilder style=new SpannableStringBuilder(item.matterReplyInfo.replyUserNmae + "：" + item.matterReplyInfo.replyContent);
                        style.setSpan(new ForegroundColorSpan(Color.parseColor("#008080")), 0, item.matterReplyInfo.replyUserNmae.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                        ((TextView)helper.getView(R.id.tx_matter_replay_content)).setText(style);
                    }

                } else {
                    helper.setVisible(R.id.relativeLayout, false);
                }

                final View view = helper.getView();
                view.setTag(helper);
                view.setTag(R.id.matter_list_item_matter_info, item);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        MatterInfo matterInfo = (MatterInfo) v.getTag(R.id.matter_list_item_matter_info);
                        // 点击的话启动事项详细页面
                        Intent intent = new Intent(mContext, MatterInfoActivity.class);
                        intent.putExtra("matterInfo", matterInfo);
                        mContext.startActivity(intent);

                    }
                });

            }

        };
        listViewMatterInfo.setAdapter(matterInfoAdapter);

        // 设置可上拉下拉
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 下拉是刷新
                isRefresh = true;
                matterProcess.matterReq(projectInfo, 0);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 上拉是加载更多
                isRefresh = true;
                matterProcess.matterReq(projectInfo, matterInfoAdapter.getCount());
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_back: {

                MatterActivity.this.finish();
                break;

            }
            case R.id.img_add: {

                Intent intent = new Intent(mContext, MatterAddActivity.class);
                intent.putExtra("projectInfo", projectInfo);
                mContext.startActivity(intent);
                break;

            }
            case R.id.imgbtn_matter_list_item_recv: {

                // 接收点击
                MatterInfo matterInfo = (MatterInfo) v.getTag(R.id.matter_list_item_recv);
                // 发送状态更新请求
                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.setMessage("数据同步中,请稍等...");
                    mLoadingDialog.show();
                }

                matterProcess.matterStatusReq(matterInfo, CommonDef.MATTER_STATUS_RECV);

                break;
            }
            case R.id.imgbtn_matter_list_item_refuse: {

                // 拒绝点击
                MatterInfo matterInfo = (MatterInfo) v.getTag(R.id.matter_list_item_re_fuse);
                // 发送状态更新请求
                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.setMessage("数据同步中,请稍等...");
                    mLoadingDialog.show();
                }

                matterProcess.matterStatusReq(matterInfo, CommonDef.MATTER_STATUS_RE_FUSE);

                break;
            }
            case R.id.imgbtn_matter_list_item_replay: {
                // 回复点击
                final MatterInfo matterInfo = (MatterInfo) v.getTag(R.id.matter_list_item_replay);

                Intent intent = new Intent(mContext, QuickReplyActivity.class);
                intent.putExtra("matterInfo", matterInfo);
                startActivityForResult(intent, 1);

                break;
            }
            case R.id.tx_matter_list_item_content_sound:
            {
                final MatterInfo matterInfo = (MatterInfo)v.getTag(R.id.matter_list_item_sound);
                playSound(v,matterInfo);

                break;
            }
            case R.id.relativeLayout:
            {
                // 回复区域点击
                final MatterInfo matterInfo = (MatterInfo) v.getTag(R.id.matter_list_item_replay);

                Intent intent = new Intent(mContext, ReplayActivity.class);
                intent.putExtra("matterInfo", matterInfo);
                startActivity(intent);

                break;
            }

        }

    }

    private void playSound(final View v, MatterInfo matterInfo) {

        // 获取URL路径里面的声音ID，就是URL的最后一个内容
        int start = matterInfo.matterContent.lastIndexOf("/");
        if (start == -1) {
            return;
        }

        String soundID = matterInfo.matterContent.substring(start)+".amr";

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }

        if (anim != null) {
            anim.stop();
        }

        if (lastView != null) {
            lastView.setBackgroundResource(R.drawable.play_han_3);
            lastView = null;
        }

        v.setBackgroundResource(R.anim.play_han);
        anim = (AnimationDrawable)v.getBackground();
        lastView = v;

        File file = new File(APPDataInfo.DOWN_SOUND_LOCAL_DIR+soundID);
        if (file.exists()) {
            if (file.isFile()) {
                // 有缓存文件，直接播放缓存文件
                try {

                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(APPDataInfo.DOWN_SOUND_LOCAL_DIR+soundID);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();

                    anim.start();
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            mp.stop();
                            anim.stop();
                            v.setBackgroundResource(R.drawable.play_han_3);
                            lastView = null;
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    anim.stop();
                    v.setBackgroundResource(R.drawable.play_han_3);
                }
            }
            return;
        }
        else {
            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.setMessage("数据下载中,请稍等...");
                mLoadingDialog.show();
            }

            // 先下载声音文件
            HttpUtils http = new HttpUtils();
            http.configRequestThreadPoolSize(3);
            http.configTimeout(5000);
            http.download(matterInfo.matterContent, APPDataInfo.DOWN_SOUND_LOCAL_DIR+soundID, true, false, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> fileResponseInfo) {

                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }

                    File file = new File(fileResponseInfo.result.getPath());
                    if (!file.exists()) {
                        return;
                    }

                    try {

                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(fileResponseInfo.result.getPath());
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();

                        anim.start();
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                mp.stop();
                                anim.stop();
                                v.setBackgroundResource(R.drawable.play_han_3);
                                lastView = null;
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        anim.stop();
                        v.setBackgroundResource(R.drawable.play_han_3);
                    }

                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }

                    Toast.makeText(getApplicationContext(),
                            "数据请求失败",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public void onMatterReqResult(int result, int reqType, List<MatterInfo> matterInfoArrayList) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (isRefresh) {
            mPullRefreshListView.onRefreshComplete();
            isRefresh = false;
        }

        if (result == SUCCESS) {

            if (reqType == 0) {
                // 全部刷新
                // 先保存数据库
                DBProcMgr.getInstance().saveMatterInfo(projectInfo.projectID, matterInfoArrayList, 0);

                matterInfoAdapter.clear();
                // 刷新数据
                if (matterInfoArrayList != null) {
                    matterInfoAdapter.addAll(matterInfoArrayList);
                }

            } else {
                // 加载更多
                DBProcMgr.getInstance().saveMatterInfo(projectInfo.projectID, matterInfoArrayList, 1);

                if (matterInfoArrayList == null) {
                    return;
                }

                // 刷新数据
                for (MatterInfo matterInfo:matterInfoArrayList) {
                    matterInfoAdapter.add(matterInfo);
                }
            }

        } else {

            Toast.makeText(getApplicationContext(),
                    "数据加载失败",
                    Toast.LENGTH_SHORT).show();

            // 失败
            if (matterInfoAdapter.getCount() == 0) {

                // 加载数据库资源
                List<MatterInfo> matterInfoList = DBProcMgr.getInstance().queryMatterInfo("projectID = " + projectInfo.projectID);
                if (matterInfoList != null) {
                    matterInfoAdapter.addAll(matterInfoList);
                }

            }
        }

    }

    @Override
    public void onMatterStatusReqResult(int result, MatterInfo matterInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (result == SUCCESS) {

            // 成功
            DBProcMgr.getInstance().updateMatterInfo(matterInfo);
            // UI界面更新
            matterInfoAdapter.notifyDataSetChanged();

        } else {

            Toast.makeText(getApplicationContext(),
                    "数据同步失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onMatterProcessReqResult(int result, MatterInfo matterInfo) {
    }

    @Override
    public void onMatterReplayReqResult(int result, MatterInfo matterInfo, MatterReplyInfo matterReplyInfo) {
    }

    @Override
    public void onQueryPersonalsResult(int result, ArrayList<PersonnelInfo> personnelInfos){}

    @Override
    public void onMatterAddReqResult(int result, MatterInfo matterInfo) {}

    @Override
    public void onMatterDelReqResult(int result, MatterInfo matterInfo) {
    }

    @Override
    public void onSoundUploadResult(int result, String soundID, MatterInfo matterInfo, String users) {
    }

    @Override
    public void onPicUploadResult(int result, String picID, MatterInfo matterInfo, String users) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        MatterInfo matterInfo = (MatterInfo)data.getSerializableExtra("result");
        switch(requestCode) {

            case 1: {

                if (resultCode == 0) {

                    for (int i = 0; i < matterInfoAdapter.getCount(); ++i) {
                        if (matterInfoAdapter.getItem(i).matterID.equals(matterInfo.matterID)) {
                            matterInfoAdapter.getItem(i).matterReplyInfo = matterInfo.matterReplyInfo;
                            matterInfoAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                }

                break;
            }

        }

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if(action.equals(CommonDef.ACTION_MATTER)){
//
//            }
            String projectID = (String) intent.getSerializableExtra("project_id");
            if (projectInfo.projectID == Integer.valueOf(projectID)) {
                // 去刷新事项
                matterProcess.matterReq(projectInfo, 0);

                abortBroadcast();
            }
        }

    };

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(CommonDef.ACTION_MATTER);
        myIntentFilter.setPriority(100);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }
}
