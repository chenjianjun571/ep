package com.cdsf.ep.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.SrvInfo;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;

public class SettingPopupWindow extends Activity {

    private RelativeLayout layout;

    private Button btnOK;

    private String regexIpv4 = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_popup_window);

        layout = (RelativeLayout) findViewById(R.id.pop_layout);

        layout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "提示：点击窗口外部关闭窗口！",
                        Toast.LENGTH_SHORT).show();
            }
        });

        final EditText ip = (EditText) findViewById(R.id.et_ip);
        final EditText port = (EditText) findViewById(R.id.et_port);

        SrvInfo srvInfo = DBProcMgr.getInstance().querySrvInfo();
        if (srvInfo != null) {
            ip.setText(srvInfo.srvIP);
            port.setText(srvInfo.srvPort);

            ip.setFocusable(true);
            ip.setSelection(ip.length());
        }

        btnOK = (Button) findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (ip.getText().toString().length() == 0) {

                    Toast.makeText(getApplicationContext(),
                            "提示：IP地址不能为空！",
                            Toast.LENGTH_SHORT).show();

                    return;
                }

                if (port.getText().toString().length() == 0) {

                    Toast.makeText(getApplicationContext(),
                            "提示：端口不能为空！",
                            Toast.LENGTH_SHORT).show();

                    return;

                }

                if (!ip.getText().toString().matches(regexIpv4)) {
                    Toast.makeText(getApplicationContext(),
                            "提示：请输入合法的IP地址！",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.valueOf(port.getText().toString()) > 65535) {
                    Toast.makeText(getApplicationContext(),
                            "提示：请输入合法的端口！",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 存储新值
                SrvInfo srvInfo = new SrvInfo();
                srvInfo.srvIP = ip.getText().toString();
                srvInfo.srvPort = port.getText().toString();
                DBProcMgr.getInstance().saveSrvInfo(srvInfo);

                // 内存更新
                APPDataInfo.BASE_URL = "http://" + srvInfo.srvIP + ":" + srvInfo.srvPort + "/";

                Toast.makeText(getApplicationContext(),
                        "保存成功",
                        Toast.LENGTH_SHORT).show();

                SettingPopupWindow.this.finish();
            }
        });
    }

    //实现onTouchEvent触屏函数但点击屏幕时销毁本Activity
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }
}
