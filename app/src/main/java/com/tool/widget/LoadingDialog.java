package com.tool.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.cdsf.ep.R;


public class LoadingDialog extends Dialog {
	private ImageView iv_progress;
	private TextView tv_message;
	private AnimationDrawable anim;

	private String msg = null;
	private int msgId = -1;

	public LoadingDialog(Context context) {
		super(context, R.style.LoadingDialog);
		setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_dialog);

		this.iv_progress = (ImageView) findViewById(R.id.iv_progress);
		this.tv_message = (TextView) findViewById(R.id.tv_message);

		this.iv_progress.setBackgroundResource(R.anim.loading_anim);
		this.anim = (AnimationDrawable) iv_progress.getBackground();
	}

	public void setMessage(String msg) {
		if (msg == null) { throw new NullPointerException("Message is null"); }
		if (tv_message == null) {
			this.msg = msg;
		}
		else {
			tv_message.setText(msg);
		}
	}

	public void setMessage(int msgId) {
		if (tv_message == null) {
			this.msgId = msgId;
		}
		else {
			tv_message.setText(msgId);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		this.anim.start();
		if (msg != null) {
			tv_message.setText(msg);
			msg = null;
		}
		else if (msgId != -1) {
			tv_message.setText(msgId);
			msgId = -1;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		this.anim.stop();
	}
}
