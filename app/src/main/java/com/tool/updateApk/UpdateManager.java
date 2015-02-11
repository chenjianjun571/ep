package com.tool.updateApk;

/**
* Created by chenjianjun on 14-10-15.
* <p/>
* Beyond their own, let the future
*/
import java.io.File;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cdsf.ep.R;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.tool.PreferenceUtils;

public class UpdateManager {

    private Context mContext;

    //提示语
    private String updateMsg = "亲，EP又有新版本了，赶快更新吧！";

    private Dialog noticeDialog;

    private Dialog downloadDialog;
    /* 下载包安装路径 */
    private static final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/update/";

    /* 进度条 */
    private ProgressBar mProgress;
    /* 状态显示 */
    private TextView mText;

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    //外部接口让主Activity调用
    public boolean checkUpdateInfo(){
        return showNoticeDialog();
    }

    private boolean showNoticeDialog() {

        if (APPDataInfo.getInstance().accountInfo == null) {
            return false;
        }

        String new_version = APPDataInfo.getInstance().accountInfo.version;
        final String apk_url = APPDataInfo.getInstance().accountInfo.apkURL;

        if (new_version == null || apk_url == null || new_version.equals("") || apk_url.equals("")) {
            return false;
        }

        String old_version;
        try
        {
            old_version = this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionName;
            if (old_version == null || old_version.equals("")) {
                return false;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }

        if (old_version.equals(new_version))
        {
            return false;
        }

        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog(apk_url);
            }
        });

        noticeDialog = builder.create();
        noticeDialog.show();

        return true;
    }

    private void showDownloadDialog(String url){

        AlertDialog.Builder builder = new Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.update_apk_progress, null);
        mProgress = (ProgressBar)v.findViewById(R.id.update_apk_loading_progress);

        mText = (TextView)v.findViewById(R.id.update_apk_loading_txt);
        mText.setText("准备下载");

        builder.setView(v);

        downloadDialog = builder.create();
        downloadDialog.show();

        if (!TextUtils.isEmpty(APPDataInfo.LOCAL_UPDATE_APK_DIR)) {
            File file = new File(APPDataInfo.LOCAL_UPDATE_APK_DIR);
            if (file.exists()) {
                file.delete();
            }
        }

        // 开始从服务器请求数据
        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(3);
        http.configTimeout(5000);
        http.download(url, APPDataInfo.LOCAL_UPDATE_APK_DIR, true, false, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> fileResponseInfo) {

                mText.setText("下载成功");
                File apkfile = new File(fileResponseInfo.result.getPath());
                if (!apkfile.exists()) {
                    return;
                }

                downloadDialog.dismiss();

                DBProcMgr.getInstance().dropDB();
                PreferenceUtils.delDirsExists(APPDataInfo.PIC_LOCAL_DIR);
                PreferenceUtils.delDirsExists(APPDataInfo.SOUND_LOCAL_DIR);
                PreferenceUtils.delDirsExists(APPDataInfo.DOWN_SOUND_LOCAL_DIR);

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
                mContext.startActivity(i);

            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                mText.setText("下载中"+current/1024+"Kbs");
                mProgress.setProgress((int)((current*100)/total));
            }

            @Override
            public void onFailure(HttpException e, String s) {
                mText.setText("下载失败");
            }
        });
    }
}

