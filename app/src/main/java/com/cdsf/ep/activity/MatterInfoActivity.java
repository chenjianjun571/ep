package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.common.CommonDef;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.MatterProcess;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.squareup.picasso.Picasso;
import com.tool.widget.LoadingDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatterInfoActivity extends Activity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MatterProcess.OnResultListener, GestureDetector.OnGestureListener {

    private static final String TAG = MatterInfoActivity.class.getSimpleName();
    // 返回按钮
    private Button backBtn;
    // 删除
    private Button delBtn;

    // 传递过来的事项信息
    private MatterInfo matterInfo;
    private Context context;
    // 事项处理
    private MatterProcess matterProcess;

    // 事项状态
    private TextView status;
    // 事项进度
    private SeekBar seekBarProcess;
    // 事项文字表述
    private TextView txProcess;
    // 拖动的进度
    private int iProcess = 0;
    // 是否修改进度
    private boolean isMod = false;

    // 重新发起 保存 回复 按钮
    private Button btnSave, btnRestart, btnReply;

    // 数据加载对话框
    LoadingDialog mLoadingDialog;

    // 是否创建人标志
    private boolean isCreateFlg = false;

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    // 声音按钮
    private ImageView sound;
    // 播放动画
    private AnimationDrawable anim;
    // 事项内容（图片）
    private ImageView matterPic;

    // 滑动监听器
    private GestureDetector detector;
    // 是否拖动进度条
    private boolean isOptSeekBar = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matter_info);

        context = this;
        matterInfo = (MatterInfo) getIntent().getSerializableExtra("matterInfo");

        detector = new GestureDetector(this);
        mLoadingDialog = new LoadingDialog(context);

        matterProcess = new MatterProcess();
        matterProcess.setResultListener(this);

        isCreateFlg = matterInfo.matterCreateUserName.equals(APPDataInfo.getInstance().accountInfo.userName);

        initView();

        initData();
    }

    private void initView() {

        btnRestart = (Button) findViewById(R.id.btn_restart);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnReply = (Button) findViewById(R.id.btn_reply);
        btnSave.setOnClickListener(this);
        btnReply.setOnClickListener(this);

        // 创建者头像
        final CircleImageView pic = (CircleImageView) findViewById(R.id.img_btn_matter_info_creater_pic);
        Picasso.with(context).load(matterInfo.matterCreateUserPic)
                .placeholder(R.drawable.def_pic)
                .error(R.drawable.def_pic)
                .into(pic);

        // 创建者ep值
        final ImageView epValue = (ImageView)findViewById(R.id.img_matter_info_creater_ep_value);
        switch (matterInfo.matterCreateEpValue) {
            case CommonDef.EP_VALUE_1: {
                epValue.setBackgroundResource(R.drawable.ep1);
                break;
            }
            case CommonDef.EP_VALUE_2: {
                epValue.setBackgroundResource(R.drawable.ep2);
                break;
            }
            case CommonDef.EP_VALUE_3: {
                epValue.setBackgroundResource(R.drawable.ep3);
                break;
            }
            case CommonDef.EP_VALUE_4: {
                epValue.setBackgroundResource(R.drawable.ep4);
                break;
            }
            case CommonDef.EP_VALUE_5: {
                epValue.setBackgroundResource(R.drawable.ep5);
                break;
            }
            case CommonDef.EP_VALUE_6: {
                epValue.setBackgroundResource(R.drawable.ep6);
                break;
            }
            case CommonDef.EP_VALUE_7: {
                epValue.setBackgroundResource(R.drawable.ep7);
                break;
            }
            case CommonDef.EP_VALUE_8: {
                epValue.setBackgroundResource(R.drawable.ep8);
                break;
            }
        }

        // 创建者姓名
        final TextView name = (TextView) findViewById(R.id.tx_matter_info_creater_name);
        name.setText(matterInfo.matterCreateUserName);

        // 事项进度
        seekBarProcess = (SeekBar) findViewById(R.id.seekbar_process);
        if (matterInfo.matterStatus == CommonDef.MATTER_STATUS_RECV ||
                matterInfo.matterStatus == CommonDef.MATTER_STATUS_ALRM ||
                matterInfo.matterStatus == CommonDef.MATTER_STATUS_TIMEOUT) {
            seekBarProcess.setVisibility(View.VISIBLE);
            seekBarProcess.setOnSeekBarChangeListener(this);
        } else {
            seekBarProcess.setVisibility(View.GONE);
        }

        // 事项状态
        status = (TextView) findViewById(R.id.tx_matter_info_status);

        // 事项文字表述
        txProcess = (TextView) findViewById(R.id.tx_matter_info_process);
        // 记录原始值
        iProcess = matterInfo.matterProcess;

        // 事项类型
        final TextView matterInfoType = (TextView) findViewById(R.id.tx_matter_info_type);
        if (matterInfo.matterType == CommonDef.MATTER_TYPE_SOUND) {
            matterInfoType.setText("语音");
        } else if (matterInfo.matterType == CommonDef.MATTER_TYPE_TEXT){
            matterInfoType.setText("文字");
        } else {
            matterInfoType.setText("图片");
        }

        // 开始时间
        final TextView matterInfoStartTime = (TextView) findViewById(R.id.tx_matter_info_start_date);
        matterInfoStartTime.setText(matterInfo.matterStartTime);

        // 结束时间
        final TextView matterInfoEndTime = (TextView) findViewById(R.id.tx_matter_info_end_date);
        matterInfoEndTime.setText(matterInfo.matterEndTime);

        // 接收人
        final TextView matterInfoRecvPersonnels = (TextView) findViewById(R.id.tx_matter_info_personnels);
        matterInfoRecvPersonnels.setText(matterInfo.matterRecvPersonnels);

        // 事项内容
        final TextView content = (TextView) findViewById(R.id.tx_matter_info_content_text);
        matterPic = (ImageView) findViewById(R.id.imv_matter_list_item_contentt_pic);
        sound = (ImageView) findViewById(R.id.tx_matter_list_item_content_sound);
        if (matterInfo.matterType == CommonDef.MATTER_TYPE_SOUND) {
            content.setVisibility(View.GONE);
            matterPic.setVisibility(View.GONE);
            sound.setVisibility(View.VISIBLE);
            sound.setBackgroundResource(R.anim.play_matter);
            sound.setOnClickListener(this);
        } else if (matterInfo.matterType == CommonDef.MATTER_TYPE_TEXT){
            sound.setVisibility(View.GONE);
            matterPic.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
            content.setText(matterInfo.matterContent);
        } else {
            sound.setVisibility(View.GONE);
            matterPic.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);

            Picasso.with(context).load(matterInfo.matterContent)
                    .placeholder(R.drawable.pic_loading)
                    .error(R.drawable.pic_load_error)
                    .into(matterPic);
        }

        backBtn = (Button) findViewById(R.id.btn_back);
        backBtn.setOnClickListener(this);
        delBtn = (Button) findViewById(R.id.btn_del);
        // 是否创建人查看
        if (isCreateFlg) {
            delBtn.setVisibility(View.VISIBLE);
            btnRestart.setVisibility(View.VISIBLE);
            delBtn.setOnClickListener(this);
            btnRestart.setOnClickListener(this);
        } else {
            delBtn.setVisibility(View.GONE);
            btnRestart.setVisibility(View.GONE);
        }

    }

    private void initData() {

        seekBarProcess.setProgress(iProcess);
        txProcess.setText(iProcess + "%");

        switch (matterInfo.matterStatus) {

            case CommonDef.MATTER_STATUS_INIT: {
                // 待处理
                status.setText("待处理");

                break;
            }
            case CommonDef.MATTER_STATUS_RECV: {
                // 已接收
                status.setText("已接收");

                break;
            }
            case CommonDef.MATTER_STATUS_RE_FUSE: {
                // 已拒绝
                status.setText("已拒绝");

                break;
            }
            case CommonDef.MATTER_STATUS_ALRM: {
                // 告警
                status.setText("告警");

                break;
            }
            case CommonDef.MATTER_STATUS_TIMEOUT: {
                // 超时
                status.setText("超时");

                break;
            }
            case CommonDef.MATTER_STATUS_FINAL: {
                // 完成
                status.setText("完成");

                break;
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_back: {
                MatterInfoActivity.this.finish();
                break;
            }
            case R.id.btn_save: {
                if (!isMod) {
                    // 没有修改的话点击保存不做处理
                    break;
                }

                // 发送事项更新请求
                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.setMessage("数据请求中，请稍后....");
                    mLoadingDialog.show();
                }

                matterProcess.matterProcessReq(matterInfo, iProcess);

                break;
            }
            case R.id.btn_restart: {
                // 发送事项更新请求
                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.setMessage("数据请求中，请稍后....");
                    mLoadingDialog.show();
                }

                // 重新发起事项 相当于修改事项的状态
                matterProcess.matterStatusReq(matterInfo, CommonDef.MATTER_STATUS_INIT);

                break;
            }
            case R.id.btn_reply: {

                Intent intent = new Intent(getApplicationContext(), ReplayActivity.class);
                intent.putExtra("matterInfo", matterInfo);
                MatterInfoActivity.this.startActivity(intent);

                break;
            }
            case R.id.btn_del: {
                new AlertDialog.Builder(context)
                        .setTitle("提示").setMessage("是否删除事项信息？")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // 事项删除
                                if (!mLoadingDialog.isShowing()) {
                                    mLoadingDialog.setMessage("数据请求中，请稍后....");
                                    mLoadingDialog.show();
                                }

                                matterProcess.matterDelReq(matterInfo);

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", null).show();

                break;
            }
            case R.id.tx_matter_list_item_content_sound: {

                playSound(sound);

                break;
            }
        }

    }

    private void playSound(final ImageView view) {

        // 获取URL路径里面的声音ID，就是URL的最后一个内容
        int start = matterInfo.matterContent.lastIndexOf("/");
        if (start == -1) {
            return;
        }

        if (anim != null) {
            anim.stop();
        }
        anim = (AnimationDrawable)view.getBackground();

        String soundID = matterInfo.matterContent.substring(start)+".amr";
        File file = new File(APPDataInfo.DOWN_SOUND_LOCAL_DIR+soundID);
        if (file.exists()) {
            if (file.isFile()) {
                // 有缓存文件，直接播放缓存文件
                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(APPDataInfo.DOWN_SOUND_LOCAL_DIR+soundID);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();

                    anim.start();
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            mp.stop();
                            anim.stop();
                            view.setBackgroundResource(R.anim.play_matter);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
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
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.stop();
                        }
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(fileResponseInfo.result.getPath());
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();

                        anim.start();
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                mp.stop();
                                anim.stop();
                                view.setBackgroundResource(R.anim.play_matter);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (progress != iProcess) {
            isMod = true;
        } else {
            isMod = false;
        }

        iProcess = progress;

        // 记录用户对进度的拖动
        seekBarProcess.setProgress(progress);
        txProcess.setText(progress + "%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isOptSeekBar = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isOptSeekBar = false;
    }

    @Override
    public void onMatterReqResult(int result, int reqType, List<MatterInfo> matterInfoArrayList) {
    }

    @Override
    public void onMatterStatusReqResult(int result, MatterInfo matterInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (result == SUCCESS) {

            // 成功
            DBProcMgr.getInstance().updateMatterInfo(matterInfo);

            initData();

        } else {

            Toast.makeText(getApplicationContext(),
                    "数据同步失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onMatterProcessReqResult(int result, MatterInfo matterInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                Toast.makeText(getApplicationContext(),
                        "事项修改成功",
                        Toast.LENGTH_SHORT).show();

                // 保存数据库状态
                DBProcMgr.getInstance().saveOneMatterInfo(matterInfo);

                initData();

                break;
            }
            case FAILURE: {

                Toast.makeText(getApplicationContext(),
                        "事项修改失败",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }

    }

    @Override
    public void onMatterReplayReqResult(int result, MatterInfo matterInfo, MatterReplyInfo matterReplyInfo) {
    }

    @Override
    public void onQueryPersonalsResult(int result, ArrayList<PersonnelInfo> personnelInfos) {
    }

    @Override
    public void onMatterAddReqResult(int result, MatterInfo matterInfo) {
    }

    @Override
    public void onMatterDelReqResult(int result, MatterInfo matterInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (result == SUCCESS) {

            // 删除数据库信息
            DBProcMgr.getInstance().delOneMatterInfo(matterInfo);

            Toast.makeText(getApplicationContext(),
                    "删除成功，请返回事项列表界面查看",
                    Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(getApplicationContext(),
                    "数据提交失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onSoundUploadResult(int result, String soundID, MatterInfo matterInfo, String users) {
    }

    @Override
    public void onPicUploadResult(int result, String picID, MatterInfo matterInfo, String users) {

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

        // 如果是在拖动进度条，这里忽略滑动
        if (isOptSeekBar) {
            return false;
        }

        if (e1.getX() - e2.getX() > 120) {
            Intent intent = new Intent(getApplicationContext(), ReplayActivity.class);
            intent.putExtra("matterInfo", matterInfo);
            MatterInfoActivity.this.startActivity(intent);
        }

        return false;

    }
}
