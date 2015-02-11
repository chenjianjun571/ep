package com.cdsf.ep.application;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.frontia.FrontiaApplication;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.tool.PreferenceUtils;
import com.tool.crash.CrashHandler;

/**
 * Created by chenjianjun on 14-11-17.
 * <p/>
 * Beyond their own, let the future
 */
public class MyApplication extends FrontiaApplication {

    private static MyApplication ourInstance = new MyApplication();
    /**
     * 返回单例
     *
     * @return 返回单例
     */
    public static MyApplication getInstance() {
        return ourInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        // 数据库初期化
        if (!DBProcMgr.initEnv(getApplicationContext())) {
            exit();
            return;
        }

        // 内存初期化
        APPDataInfo.getInstance().init();
    }

    public void exit() {

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("亲!应用初期化失败了,你可能需要重新安装应用，如果重新安装还是不能解决问题,请上官网向我们反映问题.")
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        //退出程序
                        MyApplication.this.exitAPP();

                    }
                })
                .setCancelable(false)
                .create()
                .show();

    }

    public void exitAPP() {

        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);

    }

    // 获取ApiKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        return apiKey;
    }
}
