package com.cdsf.ep.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.ModInfo;
import com.cdsf.ep.bean.ModInfoResult;
import com.cdsf.ep.bean.PersonnelInfo;
import com.cdsf.ep.bean.ProjectInfo;
import com.cdsf.ep.master.APPDataInfo;
import com.cdsf.ep.process.ProgectProcess;
import com.tool.PreferenceUtils;
import com.tool.unit.BaseAdapterHelper;
import com.tool.unit.QuickAdapter;
import com.tool.widget.LoadingDialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProgectAddActivity extends Activity implements View.OnClickListener,
        ProgectProcess.OnResultListener {

    private Context context;

    // 返回按钮
    private Button btnBack;
    // 保存按钮
    private Button btnSave;
    // 上传头像
    private Button btnUploadPic;
    // 项目图标
    private CircleImageView ciProjectPic;
    // 项目名称
    private TextView txProjectNmae;
    // 项目简介
    private TextView txProjectContent;
    // 头像UID
    private String strPicID = "";

    // 人员列表
    private GridView gvPersonals;
    // 视频网格列表数据源
    protected QuickAdapter<PersonnelInfo> mProjectUserInfoAdapter;
    // 用户人员剔重
    private HashMap<String, PersonnelInfo> mProjectUserInfoHashMap;

    // 逻辑处理过程
    private ProgectProcess progectInfoProcess;
    // 数据加载对话框
    LoadingDialog mLoadingDialog;

    /* 头像设置请求码*/
    private static final int IMAGE_REQUEST_CODE = 10;
    private static final int CAMERA_REQUEST_CODE = 11;
    private static final int RESULT_REQUEST_CODE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progect_add);

        context = this;

        progectInfoProcess = new ProgectProcess();
        progectInfoProcess.setResultListener(this);

        mLoadingDialog = new LoadingDialog(context);

        initView();
        initData();
    }

    private void initView() {

        // 返回按钮
        btnBack = (Button)findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        // 保存按钮
        btnSave = (Button)findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        // 项目头像
        ciProjectPic = (CircleImageView)findViewById(R.id.ic_project_pic);
        ciProjectPic.setOnClickListener(this);

        // 上传头像
        btnUploadPic = (Button)findViewById(R.id.btn_upload_pic);
        btnUploadPic.setOnClickListener(this);

        // 项目名称
        txProjectNmae = (TextView)findViewById(R.id.tx_project_name);
        txProjectNmae.setOnClickListener(this);

        // 项目简介
        txProjectContent = (TextView)findViewById(R.id.tx_project_content);
        txProjectContent.setOnClickListener(this);

        gvPersonals = (GridView) findViewById(R.id.gv_project_add_personnel);

        mProjectUserInfoHashMap = new HashMap<String, PersonnelInfo>();
        mProjectUserInfoAdapter = new QuickAdapter<PersonnelInfo>(this, R.layout.personal_grid_item) {
            @Override
            protected void convert(BaseAdapterHelper helper, PersonnelInfo item) {

                final ImageView delView = helper.getView(R.id.img_item_del);
                delView.setTag(helper);
                delView.setTag(R.id.project_grid_view_item_user_info, item);

                delView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 删除
                        PersonnelInfo personnelInfo = (PersonnelInfo) v.getTag(R.id.project_grid_view_item_user_info);
                        remove(personnelInfo);
                        mProjectUserInfoHashMap.remove(personnelInfo.userID);
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

                PersonnelInfo personnelInfo = (PersonnelInfo) mProjectUserInfoAdapter.getItem(position);
                if (!personnelInfo.isEnd) {

                    // 如果之前是删除，用户点击其他区域取消删除状态
                    if (personnelInfo.isDel) {
                        personnelInfo.isDel = false;
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
                    progectInfoProcess.projectQueryPersonalsReq();
                }

            }
        });

        // 设置长按监听事件
        gvPersonals.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                PersonnelInfo personnelInfo = (PersonnelInfo) mProjectUserInfoAdapter.getItem(position);
                if (personnelInfo.isEnd) {
                    // 最后一个是功能按钮，不支持长按
                    return false;
                }

                personnelInfo.isDel = true;
                mProjectUserInfoAdapter.notifyDataSetChanged();

                return true;

            }
        });
    }

    private void initData() {

        // 一个加号的图标
        PersonnelInfo projectUserInfo = new PersonnelInfo();
        projectUserInfo.isEnd = true;
        mProjectUserInfoAdapter.addFront(projectUserInfo);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_back:
            {
                ProgectAddActivity.this.finish();
                break;
            }
            case R.id.btn_save:
            {
                if (txProjectNmae.getText().toString().length() < 1 || txProjectNmae.getText().toString().length() > 30) {
                    Toast.makeText(getApplicationContext(),
                            "项目名称不能为空并且不能超过30个字符",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                if (txProjectContent.getText().toString().length() > 1024) {
                    Toast.makeText(getApplicationContext(),
                            "项目简介不能超过1024个字符",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                if (strPicID.equals("")) {

                    new AlertDialog.Builder(context)
                            .setTitle("提示").setMessage("没有添加项目图标，是否继续保存？")
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
                                    ProjectInfo projectInfo = new ProjectInfo();
                                    ArrayList<PersonnelInfo> personnelInfos = new ArrayList<PersonnelInfo>();

                                    projectInfo.projectCreater = APPDataInfo.getInstance().accountInfo.userName;
                                    projectInfo.projectPic = strPicID;
                                    projectInfo.projectIntroduction = txProjectContent.getText().toString();
                                    projectInfo.projectName = txProjectNmae.getText().toString();
                                    for (int i = 0; i < mProjectUserInfoAdapter.getCount(); ++i) {
                                        if (mProjectUserInfoAdapter.getItem(i).isEnd) {
                                            continue;
                                        }
                                        personnelInfos.add(mProjectUserInfoAdapter.getItem(i));
                                    }

                                    progectInfoProcess.projectAddReq(projectInfo, personnelInfos);

                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("取消", null).show();
                } else {
                    // 项目添加
                    if (!mLoadingDialog.isShowing()) {
                        mLoadingDialog.setMessage("数据请求中，请稍后....");
                        mLoadingDialog.show();
                    }

                    // 组装项目数据
                    ProjectInfo projectInfo = new ProjectInfo();
                    ArrayList<PersonnelInfo> personnelInfos = new ArrayList<PersonnelInfo>();

                    projectInfo.projectCreater = APPDataInfo.getInstance().accountInfo.userName;
                    projectInfo.projectPic = strPicID;
                    projectInfo.projectIntroduction = txProjectContent.getText().toString();
                    projectInfo.projectName = txProjectNmae.getText().toString();
                    for (int i = 0; i < mProjectUserInfoAdapter.getCount(); ++i) {
                        if (mProjectUserInfoAdapter.getItem(i).isEnd) {
                            continue;
                        }
                        personnelInfos.add(mProjectUserInfoAdapter.getItem(i));
                    }

                    progectInfoProcess.projectAddReq(projectInfo, personnelInfos);
                }

                break;
            }
            case R.id.tx_project_name:
            {
                ModInfo modInfo = new ModInfo();

                modInfo.type = ModInfo.TY_TEXT;
                modInfo.title = "项目名称";
                modInfo.content = txProjectNmae.getText().toString();
                // 项目名称
                Intent intent = new Intent(ProgectAddActivity.this, ModInfoActivity.class);
                intent.putExtra("data", modInfo);

                // 1是标识谁调用，在回调回来得时候根据这个标签来区别
                startActivityForResult(intent, 1);

                break;
            }
            case R.id.tx_project_content:
            {
                ModInfo modInfo = new ModInfo();

                modInfo.type = ModInfo.TY_TEXT;
                modInfo.title = "项目简介";
                modInfo.content = txProjectContent.getText().toString();
                // 项目名称
                Intent intent = new Intent(ProgectAddActivity.this, ModInfoActivity.class);
                intent.putExtra("data", modInfo);

                // 1是标识谁调用，在回调回来得时候根据这个标签来区别
                startActivityForResult(intent, 2);

                // 项目简介
                break;
            }
            case R.id.ic_project_pic:
            {
                showDialog();
                // 项目头像
                break;
            }
            case R.id.btn_upload_pic:
            {
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
                progectInfoProcess.projectPicUploadReq(IMAGE_UP_FILE_NAME);

                break;
            }
        }
    }

    @Override
    public void onQueryResult(int result, ArrayList<ProjectInfo> projectInfoArrayList) {
    }

    @Override
    public void onDelResult(int result, ProjectInfo progectInfo) {
    }

    @Override
    public void onModResult(int result, ProjectInfo progectInfo) {
    }

    @Override
    public void onAddResult(int result, ProjectInfo progectInfo) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                Toast.makeText(getApplicationContext(),
                        "项目添加成功,请刷新列表...",
                        Toast.LENGTH_SHORT).show();

                ProgectAddActivity.this.finish();

                break;
            }
            case FAILURE: {

                Toast.makeText(getApplicationContext(),
                        "项目添加失败",
                        Toast.LENGTH_SHORT).show();

                break;
            }
        }
    }

    @Override
    public void onQueryPersonalsResult(int result, final ArrayList<PersonnelInfo> personnelInfos) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                final String[] personals = new String[personnelInfos.size()];
                final boolean[] personalsSelected = new boolean[personnelInfos.size()];

                for (int i = 0; i < personnelInfos.size(); ++i) {
                    personals[i] = personnelInfos.get(i).userName;
                    personalsSelected[i] = false;
                }

                Dialog alertDialog = new AlertDialog.Builder(context).
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

                                        if (mProjectUserInfoHashMap.get(personnelInfos.get(i).userID) != null) {
                                            continue;
                                        }

                                        mProjectUserInfoAdapter.addFront(personnelInfos.get(i));
                                        mProjectUserInfoHashMap.put(personnelInfos.get(i).userID,
                                                personnelInfos.get(i));
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
    public void onQueryPersonalsListResult(int result, ArrayList<PersonnelInfo> personalInfoArrayList) {
    }

    @Override
    public void onPicUploadResult(int result, String picID) {

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        switch (result) {

            case SUCCESS: {

                Toast.makeText(getApplicationContext(),
                        "头上传成功",
                        Toast.LENGTH_SHORT).show();

                strPicID = picID;

                break;
            }
            case FAILURE: {

                Toast.makeText(getApplicationContext(),
                        "头上传失败，服务器异常，请检查网络....",
                        Toast.LENGTH_SHORT).show();

                break;
            }
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

            case 1: { // 项目名称回来的

                ModInfoResult result_value = (ModInfoResult)data.getSerializableExtra("result");
                if (resultCode == 0) {
                    txProjectNmae.setText(result_value.result);
                }

                break;
            }
            case 2: { // 项目简介回来的

                ModInfoResult result_value = (ModInfoResult)data.getSerializableExtra("result");
                if (resultCode == 0) {
                    txProjectContent.setText(result_value.result);
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

    private String[] items = new String[] { "选择本地图片","拍照" };
    /*头像名称*/
    private static final String IMAGE_FILE_NAME = APPDataInfo.PIC_LOCAL_DIR + "progectImage.jpg";
    private static final String IMAGE_UP_FILE_NAME = APPDataInfo.PIC_LOCAL_DIR + "progectImageUp.jpg";

    private void showDialog() {
        new AlertDialog.Builder(context)
                .setTitle("设置头像")
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
                ciProjectPic.setImageResource(R.drawable.def_project_pic);
                ciProjectPic.setImageURI(Uri.fromFile(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
