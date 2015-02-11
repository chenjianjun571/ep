package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.MatterInfo;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.cdsf.ep.bean.ModInfo;
import com.cdsf.ep.bean.ModInfoResult;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.bean.ProjectInfo;
import com.cdsf.ep.common.CommonDef;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.MatterProcess;
import com.squareup.picasso.Picasso;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatterAddActivity extends Activity implements View.OnClickListener,
        MatterProcess.OnResultListener,
        View.OnTouchListener {

    Context mContext;

    // 返回按钮
    public Button btnBack;
    // 保存按钮
    public Button btnSave;

    // 录音提示层
    private View rcChat_popup;
    private LinearLayout voice_rcd_hint_rcding, voice_rcd_hint_tooshort;
    private boolean isShosrt = false;
    private long startVoiceT, endVoiceT;
    private SoundMeter mSensor;
    private ImageView volume;

    // 事项创建者URL
    private CircleImageView civCreater;
    // 事项创建者名称
    private TextView txCreater;
    // 事项类型
    private RadioGroup rgMatterType;
    private int matterType;
    // 开始时间
    private TextView matterStartTime;
    // 结束时间
    private TextView matterEndTime;

    // 事项内容(文本)
    private TextView matterContentText;
    // 事项内容(语音)
    private TextView matterContentSound;
    // 事项内容(图片)
    private ImageView matterContentPic;
    // 事项内容
    private String matterContent;

    // 人员列表
    private GridView gvPersonals;
    // 列表数据源
    protected QuickAdapter<PersonnelInfo> mProjectUserInfoAdapter;
    // 用户人员剔重
    private HashMap<String, PersonnelInfo> mProjectUserInfoHashMap;

    // 传递过来的项目信息
    private ProjectInfo projectInfo;

    // 数据加载对话框
    LoadingDialog mLoadingDialog;

    // 逻辑出来过程
    MatterProcess matterProcess;



    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matter_add);

        mContext = this;
        mLoadingDialog = new LoadingDialog(mContext);
        matterProcess = new MatterProcess();
        matterProcess.setResultListener(this);

        mSensor = new SoundMeter();

        projectInfo = (ProjectInfo) getIntent().getSerializableExtra("projectInfo");

        initView();
        initData();

        // 删除残留文件
        File file = new File(APPDataInfo.MATTER_SOUND_LOCAL_PATH);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    private void initView() {
        // 返回按钮
        btnBack = (Button)findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        // 保存按钮
        btnSave = (Button)findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        // 录音提示层
        rcChat_popup = findViewById(R.id.rcChat_popup);
        voice_rcd_hint_rcding = (LinearLayout)findViewById(R.id.voice_rcd_hint_rcding);
        voice_rcd_hint_tooshort = (LinearLayout)findViewById(R.id.voice_rcd_hint_tooshort);
        volume = (ImageView)findViewById(R.id.volume);

        // 事项创建者头像
        civCreater = (CircleImageView)findViewById(R.id.ic_createter_pic);
        Picasso.with(mContext).load(APPDataInfo.getInstance().accountInfo.userPic)
                .placeholder(R.drawable.def_pic)
                .error(R.drawable.def_pic)
                .into(civCreater);

        // 创建者ep值
        final ImageView epValue = (ImageView)findViewById(R.id.img_creater_ep_value);
        switch (APPDataInfo.getInstance().accountInfo.epValue) {
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

        // 事项创建者名称
        txCreater = (TextView)findViewById(R.id.tx_createter);
        txCreater.setText(APPDataInfo.getInstance().accountInfo.userName);

        // 事项内容
        matterContentText = (TextView)findViewById(R.id.tx_matter_content_text);
        matterContentText.setOnClickListener(this);
        matterContentPic = (ImageView)findViewById(R.id.imv_matter_content_pic);
        matterContentPic.setOnClickListener(this);
        matterContentSound = (TextView)findViewById(R.id.tx_matter_content_sound);
        matterContentSound.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!Environment.getExternalStorageDirectory().exists()) {
                    Toast.makeText(getApplicationContext(), "No SDCard", Toast.LENGTH_LONG).show();
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                        // 按下事件
                        matterContentSound.setBackgroundResource(R.drawable.voice_rcd_btn_pressed);
                        rcChat_popup.setVisibility(View.VISIBLE);
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
                        matterContentSound.setBackgroundResource(R.drawable.voice_rcd_btn_nor);
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
                                    rcChat_popup.setVisibility(View.GONE);
                                    isShosrt = false;
                                }
                            }, 500);
                            return false;
                        }

                        rcChat_popup.setVisibility(View.GONE);

                        break;
                    }
                    default: {
                        return false;
                    }
                }

                return true;
            }
        });

        // 事项类型
        rgMatterType = (RadioGroup)findViewById(R.id.rg_matter_type);
        final RadioButton rbText = (RadioButton)findViewById(R.id.rb_type_text);
        final RadioButton rbSound = (RadioButton)findViewById(R.id.rb_type_sound);
        final RadioButton rbPic = (RadioButton)findViewById(R.id.rb_type_pic);
        // 设置默认为文本
        matterType = CommonDef.MATTER_TYPE_TEXT;
        rgMatterType.check(rbText.getId());
        rgMatterType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // 删除残留文件
                File file = new File(APPDataInfo.REPLAY_SOUND_LOCAL_PATH);
                if (file.exists()) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
                File file1 = new File(IMAGE_UP_FILE_NAME);
                if (file1.exists()) {
                    if (file1.isFile()) {
                        file1.delete();
                    }
                }

                if (checkedId == rbText.getId()) {
                    matterType = CommonDef.MATTER_TYPE_TEXT;
                    matterContentText.setVisibility(View.VISIBLE);
                    matterContentPic.setVisibility(View.GONE);
                    matterContentSound.setVisibility(View.GONE);
                } else if (checkedId == rbSound.getId()) {
                    matterType = CommonDef.MATTER_TYPE_SOUND;
                    matterContentSound.setVisibility(View.VISIBLE);
                    matterContentPic.setVisibility(View.GONE);
                    matterContentText.setVisibility(View.GONE);
                } else if (checkedId == rbPic.getId()) {
                    matterType = CommonDef.MATTER_TYPE_PIC;
                    matterContentSound.setVisibility(View.GONE);
                    matterContentPic.setVisibility(View.VISIBLE);
                    matterContentText.setVisibility(View.GONE);
                }

            }
        });

        // 开始时间
        matterStartTime = (TextView)findViewById(R.id.tx_matter_start_time);
        matterStartTime.setOnTouchListener(this);

        // 结束时间
        matterEndTime = (TextView)findViewById(R.id.tx_matter_end_time);
        matterEndTime.setOnTouchListener(this);

        gvPersonals = (GridView) findViewById(R.id.gv_matter_add_personnel);
        mProjectUserInfoHashMap = new HashMap<String, PersonnelInfo>();
        mProjectUserInfoAdapter = new QuickAdapter<PersonnelInfo>(mContext, R.layout.personal_grid_item) {
            @Override
            protected void convert(BaseAdapterHelper helper, PersonnelInfo item) {
                final ImageView delView = helper.getView(R.id.img_item_del);
                delView.setTag(helper);
                delView.setTag(R.id.project_grid_view_item_user_info, item);

                delView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 删除
                        PersonnelInfo projectUserInfo = (PersonnelInfo) v.getTag(R.id.project_grid_view_item_user_info);
                        remove(projectUserInfo);
                        mProjectUserInfoHashMap.remove(projectUserInfo.userID);
                    }
                });

                if (item.isEnd) {
                    // 是最后一个
                    helper.setImageLoaclResID(R.id.cimg_personal_pic, R.drawable.add_user);
                    helper.setText(R.id.tx_personal_name, "添加");
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
                        helper.setVisible(R.id.img_item_del, true);
                    } else {
                        helper.setVisible(R.id.img_item_del, false);
                    }
                }
            }
        };
        gvPersonals.setAdapter(mProjectUserInfoAdapter);

        // 设置单击监听事件
        gvPersonals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                PersonnelInfo projectUserInfo = (PersonnelInfo) mProjectUserInfoAdapter.getItem(position);
                if (!projectUserInfo.isEnd) {

                    // 如果之前是删除，用户点击其他区域取消删除状态
                    if (projectUserInfo.isDel) {
                        projectUserInfo.isDel = false;
                        mProjectUserInfoAdapter.notifyDataSetChanged();
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
                    matterProcess.projectQueryPersonalsReq(projectInfo);
                }

            }
        });

        // 设置长按监听事件
        gvPersonals.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                PersonnelInfo projectUserInfo = (PersonnelInfo) mProjectUserInfoAdapter.getItem(position);
                if (projectUserInfo.isEnd) {
                    // 最后一个是功能按钮，不支持长按
                    return false;
                }

                projectUserInfo.isDel = true;
                mProjectUserInfoAdapter.notifyDataSetChanged();

                return true;

            }
        });
    }


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

        File file = new File(APPDataInfo.MATTER_SOUND_LOCAL_PATH);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            }
        }

        if (!mSensor.start(APPDataInfo.MATTER_SOUND_LOCAL_PATH)) {
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

    private void initData() {

        // 一个加号的图标
        PersonnelInfo projectUserInfo = new PersonnelInfo();
        projectUserInfo.isEnd = true;
        mProjectUserInfoAdapter.addFront(projectUserInfo);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        matterStartTime.setText(String.format("%d-%02d-%02d %02d:%02d:00", year, month, day, hour, minute));
        matterEndTime.setText(String.format("%d-%02d-%02d %02d:%02d:00", year, month, day, hour+1, minute));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_back:
            {
                MatterAddActivity.this.finish();
                break;
            }
            case R.id.btn_save:
            {
                if (matterStartTime.getText().toString().length() < 1 ) {
                    Toast.makeText(getApplicationContext(),
                            "事项开始时间不能为空",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                if (matterEndTime.getText().toString().length() < 1 ) {
                    Toast.makeText(getApplicationContext(),
                            "事项结束时间不能为空",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                {
                    // 判断时间
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date startTime = null;
                    java.util.Date endTime= null;
                    try {
                        startTime = df.parse(matterStartTime.getText().toString());
                        endTime = df.parse(matterEndTime.getText().toString());
                        if (startTime.getTime()-endTime.getTime() >= 0) {
                            Toast.makeText(getApplicationContext(),
                                    "事项结束时间不能小于等于开始时间",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        break;
                    }
                }

                if (mProjectUserInfoAdapter.getCount() == 1) {
                    Toast.makeText(getApplicationContext(),
                            "请选择接收人员",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                if (matterType == CommonDef.MATTER_TYPE_SOUND) {
                    // 如果用户选择的是语音
                    File file = new File(APPDataInfo.MATTER_SOUND_LOCAL_PATH);
                    if (!file.exists()) {
                        Toast.makeText(getApplicationContext(),
                                "亲，选择了语音就秀一段吧...",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                } else if (matterType == CommonDef.MATTER_TYPE_TEXT) {
                    // 用户选择的是文本
                    if (matterContentText.getText().toString().length() > 1024) {

                        Toast.makeText(getApplicationContext(),
                                "事项内容不能超过1024个字符",
                                Toast.LENGTH_SHORT).show();

                        break;
                    }
                } else {

                    // 如果用户选择的是图片
                    File file = new File(IMAGE_UP_FILE_NAME);
                    if (!file.exists()) {
                        Toast.makeText(getApplicationContext(),
                                "亲，选择了图片就秀一张吧...",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

                saveProcess();

                break;
            }
            case R.id.tx_matter_content_text:
            {
                // 事项内容
                ModInfo modInfo = new ModInfo();

                modInfo.type = ModInfo.TY_TEXT;
                modInfo.title = "事项内容";
                modInfo.content = matterContentText.getText().toString();
                // 项目名称
                Intent intent = new Intent(MatterAddActivity.this, ModInfoActivity.class);
                intent.putExtra("data", modInfo);

                // 1是标识谁调用，在回调回来得时候根据这个标签来区别
                startActivityForResult(intent, 4);

                break;
            }
            case R.id.imv_matter_content_pic:
            {
                showDialog();

                break;
            }
        }
    }

    private void saveProcess() {


        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.setMessage("数据请求中，请稍后....");
            mLoadingDialog.show();
        }

        // 事项信息
        MatterInfo matterInfo = new MatterInfo();
        // 项目ID
        matterInfo.projectID = projectInfo.projectID;
        // 事项类型
        matterInfo.matterType = matterType;
        // 事项内容
        matterInfo.matterContent = matterContentText.getText().toString();
        // 事项开始时间
        matterInfo.matterStartTime = matterStartTime.getText().toString();
        // 事项结束时间
        matterInfo.matterEndTime = matterEndTime.getText().toString();
        // 参与人员
        String str = "";
        for (int i = 0; i < mProjectUserInfoAdapter.getCount(); ++i)
        {
            if (mProjectUserInfoAdapter.getItem(i).isEnd) {
                continue;
            }

            if (mProjectUserInfoAdapter.getItem(i).userID.equals(APPDataInfo.getInstance().accountInfo.userID)) {
                continue;
            }

            if (!str.equals("")) {
                str += ",";
            }
            str += mProjectUserInfoAdapter.getItem(i).userID;
        }

        if (matterType == CommonDef.MATTER_TYPE_TEXT) {
            // 事项添加
            matterProcess.matterAddReq(matterInfo, str);
        } else if (matterType == CommonDef.MATTER_TYPE_SOUND) {
            // 先传声音文件
            matterProcess.soundUploadReq(APPDataInfo.MATTER_SOUND_LOCAL_PATH, matterInfo, str);
        } else {
            // 先传图片文件
            matterProcess.picUploadReq(IMAGE_UP_FILE_NAME, matterInfo, str);
        }

    }

    /**
     * 所有的Activity对象的返回值都是由这个方法来接收
     * requestCode:    表示的是启动一个Activity时传过去的requestCode值
     * resultCode：表示的是启动后的Activity回传值时的resultCode值
     * data：表示的是启动后的Activity回传过来的Intent对象
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 4: { // 事项内容回来的

                if (resultCode == 0) {
                    ModInfoResult result_value = (ModInfoResult)data.getSerializableExtra("result");
                    matterContentText.setText(result_value.result);
                }

                break;
            }
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
                    Toast.makeText(mContext, "未找到存储卡，无法存储照片！",Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onQueryPersonalsResult(int result, final ArrayList<PersonnelInfo> projectUserInfos) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                final String[] personals = new String[projectUserInfos.size()];
                final boolean[] personalsSelected = new boolean[projectUserInfos.size()];

                for (int i = 0; i < projectUserInfos.size(); ++i) {
                    personals[i] = projectUserInfos.get(i).userName;
                    personalsSelected[i] = false;
                }

                Dialog alertDialog = new AlertDialog.Builder(mContext).
                        setTitle("人员列表").
                        setIcon(R.drawable.login_user)
                        .setMultiChoiceItems(personals, personalsSelected, new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                personalsSelected[which] = isChecked;
                            }
                        }).setPositiveButton("确认", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                for (int i = 0; i < personalsSelected.length; i++) {

                                    if (personalsSelected[i] == true) {

                                        if (mProjectUserInfoHashMap.get(projectUserInfos.get(i).userID) != null) {
                                            continue;
                                        }

                                        // 如果事项接收人是自己也不处理
                                        if (projectUserInfos.get(i).userName.equals(APPDataInfo.getInstance().accountInfo.userName)) {
                                            continue;
                                        }

                                        mProjectUserInfoAdapter.addFront(projectUserInfos.get(i));
                                        mProjectUserInfoHashMap.put(projectUserInfos.get(i).userID, projectUserInfos.get(i));
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
    public void onMatterAddReqResult(int result, MatterInfo matterInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (result == SUCCESS) {

            Toast.makeText(getApplicationContext(),
                    "添加成功，请刷新界面查看",
                    Toast.LENGTH_SHORT).show();

            MatterAddActivity.this.finish();

        } else {

            Toast.makeText(getApplicationContext(),
                    "数据提交失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

        }
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

        matterInfo.matterContent = soundID;
        // 事项添加
        matterProcess.matterAddReq(matterInfo, users);

    }

    @Override
    public void onPicUploadResult(int result, String picURL, MatterInfo matterInfo, String users) {

        if (result != SUCCESS) {

            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }

            Toast.makeText(getApplicationContext(),
                    "图片上传失败，请稍后再试...",
                    Toast.LENGTH_SHORT).show();

            return;

        }

        matterInfo.matterContent = picURL;
        // 事项添加
        matterProcess.matterAddReq(matterInfo, users);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = View.inflate(this, R.layout.date_time_dialog, null);
            final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
            final TimePicker timePicker = (android.widget.TimePicker) view.findViewById(R.id.time_picker);
            builder.setView(view);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(Calendar.MINUTE);

            if (v.getId() == R.id.tx_matter_start_time) {

                builder.setTitle("选取起始时间");
                builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        StringBuffer sb = new StringBuffer();
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        sb.append("  ");
                        sb.append(timePicker.getCurrentHour())
                                .append(":").append(timePicker.getCurrentMinute()).append(":00");

                        matterStartTime.setText(sb);

                        dialog.cancel();
                    }
                });

            } else if (v.getId() == R.id.tx_matter_end_time) {

                builder.setTitle("选取结束时间");
                builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        StringBuffer sb = new StringBuffer();
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        sb.append("  ");
                        sb.append(timePicker.getCurrentHour())
                                .append(":").append(timePicker.getCurrentMinute()).append(":00");
                        matterEndTime.setText(sb);

                        dialog.cancel();
                    }
                });
            }

            Dialog dialog = builder.create();
            dialog.show();
        }

        return true;
    }


    private static final int IMAGE_REQUEST_CODE = 10;
    private static final int CAMERA_REQUEST_CODE = 11;
    private static final int RESULT_REQUEST_CODE = 12;

    private String[] items = new String[] { "本地图片","拍照" };
    /*头像名称*/
    private static final String IMAGE_FILE_NAME = APPDataInfo.PIC_LOCAL_DIR + "matterImage.jpg";
    private static final String IMAGE_UP_FILE_NAME = APPDataInfo.PIC_LOCAL_DIR + "matterImageUp.jpg";

    private void showDialog() {
        new AlertDialog.Builder(mContext)
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
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX",320);
        intent.putExtra("outputY",320);
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
                matterContentPic.setImageResource(R.drawable.def_project_pic);
                matterContentPic.setImageURI(Uri.fromFile(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
