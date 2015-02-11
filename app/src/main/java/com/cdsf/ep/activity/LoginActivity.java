package com.cdsf.ep.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.cdsf.ep.R;
import com.cdsf.ep.application.MyApplication;
import com.cdsf.ep.baidu.MyPushMessageReceiver;
import com.cdsf.ep.common.SignalEventDef;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.LoginProcess;
import com.tool.PreferenceUtils;
import com.tool.updateApk.UpdateManager;
import com.tool.widget.LoadingDialog;

public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    // 登录按钮
    private Button longinBtn;
    // 用户名
    private EditText userName;
    // 密码
    private EditText userPasswd;

    // 登录业务处理逻辑
    LoginProcess mLoginProcess;

    // 登录框
    LoadingDialog mLoadingDialog;
    // 设置
    ImageView mIVSetting;

    private UpdateManager mUpdateManager;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 检查目录是否存在,不存在就创建
        PreferenceUtils.checkDirsExists(APPDataInfo.PIC_LOCAL_DIR);
        PreferenceUtils.checkDirsExists(APPDataInfo.SOUND_LOCAL_DIR);
        PreferenceUtils.checkDirsExists(APPDataInfo.DOWN_SOUND_LOCAL_DIR);
        PreferenceUtils.checkDirsExists(APPDataInfo.REPLAY_PIC_DIR);

        mContext = this;
        longinBtn = (Button)findViewById(R.id.loginBtn);
        longinBtn.setOnClickListener(this);

        mIVSetting = (ImageView)findViewById(R.id.ivSetting);
        mIVSetting.setOnClickListener(this);

        userName = (EditText) findViewById(R.id.loginUser);
        userPasswd = (EditText) findViewById(R.id.loginPasswd);

        if (APPDataInfo.getInstance().accountInfo != null) {
            userName.setText(APPDataInfo.getInstance().accountInfo.userName);
            userPasswd.setText(APPDataInfo.getInstance().accountInfo.passWord);
            userName.setFocusable(true);
            userName.setSelection(userName.length());
        }

        mLoginProcess = new LoginProcess();
        mLoginProcess.setOnClickListener(new LoginProcess.OnSendMsgListener() {
            @Override
            public void onSendNextMsg(Message msg) {
                myHander.sendMessage(msg);
            }
        });
        mLoadingDialog = new LoadingDialog(this);

        myHander.sendEmptyMessage(SignalEventDef.SE_LOGIN);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            return true;
        }

        return super.dispatchKeyEvent(event);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.loginBtn: {

                if (APPDataInfo.BASE_URL == null) {

                    Toast.makeText(getApplicationContext(),
                            "提示：请设置服务器信息！",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, SettingPopupWindow.class);
                    LoginActivity.this.startActivity(intent);

                    break;
                }

                if(userName.length() < 1 || userPasswd.length() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                            "请输入用户名和密码",
                            Toast.LENGTH_SHORT).show();

                    break;
                }

                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.setMessage("登录中,请稍等...");
                    mLoadingDialog.show();
                }

                // 执行自动登录
                mLoginProcess.login(userName.getText().toString(),
                        userPasswd.getText().toString());

                break;
            }
            case R.id.ivSetting: {

                Intent intent = new Intent(LoginActivity.this, SettingPopupWindow.class);
                LoginActivity.this.startActivity(intent);

                break;
            }

        }

    }

    private Handler myHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case SignalEventDef.SE_LOGIN: {

                    if (APPDataInfo.BASE_URL == null) {

                        Toast.makeText(getApplicationContext(),
                                "提示：请设置服务器信息！",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, SettingPopupWindow.class);
                        LoginActivity.this.startActivity(intent);

                        break;
                    }

                    if (userName.getText().toString().length() > 0 && userPasswd.getText().toString().length() > 0) {

                        if (!mLoadingDialog.isShowing()) {
                            mLoadingDialog.setMessage("登录中,请稍等...");
                            mLoadingDialog.show();
                        }

                        // 执行自动登录
                        mLoginProcess.login(userName.getText().toString(),
                                userPasswd.getText().toString());

                    }

                    break;
                }
                case SignalEventDef.SE_LOGIN_SUSESS: {

                    if(mLoadingDialog.isShowing())
                    {
                        mLoadingDialog.dismiss();
                    }

                    // 检查检查更新
                    mUpdateManager = new UpdateManager(mContext);
                    if (!mUpdateManager.checkUpdateInfo()) {
                        // 登录成功 并且没有版本更新
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        LoginActivity.this.startActivity(intent);
                        LoginActivity.this.finish();
                    }

                    break;
                }
                case SignalEventDef.SE_LOGIN_FAILURE: {

                    if(mLoadingDialog.isShowing())
                    {
                        mLoadingDialog.dismiss();
                    }

                    Toast.makeText(getApplicationContext(),
                            (String)msg.obj,
                            Toast.LENGTH_LONG).show();

                    break;

                }
            }

        }

    };


//    /**
//     * 如果没有绑定百度云，则绑定，并记录在属性文件中
//     */
//    private void autoBindBaiduYunTuiSong()
//    {
////        if (!PreferenceUtils.hasBind(getApplicationContext()))
////        {
////            if (!mLoadingDialog.isShowing()) {
////                mLoadingDialog.setMessage("初期化中,请稍等...");
////                mLoadingDialog.show();
////            }
////
////            PushManager.startWork(getApplicationContext(),
////                    PushConstants.LOGIN_TYPE_API_KEY,
////                    MyApplication.getMetaValue(LoginActivity.this,
////                            "api_key"));
////
////            inigFlg = true;
////        }
////        else
////        {
////            if (PushManager.isPushEnabled(getApplicationContext()))
////            {
////                if (!mLoadingDialog.isShowing()) {
////                    mLoadingDialog.setMessage("初期化中,请稍等...");
////                    mLoadingDialog.show();
////                }
////
////                PushManager.startWork(getApplicationContext(),
////                        PushConstants.LOGIN_TYPE_API_KEY,
////                        MyApplication.getMetaValue(LoginActivity.this,
////                                "api_key"));
////
////                inigFlg = true;
////
////            } else {
////                myHander.sendEmptyMessage(SignalEventDef.SE_LOGIN);
////            }
////        }
//        if (PushManager.isPushEnabled(getApplicationContext()))
//        {
//            if (!mLoadingDialog.isShowing()) {
//                mLoadingDialog.setMessage("初期化中,请稍等...");
//                mLoadingDialog.show();
//            }
//
//            PushManager.startWork(getApplicationContext(),
//                    PushConstants.LOGIN_TYPE_API_KEY,
//                    MyApplication.getMetaValue(LoginActivity.this,
//                            "api_key"));
//
//            inigFlg = true;
//
//        } else {
//            myHander.sendEmptyMessage(SignalEventDef.SE_LOGIN);
//        }
//    }
//
//    @Override
//    public void onBindStatus(int code, String appid, String userId, String channelId, String requestId) {
//
//        inigFlg = false;
//
//        if(mLoadingDialog.isShowing())
//        {
//            mLoadingDialog.dismiss();
//        }
//
//        if (code == 0) {
//            // 绑定成功
//            myHander.sendEmptyMessage(SignalEventDef.SE_LOGIN);
//        }
//        else {
//            Message msg = myHander.obtainMessage();
//            msg.what = SignalEventDef.SE_LOGIN_FAILURE;
//            msg.obj = "网络错误";
//            myHander.sendMessage(msg);
//        }
//
//    }
//
//    @Override
//    public void onUnBindStatus(int code, String requestId) {
//    }

}
