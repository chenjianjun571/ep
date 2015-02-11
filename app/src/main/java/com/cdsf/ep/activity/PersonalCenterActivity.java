package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushManager;
import com.cdsf.ep.R;
import com.cdsf.ep.bean.AccountInfo;
import com.cdsf.ep.bean.ModInfo;
import com.cdsf.ep.bean.ModInfoResult;
import com.cdsf.ep.common.CommonDef;
import com.cdsf.ep.db.DBProcMgr;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.PersonalCenterProcess;
import com.squareup.picasso.Picasso;
import com.tool.PreferenceUtils;
import com.tool.widget.LoadingDialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalCenterActivity extends Activity implements View.OnClickListener,
        PersonalCenterProcess.OnResultListener {

    // 保存按钮
    private Button btnSave;
    // 上传头像
    private Button btnUploadPic;
    // 注销按钮
    private Button btnLogout;
    // 头像按钮
    private CircleImageView circleImageView;
    // 用户名 电话号码 个人签名
    private TextView txUserName, txPthone, txSign;
    private Context context;
    // 头像UID
    private String strPicID = "";

    // 网络处理
    private PersonalCenterProcess personalCenterProcess;
    // 数据加载对话框
    LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_center);

        context = this;
        personalCenterProcess = new PersonalCenterProcess();
        personalCenterProcess.setResultListener(this);
        mLoadingDialog = new LoadingDialog(context);

        initView();
    }

    private void initView() {

        // 保存按钮
        btnSave = (Button)findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        // 注销按钮
        btnLogout = (Button)findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(this);

        // 上传头像
        btnUploadPic = (Button)findViewById(R.id.btn_upload_pic);
        btnUploadPic.setOnClickListener(this);

        // 头像按钮
        circleImageView = (CircleImageView)findViewById(R.id.ic_project_pic);
        circleImageView.setOnClickListener(this);

        txUserName = (TextView)findViewById(R.id.tx_user_name);
        txPthone = (TextView)findViewById(R.id.tx_user_phone);
        txPthone.setOnClickListener(this);
        txSign = (TextView)findViewById(R.id.tx_user_sign);
        txSign.setOnClickListener(this);

        if (APPDataInfo.getInstance().accountInfo != null) {
            txUserName.setText(APPDataInfo.getInstance().accountInfo.userName);
            txPthone.setText(APPDataInfo.getInstance().accountInfo.userPthone);
            txSign.setText(APPDataInfo.getInstance().accountInfo.userSign);
            Picasso.with(this).load(APPDataInfo.getInstance().accountInfo.userPic)
                    .placeholder(R.drawable.def_pic)
                    .error(R.drawable.def_pic)
                    .into(circleImageView);

            // 个人ep值img_ep_value
            final ImageView epValue = (ImageView)findViewById(R.id.img_ep_value);
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
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_logout: {

                new AlertDialog.Builder(context)
                        .setTitle("提示").setMessage("你确定要退出并清除登陆信息？")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!mLoadingDialog.isShowing()) {
                                    mLoadingDialog.setMessage("注销中，请稍后....");
                                    mLoadingDialog.show();
                                }

                                // 上传头像
                                personalCenterProcess.logout();
                            }
                        })
                        .setNegativeButton("取消", null).show();

                break;
            }
            case R.id.ic_project_pic: {
                showDialog();
                break;
            }
            case R.id.tx_user_phone: {
                // 电话号码
                ModInfo modInfo = new ModInfo();

                modInfo.type = ModInfo.TY_TEXT;
                modInfo.title = "电话号码";
                modInfo.content = txPthone.getText().toString();
                // 项目名称
                Intent intent = new Intent(PersonalCenterActivity.this, ModInfoActivity.class);
                intent.putExtra("data", modInfo);

                // 1是标识谁调用，在回调回来得时候根据这个标签来区别
                startActivityForResult(intent, PTHONE_REQUEST_CODE);
                break;
            }
            case R.id.tx_user_sign: {
                // 个人签名
                ModInfo modInfo = new ModInfo();

                modInfo.type = ModInfo.TY_TEXT;
                modInfo.title = "个人签名";
                modInfo.content = txSign.getText().toString();
                // 项目名称
                Intent intent = new Intent(PersonalCenterActivity.this, ModInfoActivity.class);
                intent.putExtra("data", modInfo);

                // 1是标识谁调用，在回调回来得时候根据这个标签来区别
                startActivityForResult(intent, SIGN_REQUEST_CODE);
                break;
            }
            case R.id.btn_upload_pic: {

                File file = new File(IMAGE_UP_FILE_NAME);
                if (!file.exists()) {
                    Toast.makeText(getApplicationContext(),
                            "亲!请选择头像以后再点击我.",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.setMessage("上传中，请稍后....");
                    mLoadingDialog.show();
                }

                // 上传头像
                personalCenterProcess.userPicUploadReq(IMAGE_UP_FILE_NAME);

                // 上传头像
                break;
            }
            case R.id.btn_save: {

                if (txPthone.getText().toString().length() < 1 || txPthone.getText().toString().length() > 30) {
                    Toast.makeText(getApplicationContext(),
                            "电话号码不能为空并且不能超过30个字符",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                if (txSign.getText().toString().length() < 1 || txSign.getText().toString().length() > 1024) {
                    Toast.makeText(getApplicationContext(),
                            "签名不能为空并且不能超过1024个字符",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                if (strPicID.equals("")) {

                    new AlertDialog.Builder(context)
                            .setTitle("提示").setMessage("没有修改头像，是否继续保存？")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // 事项删除
                                    if (!mLoadingDialog.isShowing()) {
                                        mLoadingDialog.setMessage("数据请求中，请稍后....");
                                        mLoadingDialog.show();
                                    }

                                    // 组装项目数据
                                    AccountInfo accountInfo = new AccountInfo();

                                    accountInfo.userName = APPDataInfo.getInstance().accountInfo.userName;
                                    accountInfo.passWord = APPDataInfo.getInstance().accountInfo.passWord;
                                    accountInfo.userPic = strPicID;
                                    accountInfo.userID = APPDataInfo.getInstance().accountInfo.userID;
                                    accountInfo.userPthone = txPthone.getText().toString();
                                    accountInfo.userSign = txSign.getText().toString();

                                    personalCenterProcess.userinfoModReq(accountInfo);

                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("取消", null).show();
                } else {
                    // 提交修改
                    if (!mLoadingDialog.isShowing()) {
                        mLoadingDialog.setMessage("数据请求中，请稍后....");
                        mLoadingDialog.show();
                    }

                    // 组装项目数据
                    AccountInfo accountInfo = new AccountInfo();

                    accountInfo.userName = APPDataInfo.getInstance().accountInfo.userName;
                    accountInfo.passWord = APPDataInfo.getInstance().accountInfo.passWord;
                    accountInfo.userPic = strPicID;
                    accountInfo.userID = APPDataInfo.getInstance().accountInfo.userID;
                    accountInfo.userPthone = txPthone.getText().toString();
                    accountInfo.userSign = txSign.getText().toString();

                    personalCenterProcess.userinfoModReq(accountInfo);
                }

                // 保存
                break;
            }
        }

    }

    @Override
    public void onModResult(int result, AccountInfo accountInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                Toast.makeText(getApplicationContext(),
                        "修改成功",
                        Toast.LENGTH_SHORT).show();

                break;
            }
            case FAILURE: {

                Toast.makeText(getApplicationContext(),
                        "修改失败",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }

    }

    @Override
    public void onPicUploadResult(int result, String picID) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                Toast.makeText(getApplicationContext(),
                        "上传成功，可以保存修改信息了",
                        Toast.LENGTH_SHORT).show();

                strPicID = picID;

                break;
            }
            case FAILURE: {

                Toast.makeText(getApplicationContext(),
                        "上传失败",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }

    }

    @Override
    public void onLogoutResult(int result, String msg) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                PushManager.stopWork(getApplicationContext());

                // 注销
                APPDataInfo.getInstance().accountInfo = null;
                DBProcMgr.getInstance().dropDB();
                PreferenceUtils.delDirsExists(APPDataInfo.PIC_LOCAL_DIR);
                PreferenceUtils.delDirsExists(APPDataInfo.SOUND_LOCAL_DIR);
                PreferenceUtils.delDirsExists(APPDataInfo.DOWN_SOUND_LOCAL_DIR);

                moveTaskToBack(true);
                PersonalCenterActivity.this.finish();

                Intent intent = new Intent();
                intent.setClass(context.getApplicationContext(),LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);

                break;
            }
            case FAILURE: {

                Toast.makeText(getApplicationContext(),
                        "注销失败，请检查网络环境.....",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }

    }

    /* 设置请求码*/
    private static final int PTHONE_REQUEST_CODE = 0;
    private static final int SIGN_REQUEST_CODE = 1;
    private static final int IMAGE_REQUEST_CODE = 10;
    private static final int CAMERA_REQUEST_CODE = 11;
    private static final int RESULT_REQUEST_CODE = 12;
    private String[] items = new String[] { "选择本地图片","拍照" };
    /*头像名称*/
    private static final String IMAGE_FILE_NAME = APPDataInfo.PIC_LOCAL_DIR + "userImage.jpg";
    private static final String IMAGE_UP_FILE_NAME = APPDataInfo.PIC_LOCAL_DIR + "userImageUp.jpg";

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

            case PTHONE_REQUEST_CODE: { // 电话号码回来得

                ModInfoResult result_value = (ModInfoResult)data.getSerializableExtra("result");
                if (resultCode == 0) {
                    txPthone.setText(result_value.result);
                }

                break;
            }
            case SIGN_REQUEST_CODE: { // 个人签名回来的

                ModInfoResult result_value = (ModInfoResult)data.getSerializableExtra("result");
                if (resultCode == 0) {
                    txSign.setText(result_value.result);
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
                    Toast.makeText(context, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
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
                .setTitle("设置头像")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                Intent intentFromGallery = new Intent(Intent.ACTION_GET_CONTENT);
                                intentFromGallery.setType("image/*");
                                PersonalCenterActivity.this.startActivityForResult(intentFromGallery,IMAGE_REQUEST_CODE);
                                break;
                            case 1:
                                Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                // 判断存储卡是否可以用，可用进行存储
                                if (PreferenceUtils.hasSdcard()) {
                                    intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(new File(IMAGE_FILE_NAME)));
                                }
                                PersonalCenterActivity.this.startActivityForResult(intentFromCapture,CAMERA_REQUEST_CODE);
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
        PersonalCenterActivity.this.startActivityForResult(intent, RESULT_REQUEST_CODE);
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
                circleImageView.setImageResource(R.drawable.def_pic);
                circleImageView.setImageURI(Uri.fromFile(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
