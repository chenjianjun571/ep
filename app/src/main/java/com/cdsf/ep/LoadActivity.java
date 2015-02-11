package com.cdsf.ep;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.cdsf.ep.activity.LoginActivity;
import com.cdsf.ep.common.SignalEventDef;
import com.tool.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;
//import com.cdsf.ep.tool.Utils;

public class LoadActivity extends Activity implements Animation.AnimationListener {

    private static final String TAG = "LoadActivity";
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        ImageView imageView = (ImageView) findViewById(R.id.loading_view);
        context = this;

        // 加载动画效果
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1.0f);
        // 设置展示时间为3秒
        alphaAnimation.setDuration(3000);
        alphaAnimation.setAnimationListener(this);

        imageView.startAnimation(alphaAnimation);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // 跳转到首页界面
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

}
