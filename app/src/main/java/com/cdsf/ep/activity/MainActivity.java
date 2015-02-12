package com.cdsf.ep.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.cdsf.ep.R;
import com.cdsf.ep.application.MyApplication;
import com.cdsf.ep.baidu.MyPushMessageReceiver;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.ChannelIdSyncProcess;
import com.tool.updateApk.UpdateManager;

public class MainActivity extends TabActivity implements View.OnClickListener, MyPushMessageReceiver.OnPushStatusListener, ChannelIdSyncProcess.OnResultListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int RE_START = 0x000001;
    public static final int RE_SYNC = 0x000002;

    // 再按一次超时时间
    private static final int EXIT_TIME_OUT = 2000;
    private long exitTime = 0;

    private TabHost tabhost;

    // 项目按钮
    private ImageView rb_progect;
    // 个人中心按钮
    private ImageView rb_personal_center;
    // 通道同步出来过程
    private ChannelIdSyncProcess channelIdSyncProcess;
    private Handler mHandler = null;

    private String mappid , muserId, mchannelId, mrequestId;
    private boolean syncFlg = true;

    private MyThread myThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rb_progect = (ImageView) findViewById(R.id.rbMainProgect);
        rb_personal_center = (ImageView) findViewById(R.id.rbMainMyInfo);

        rb_progect.setOnClickListener(this);
        rb_personal_center.setOnClickListener(this);

        channelIdSyncProcess = new ChannelIdSyncProcess();
        channelIdSyncProcess.setResultListener(this);

        tabhost = this.getTabHost();
        // 添加标签页：主页面、用户页面
        tabhost.addTab(tabhost.newTabSpec("Progect").setIndicator("Progect")
                .setContent(new Intent(this, ProgectActivity.class)));

        tabhost.addTab(tabhost.newTabSpec("User").setIndicator("User")
                .setContent(new Intent(this, PersonalCenterActivity.class)));

        MyPushMessageReceiver.setOnPushStatusListener(this);
        Log.e("------", "启动推送........"); 
        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                MyApplication.getMetaValue(getApplicationContext(),"api_key"));

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {

                    case RE_START: {

                        if (PushManager.isPushEnabled(getApplicationContext()))
                        {
                            PushManager.startWork(getApplicationContext(),
                                    PushConstants.LOGIN_TYPE_API_KEY,
                                    MyApplication.getMetaValue(getApplicationContext(),"api_key"));
                        }

                        break;
                    }
                    case RE_SYNC: {

                        if (!isSyncFlg()) {

                        }


                        break;
                    }
                }

                super.handleMessage(msg);
            }
        };

        myThread = new MyThread();
    }

    @Override
    protected void onDestroy() {
        MyPushMessageReceiver.setOnPushStatusListener(null);
        PushManager.stopWork(getApplicationContext());
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.rbMainProgect:
            {
                tabhost.setCurrentTabByTag("Progect");
                rb_progect.setBackgroundResource(R.drawable.ep_selected);
                rb_personal_center.setBackgroundResource(R.drawable.me_nomal);

                break;
            }
            case R.id.rbMainMyInfo:
            {
                tabhost.setCurrentTabByTag("User");
                rb_progect.setBackgroundResource(R.drawable.ep_nomal);
                rb_personal_center.setBackgroundResource(R.drawable.me_selected);

                break;
            }
            default:
                break;
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            // 如果是在用户页面，则跳转到主页面
            if (tabhost.getCurrentTabTag().equals("User")) {

                tabhost.setCurrentTabByTag("Progect");

                return true;
            }

            // 处理程序退出
            if(Math.abs(System.currentTimeMillis() - exitTime) > EXIT_TIME_OUT){

                Toast.makeText(getApplicationContext(),
                        "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();

                exitTime = System.currentTimeMillis();

            } else {
                moveTaskToBack(true);
            }

            return true;
        }

        return super.dispatchKeyEvent(event);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBindStatus(int code, String appid, String userId, String channelId, String requestId) {

        Log.e("------", "绑定返回........");

        myThread.start();
        if (code == 0) {
            mappid = appid;
            muserId = userId;
            mchannelId = channelId;
            mrequestId = requestId;
            Log.e("------", "绑定成功........");
            // 通道同步请求
            channelIdSyncProcess.syncReq(mappid, muserId, mchannelId, mrequestId);
        } else {
            Log.e("------", "绑定失败........");
            // 绑定失败，需要重新绑定
            Message msg = new Message();
            msg.what = RE_START;
            mHandler.sendMessage(msg);
        }

    }

    @Override
    public void onUnBindStatus(int code, String requestId) {
    }

    @Override
    public void onSyncResult(int result) {

        if (result == SUCCESS) {
            setSyncFlg(true);
        } else {
            setSyncFlg(false);
        }

    }

    synchronized public boolean isSyncFlg() {
        return syncFlg;
    }

    synchronized public void setSyncFlg(boolean syncFlg) {
        this.syncFlg = syncFlg;
    }

    public class MyThread extends Thread {

        public void run() {

            while (!isSyncFlg()) {

                if (!isSyncFlg()) {

                    Message msg = new Message();
                    msg.what = RE_SYNC;
                    mHandler.sendMessage(msg);

                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
