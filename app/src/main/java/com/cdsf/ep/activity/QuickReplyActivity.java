package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.bean.ModInfoResult;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.common.CommonDef;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.MatterProcess;
import com.tool.PreferenceUtils;
import com.tool.media.SoundMeter;
import com.tool.widget.LoadingDialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class QuickReplyActivity extends Activity implements View.OnClickListener,
        MatterProcess.OnResultListener {

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

    // 录音显示层
    private LinearLayout lyRcd;
    private LinearLayout voice_rcd_hint_rcding, voice_rcd_hint_tooshort;
    private boolean isShosrt = false;
    private long startVoiceT, endVoiceT;
    private SoundMeter mSensor;
    private ImageView volume;

    // 确定按钮
    private TextView txOK;
    // 取消按钮
    private TextView txCancel;

    // 切换标志
    private boolean flg = true;
    // 回复类型
    private int type = CommonDef.REPLAY_TYPE_TEXT;
    // 回复内容
    private String content = "";

    // 网络处理过程
    private MatterProcess matterProcess;

    // 回复信息
    private MatterInfo matterInfo;
    LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_reply);

        mSensor = new SoundMeter();
        mLoadingDialog = new LoadingDialog(this);
        matterProcess = new MatterProcess();
        matterProcess.setResultListener(this);

        context = this;
        matterInfo = (MatterInfo) getIntent().getSerializableExtra("matterInfo");

        initView();
        intData();

        // 删除残留文件
        File file = new File(APPDataInfo.REPLAY_SOUND_LOCAL_PATH);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    private void initView() {
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

        txOK = (TextView)findViewById(R.id.btn_ok);
        txOK.setOnClickListener(this);

        txCancel = (TextView)findViewById(R.id.btn_cancel);
        txCancel.setOnClickListener(this);

        final LinearLayout ly = (LinearLayout)findViewById(R.id.pop_layout);
        ly.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            }
        });
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

    private void intData() {

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

                //Toast.makeText(getApplicationContext(), "即将开启，敬请期待...", Toast.LENGTH_LONG).show();
                showDialog();

                break;

            }
            case R.id.btn_cancel: {

                Intent intent = new Intent();
                intent.putExtra("result", matterInfo);
                setResult(1, intent);

                QuickReplyActivity.this.finish();

                break;

            }
            case R.id.btn_ok: {

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

                        File file = new File(IMAGE_UP_FILE_NAME);
                        if (!file.exists()) {
                            Toast.makeText(getApplicationContext(), "拍点什么吧!" , Toast.LENGTH_LONG).show();
                            break;
                        }

                        type = CommonDef.REPLAY_TYPE_PIC;
                        processHander.sendEmptyMessage(1);

                        break;
                    }
                }

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
                    matterProcess.soundUploadReq(APPDataInfo.REPLAY_SOUND_LOCAL_PATH, matterInfo, "");

                    break;
                }
                case 1:{

                    // 处理图片上传
                    matterProcess.picUploadReq(IMAGE_UP_FILE_NAME, matterInfo, "");

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

                    matterProcess.matterReplayReq(matterInfo, replyInfo);

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
    public void onMatterReqResult(int result, int reqType, List<MatterInfo> matterInfoArrayList) {
    }

    @Override
    public void onMatterStatusReqResult(int result, MatterInfo matterInfo) {
    }

    @Override
    public void onMatterProcessReqResult(int result, MatterInfo matterInfo) {
    }

    @Override
    public void onMatterReplayReqResult(int result, MatterInfo matterInfo, MatterReplyInfo matterReplyInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (result == SUCCESS) {

            // 成功
            matterInfo.matterReplyInfo = matterReplyInfo;
            Intent intent = new Intent();
            intent.putExtra("result", matterInfo);
            setResult(0, intent);

            QuickReplyActivity.this.finish();

        } else {

            Toast.makeText(getApplicationContext(),
                    "数据提交失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onQueryPersonalsResult(int result, ArrayList<PersonnelInfo> personalInfoArrayList) {
    }

    @Override
    public void onMatterAddReqResult(int result, MatterInfo matterInfo) {
    }

    @Override
    public void onMatterDelReqResult(int result, MatterInfo matterInfo) {
    }

    @Override
    public void onSoundUploadResult(int result, String soundID, MatterInfo matterInfo, String users) {

        if (result != SUCCESS) {

            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }

            Toast.makeText(getApplicationContext(),
                    "录音上传失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

            return;

        }

        content = soundID;
        processHander.sendEmptyMessage(2);

    }

    @Override
    public void onPicUploadResult(int result, String picUrl, MatterInfo matterInfo, String users) {

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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            Intent intent = new Intent();
            intent.putExtra("result", matterInfo);
            setResult(1, intent);

            QuickReplyActivity.this.finish();

            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    //实现onTouchEvent触屏函数但点击屏幕时不销毁本Activity
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
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
