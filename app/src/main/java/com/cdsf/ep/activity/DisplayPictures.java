package com.cdsf.ep.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.MatterReplyInfo;
import com.squareup.picasso.Picasso;
import com.tool.unit.OnDoubleClickListenerImpl;

public class DisplayPictures extends Activity implements OnDoubleClickListenerImpl.OnDoubleClickListener {

    private MatterReplyInfo matterReplyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_pictures);

        matterReplyInfo = (MatterReplyInfo) getIntent().getSerializableExtra("matterReplyInfo");

        final ImageView pic = (ImageView) findViewById(R.id.imv_pic);
        Picasso.with(this).load(matterReplyInfo.replyContent)
                .placeholder(R.drawable.pic_loading)
                .error(R.drawable.pic_load_error)
                .into(pic);


        OnDoubleClickListenerImpl.registerDoubleClickListener(pic, this);
    }

    @Override
    public void OnSingleClick(View v) {
    }

    @Override
    public void OnDoubleClick(View v) {
        DisplayPictures.this.finish();
    }
}
