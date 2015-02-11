package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.common.CommonDef;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.MatterInfoProcess;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.tool.PreferenceUtils;
import com.tool.media.SoundMeter;
import com.tool.unit.BaseAdapterHelper;
import com.tool.unit.QuickAdapter;
import com.tool.widget.LoadingDialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReplayActivity extends Activity implements View.OnClickListener,
        MatterInfoProcess.OnResultListener, GestureDetector.OnGestureListener {

    private Context context;
    // 回复文本语音切换按钮
    private ImageView imvReplayTypeChange;
    // 回复其他按钮
    private ImageView imvReplayOther;
    // 其他功能区
    private LinearLayout lyOtherTools;
    // 文本编辑框
    private EditText editTextText;
    // 语音触摸按钮
    private ImageView imvSoundTouch;
    // 图片回复
    private ImageView imvPicReplay;
    // 回复提交
    private ImageView imvMsgReply;
    // 录音显示层
    private LinearLayout lyRcd;
    private LinearLayout voice_rcd_hint_rcding, voice_rcd_hint_tooshort;
    private boolean isShosrt = false;
    private long startVoiceT, endVoiceT;
    private SoundMeter mSensor;
    private ImageView volume;
    // 切换标志
    private boolean flg = true;
    // 回复类型
    private int type = CommonDef.REPLAY_TYPE_TEXT;
    // 回复内容
    private String content = "";

    PullToRefreshListView mPullRefreshListView;
    // 回复list
    private ListView listViewReply;
    protected QuickAdapter<MatterReplyInfo> matterReplyInfoAdapter;

    // 数据加载对话框
    LoadingDialog mLoadingDialog;
    // 事项相关处理
    private MatterInfoProcess matterInfoProcess;
    // 传递过来的事项信息
    private MatterInfo matterInfo;

    // 记录是否上拉加载
    private boolean isRefresh = false;

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    // 播放动画
    private AnimationDrawable anim;

    // 最后一个播放的view
    private View lastView = null;

    // 返回
    private Button btnBack;

    // 滑动监听器
    private GestureDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        mLoadingDialog = new LoadingDialog(this);
        mSensor = new SoundMeter();

        context = this;
        detector = new GestureDetector(this);

        matterInfoProcess = new MatterInfoProcess();
        matterInfoProcess.setResultListener(this);

        matterInfo = (MatterInfo) getIntent().getSerializableExtra("matterInfo");

        // 注册广播
        registerBoradcastReceiver();

        initView();
        initData();
    }


    private Handler mHandler = new Handler();
    private static final int POLL_INTERVAL = 300;

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            stop();
        }
    };
    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = mSensor.getAmplitude();
            updateDisplay(amp);
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };

    private void start() {

        File file = new File(APPDataInfo.REPLAY_SOUND_LOCAL_PATH);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            }
        }

        if (!mSensor.start(APPDataInfo.REPLAY_SOUND_LOCAL_PATH)) {
            Toast.makeText(getApplicationContext(),
                    "打开录音设备失败",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private void stop() {
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        volume.setImageResource(R.drawable.amp1);
    }

    private void updateDisplay(double signalEMA) {

        switch ((int) signalEMA) {
            case 0:
            case 1:
                volume.setImageResource(R.drawable.amp1);
                break;
            case 2:
            case 3:
                volume.setImageResource(R.drawable.amp2);

                break;
            case 4:
            case 5:
                volume.setImageResource(R.drawable.amp3);
                break;
            case 6:
            case 7:
                volume.setImageResource(R.drawable.amp4);
                break;
            case 8:
            case 9:
                volume.setImageResource(R.drawable.amp5);
                break;
            case 10:
            case 11:
                volume.setImageResource(R.drawable.amp6);
                break;
            default:
                volume.setImageResource(R.drawable.amp7);
                break;
        }
    }

    private void initView() {

        btnBack = (Button)findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        imvMsgReply = (ImageView) findViewById(R.id.iv_send);
        imvMsgReply.setOnClickListener(this);

        imvReplayTypeChange = (ImageView)findViewById(R.id.iv_send_type_chg);
        imvReplayTypeChange.setOnClickListener(this);

        editTextText = (EditText)findViewById(R.id.edit_text_replay);
        imvSoundTouch = (ImageView)findViewById(R.id.imv_sound_replay);

        imvReplayOther = (ImageView)findViewById(R.id.iv_other_replay);
        imvReplayOther.setOnClickListener(this);

        lyOtherTools = (LinearLayout)findViewById(R.id.ly_send_other);

        lyRcd = (LinearLayout)findViewById(R.id.ly_rcd_popup);
        voice_rcd_hint_rcding = (LinearLayout)findViewById(R.id.voice_rcd_hint_rcding);
        voice_rcd_hint_tooshort = (LinearLayout)findViewById(R.id.voice_rcd_hint_tooshort);
        volume = (ImageView)findViewById(R.id.volume);

        imvPicReplay = (ImageView)findViewById(R.id.imv_pic_replay);
        imvPicReplay.setOnClickListener(this);

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.matter_info_pull_refresh);
        listViewReply = mPullRefreshListView.getRefreshableView();
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 下拉是加载更多的历史数据
                isRefresh = true;

                if (matterReplyInfoAdapter.getCount() > 0) {
                    matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_FRONT, matterReplyInfoAdapter.getItem(0).replayID);
                } else {
                    matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_INIT, 0);
                }

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 上拉是加载最新数据
                isRefresh = true;
                if (matterReplyInfoAdapter.getCount() > 0) {
                    matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_BACK, matterReplyInfoAdapter.getItem(matterReplyInfoAdapter.getCount()-1).replayID);
                } else {
                    matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_INIT, 0);
                }
            }
        });

        // 允许上下拉
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);

        matterReplyInfoAdapter = new QuickAdapter<MatterReplyInfo>(this, R.layout.matter_replay_list_item) {

            @Override
            protected void convert(BaseAdapterHelper helper, MatterReplyInfo item) {

                final View view = helper.getView();
                if (item.replyUserNmae.equals(APPDataInfo.getInstance().accountInfo.userName)) {
                    //view.setBackgroundColor(Color.parseColor("#fcF5DC"));
                    // 回复人名称
                    helper.setText(R.id.iv_replay_item_user_name, "我");
                } else {
                    // 回复人名称
                    helper.setText(R.id.iv_replay_item_user_name, item.replyUserNmae);
                }

                // 回复人头像
                helper.setImageUrl(R.id.iv_replay_item_user_pic,
                        item.replyUserPic,
                        R.drawable.def_pic,
                        R.drawable.def_pic);

                switch (item.replyEpValue) {
                    case CommonDef.EP_VALUE_1: {
                        helper.setImageDrawable(R.id.img_replay_item_ep_value, R.drawable.ep1);
                        break;
                    }
                    case CommonDef.EP_VALUE_2: {
                        helper.setImageDrawable(R.id.img_replay_item_ep_value, R.drawable.ep2);
                        break;
                    }
                    case CommonDef.EP_VALUE_3: {
                        helper.setImageDrawable(R.id.img_replay_item_ep_value, R.drawable.ep3);
                        break;
                    }
                    case CommonDef.EP_VALUE_4: {
                        helper.setImageDrawable(R.id.img_replay_item_ep_value, R.drawable.ep4);
                        break;
                    }
                    case CommonDef.EP_VALUE_5: {
                        helper.setImageDrawable(R.id.img_replay_item_ep_value, R.drawable.ep5);
                        break;
                    }
                    case CommonDef.EP_VALUE_6: {
                        helper.setImageDrawable(R.id.img_replay_item_ep_value, R.drawable.ep6);
                        break;
                    }
                    case CommonDef.EP_VALUE_7: {
                        helper.setImageDrawable(R.id.img_replay_item_ep_value, R.drawable.ep7);
                        break;
                    }
                    case CommonDef.EP_VALUE_8: {
                        helper.setImageDrawable(R.id.img_replay_item_ep_value, R.drawable.ep8);
                        break;
                    }
                }

                // 回复内容
                if (item.replyType == CommonDef.REPLAY_TYPE_SOUND) {
                    helper.setVisible(R.id.tx_replay_item_content, false);
                    helper.setVisible(R.id.imv_replay_item_content_pic, false);
                    helper.setVisible(R.id.imv_replay_item_content_sound, true);

                    final ImageView playSound = (ImageView) helper.getView(R.id.imv_replay_item_content_sound);
                    playSound.setTag(R.id.replay_list_item_sound, item);
                    playSound.setOnClickListener(ReplayActivity.this);

                } else if (item.replyType == CommonDef.REPLAY_TYPE_PIC) {

                    helper.setVisible(R.id.tx_replay_item_content, false);
                    helper.setVisible(R.id.imv_replay_item_content_pic, true);
                    helper.setVisible(R.id.imv_replay_item_content_sound, false);

                    helper.setImageUrl(R.id.imv_replay_item_content_pic,
                            item.replyContent,
                            R.drawable.pic_loading,
                            R.drawable.pic_load_error);

                    final ImageView playPic = (ImageView) helper.getView(R.id.imv_replay_item_content_pic);
                    playPic.setTag(R.id.replay_list_item_pic, item);
                    playPic.setOnClickListener(ReplayActivity.this);
                }
                else {
                    helper.setVisible(R.id.tx_replay_item_content, true);
                    helper.setVisible(R.id.imv_replay_item_content_sound, false);
                    helper.setVisible(R.id.imv_replay_item_content_pic, false);
                    helper.setText(R.id.tx_replay_item_content, item.replyContent);
                }

                // 回复时间
                helper.setText(R.id.tx_replay_item_time, item.replyDate);
            }

        };

        listViewReply.setAdapter(matterReplyInfoAdapter);

        // 请求回复信息
        matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_INIT, 0);
    }

    private void initData() {

        type = CommonDef.REPLAY_TYPE_TEXT;
        flg = true;
        content = "";
        imvReplayTypeChange.setImageResource(R.drawable.send_text);
        editTextText.setVisibility(View.VISIBLE);
        imvSoundTouch.setVisibility(View.GONE);
        lyOtherTools.setVisibility(View.GONE);
        lyRcd.setVisibility(View.GONE);

        imvSoundTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!Environment.getExternalStorageDirectory().exists()) {
                    Toast.makeText(getApplicationContext(), "No SDCard", Toast.LENGTH_LONG).show();
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // 按下事件
                        imvSoundTouch.setBackgroundResource(R.drawable.btn_end_speak);
                        lyRcd.setVisibility(View.VISIBLE);
                        voice_rcd_hint_rcding.setVisibility(View.GONE);
                        voice_rcd_hint_tooshort.setVisibility(View.GONE);

                        start();

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        startVoiceT = System.currentTimeMillis();

                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                if (!isShosrt) {
                                    voice_rcd_hint_rcding.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 200);

                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        // 松开事件
                        imvSoundTouch.setBackgroundResource(R.drawable.btn_press_speak);
                        voice_rcd_hint_rcding.setVisibility(View.GONE);

                        stop();
                        endVoiceT = System.currentTimeMillis();

                        int time = (int) ((endVoiceT - startVoiceT) / 1000);
                        if (time < 1) {
                            isShosrt = true;
                            voice_rcd_hint_rcding.setVisibility(View.GONE);
                            voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    voice_rcd_hint_tooshort.setVisibility(View.GONE);
                                    lyRcd.setVisibility(View.GONE);
                                    isShosrt = false;
                                }
                            }, 500);
                            return false;
                        }

                        lyRcd.setVisibility(View.GONE);
                    }
                    default: {
                        return false;
                    }
                }

                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.iv_send_type_chg: {

                // 删除残留文件
                File file = new File(APPDataInfo.REPLAY_SOUND_LOCAL_PATH);
                if (file.exists()) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
                content = "";

                if (flg) {
                    imvReplayTypeChange.setImageResource(R.drawable.send_sound);
                    editTextText.setVisibility(View.GONE);
                    imvSoundTouch.setVisibility(View.VISIBLE);
                    type = CommonDef.REPLAY_TYPE_SOUND;

                    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(ReplayActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                } else {
                    imvReplayTypeChange.setImageResource(R.drawable.send_text);
                    editTextText.setVisibility(View.VISIBLE);
                    imvSoundTouch.setVisibility(View.GONE);
                    type = CommonDef.REPLAY_TYPE_TEXT;
                }

                flg = !flg;

                break;

            }
            case R.id.iv_other_replay: {

                if (lyOtherTools.getVisibility() == View.VISIBLE) {
                    lyOtherTools.setVisibility(View.GONE);
                } else {
                    lyOtherTools.setVisibility(View.VISIBLE);
                }

                break;

            }
            case R.id.imv_pic_replay: {

                // 删除残留文件
                File file = new File(IMAGE_UP_FILE_NAME);
                if (file.exists()) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }

                File file1 = new File(IMAGE_FILE_NAME);
                if (file1.exists()) {
                    if (file1.isFile()) {
                        file1.delete();
                    }
                }

                showDialog();

                break;

            }
            case R.id.iv_send: {

                switch (type) {

                    case CommonDef.REPLAY_TYPE_TEXT: {

                        if (editTextText.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "回复内容不能为空哟！", Toast.LENGTH_LONG).show();
                            break;
                        }

                        if (editTextText.getText().length() > 1024) {
                            Toast.makeText(getApplicationContext(), "回复内容太长了！", Toast.LENGTH_LONG).show();
                            break;
                        }

                        type = CommonDef.REPLAY_TYPE_TEXT;
                        content = editTextText.getText().toString();

                        processHander.sendEmptyMessage(2);

                        break;
                    }
                    case CommonDef.REPLAY_TYPE_SOUND: {

                        File file = new File(APPDataInfo.REPLAY_SOUND_LOCAL_PATH);
                        if (!file.exists()) {
                            Toast.makeText(getApplicationContext(), "说点什么吧!" , Toast.LENGTH_LONG).show();
                            break;
                        }

                        type = CommonDef.REPLAY_TYPE_SOUND;
                        processHander.sendEmptyMessage(0);

                        break;
                    }
                    case CommonDef.REPLAY_TYPE_PIC: {

                        type = CommonDef.REPLAY_TYPE_PIC;
                        processHander.sendEmptyMessage(1);

                        break;
                    }
                }

                break;

            }
            case R.id.imv_replay_item_content_sound:
            {
                final MatterReplyInfo matterReplyInfo = (MatterReplyInfo)v.getTag(R.id.replay_list_item_sound);
                playSound(v,matterReplyInfo);

                break;
            }
            case R.id.imv_replay_item_content_pic:
            {
                final MatterReplyInfo matterReplyInfo = (MatterReplyInfo)v.getTag(R.id.replay_list_item_pic);
                Intent intent = new Intent(ReplayActivity.this, DisplayPictures.class);
                intent.putExtra("matterReplyInfo", matterReplyInfo);
                context.startActivity(intent);

                break;
            }
            case R.id.btn_back:
            {
                finish();
                break;
            }

        }

    }

    private Handler processHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.setMessage("数据处理中,请稍等...");
                mLoadingDialog.show();
            }

            switch (msg.what) {

                case 0:{

                    // 处理声音上传
                    matterInfoProcess.soundUploadReq(APPDataInfo.REPLAY_SOUND_LOCAL_PATH);

                    break;
                }
                case 1:{

                    // 处理图片上传
                    matterInfoProcess.picUploadReq(IMAGE_UP_FILE_NAME);

                    break;
                }
                case 2:{

                    // 提交回复
                    MatterReplyInfo replyInfo = new MatterReplyInfo();
                    replyInfo.replyType = type;
                    replyInfo.matterID = matterInfo.matterID;
                    replyInfo.replyContent = content;
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    replyInfo.replyDate = sDateFormat.format(new java.util.Date());
                    replyInfo.replyUserNmae = APPDataInfo.getInstance().accountInfo.userName;
                    replyInfo.replyUserPic = APPDataInfo.getInstance().accountInfo.userPic;

                    matterInfoProcess.replayReq(matterInfo, replyInfo);

                    break;
                }
                default:{

                    if(mLoadingDialog.isShowing())
                    {
                        mLoadingDialog.dismiss();
                    }

                    break;
                }
            }

        }

    };

    @Override
    public <T> void onResult(int result, List<T> listinfo, int type) {

        if (isRefresh) {
            mPullRefreshListView.onRefreshComplete();
            isRefresh = false;
        }

        if (result != SUCCESS) {

            Toast.makeText(getApplicationContext(),
                    "数据加载失败",
                    Toast.LENGTH_SHORT).show();

            // 失败
            if (matterReplyInfoAdapter.getCount() == 0) {

                // 加载数据库资源
                List<MatterReplyInfo> matterReplyInfos = DBProcMgr.getInstance().queryMatterReplyInfo("matterID = '" + matterInfo.matterID + "'");
                if (matterReplyInfos != null) {
                    matterReplyInfoAdapter.addAll(matterReplyInfos);
                    listViewReply.setSelection(matterReplyInfos.size());
                }

            }

            return;
        }

        // 成功
        List<MatterReplyInfo> matterReplyInfos = (List<MatterReplyInfo>) listinfo;
        // 先保存数据库
        DBProcMgr.getInstance().saveMatterReplyInfo(matterInfo.matterID, matterReplyInfos);

        switch (type) {
            case CommonDef.REPLAY_LOAD_TYPE_INIT: {

                matterReplyInfoAdapter.clear();
                // 刷新数据
                if (matterReplyInfos != null) {
                    matterReplyInfoAdapter.addAll(matterReplyInfos);
                    listViewReply.setSelection(matterReplyInfos.size());
                }

                break;
            }
            case CommonDef.REPLAY_LOAD_TYPE_FRONT: {
                if (matterReplyInfos != null) {
                    matterReplyInfoAdapter.addFrontAll(matterReplyInfos);
                    listViewReply.setSelection(0);
                }

                break;
            }
            case CommonDef.REPLAY_LOAD_TYPE_NEW_ONE:
            case CommonDef.REPLAY_LOAD_TYPE_BACK: {
                if (matterReplyInfos != null) {
                    matterReplyInfoAdapter.addBackAll(matterReplyInfos);
                    listViewReply.setSelection(matterReplyInfoAdapter.getCount());
                }

                break;
            }
        }

    }

    @Override
    public void onReplayReqResult(int result, MatterInfo matterInfo, MatterReplyInfo matterReplyInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (type == CommonDef.REPLAY_TYPE_PIC) {
            if (flg) {
                type = CommonDef.REPLAY_TYPE_TEXT;
            } else {
                type = CommonDef.REPLAY_TYPE_SOUND;
            }
            lyOtherTools.setVisibility(View.GONE);
        }

        if (result == SUCCESS) {

            content = "";
            editTextText.setText("");

            if (matterReplyInfoAdapter.getCount() > 0) {
                matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_BACK, matterReplyInfoAdapter.getItem(matterReplyInfoAdapter.getCount()-1).replayID);
            } else {
                matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_INIT, 0);
            }

        } else {

            Toast.makeText(getApplicationContext(),
                    "数据提交失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onSoundUploadResult(int result, String soundUrl) {

        if (result == SUCCESS) {
            // 成功
            content = soundUrl;
            type = CommonDef.REPLAY_TYPE_SOUND;

            processHander.sendEmptyMessage(2);

        } else {

            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }

            Toast.makeText(getApplicationContext(),
                    "数据提交失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPicUploadResult(int result, String picUrl) {

        if (result != SUCCESS) {

            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }

            Toast.makeText(getApplicationContext(),
                    "图片上传失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

            return;

        }

        content = picUrl;
        processHander.sendEmptyMessage(2);

    }

    private void playSound(final View v, MatterReplyInfo matterReplyInfo) {

        // 获取URL路径里面的声音ID，就是URL的最后一个内容
        int start = matterReplyInfo.replyContent.lastIndexOf("/");
        if (start == -1) {
            return;
        }

        String soundID = matterReplyInfo.replyContent.substring(start)+".amr";


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
            http.download(matterReplyInfo.replyContent, APPDataInfo.DOWN_SOUND_LOCAL_DIR+soundID, true, false, new RequestCallBack<File>() {
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
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//            String action = intent.getAction();
//            if (action.equals(CommonDef.ACTION_MATTER_REPLAY)) {
//            }

            String matterID = (String) intent.getSerializableExtra("matter_id");
            if (matterID != null && matterID.equals(matterInfo.matterID)) {
                if (matterReplyInfoAdapter.getCount() > 0) {
                    matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_BACK, matterReplyInfoAdapter.getItem(matterReplyInfoAdapter.getCount()-1).replayID);
                } else {
                    matterInfoProcess.matterInfoReplayReq(matterInfo, CommonDef.REPLAY_LOAD_TYPE_INIT, 0);
                }

                abortBroadcast();
            }

        }

    };

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(CommonDef.ACTION_MATTER_REPLAY);
        myIntentFilter.setPriority(1000);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 执行touch 事件
        super.onTouchEvent(event);
        return this.detector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //先执行滑屏事件
        detector.onTouchEvent(ev);
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

//        if (e1.getX() - e2.getX() < -120) {
//            ReplayActivity.this.finish();
//        }

        return false;

    }


    private static final int IMAGE_REQUEST_CODE = 10;
    private static final int CAMERA_REQUEST_CODE = 11;
    private static final int RESULT_REQUEST_CODE = 12;
    private String[] items = new String[] { "本地图片","拍照" };
    /*头像名称*/
    private static final String IMAGE_FILE_NAME = APPDataInfo.REPLAY_PIC_DIR + "image.jpg";
    private static final String IMAGE_UP_FILE_NAME = APPDataInfo.REPLAY_PIC_DIR + "imageUp.jpg";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case IMAGE_REQUEST_CODE:
            {
                if (resultCode == RESULT_CANCELED) {
                    break;
                }

                startPhotoZoom(data.getData());
                break;
            }
            case CAMERA_REQUEST_CODE:
            {
                if (resultCode == RESULT_CANCELED) {
                    break;
                }

                if (PreferenceUtils.hasSdcard()) {
                    File tempFile = new File(IMAGE_FILE_NAME);
                    startPhotoZoom(Uri.fromFile(tempFile));
                } else {
                    Toast.makeText(context, "未找到存储卡，无法存储照片！",Toast.LENGTH_LONG).show();
                }

                break;
            }
            case RESULT_REQUEST_CODE:
            {
                if (resultCode == RESULT_CANCELED) {
                    break;
                }

                if (data != null) {
                    getImageToView(data);
                }

                break;
            }

        }

    }

    private void showDialog() {
        new AlertDialog.Builder(context)
                .setTitle("选择图片")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                Intent intentFromGallery = new Intent();
                                intentFromGallery.setType("image/*");
                                // 设置文件类型
                                intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intentFromGallery,IMAGE_REQUEST_CODE);
                                break;
                            case 1:
                                Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                // 判断存储卡是否可以用，可用进行存储
                                if (PreferenceUtils.hasSdcard()) {
                                    intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(new File(IMAGE_FILE_NAME)));
                                }
                                startActivityForResult(intentFromCapture,CAMERA_REQUEST_CODE);
                                break;
                        }

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        // 设置裁剪
        intent.putExtra("crop","true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX",3);
        intent.putExtra("aspectY",4);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX",240);
        intent.putExtra("outputY",320);
        intent.putExtra("noFaceDetection", true);// 取消默认的人脸识别比例
        intent.putExtra("scale", true);//黑边
        intent.putExtra("scaleUpIfNeeded", true);//黑边
        intent.putExtra("return-data",true);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    private void getImageToView(Intent data){
        Bundle extras = data.getExtras();
        if(extras != null) {

            Bitmap photo = extras.getParcelable("data");

            File file=new File(IMAGE_UP_FILE_NAME);
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                type = CommonDef.REPLAY_TYPE_PIC;
                processHander.sendEmptyMessage(1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
