package com.cdsf.ep.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cdsf.ep.R;
import com.cdsf.ep.bean.ModInfo;
import com.cdsf.ep.bean.ModInfoResult;

import java.util.Calendar;


public class ModInfoActivity extends Activity implements View.OnClickListener {

    // 返回按钮
    private Button btnBack;
    // 保存按钮
    private Button btnSave;
    // 标题
    private TextView txTitle;
    // 修改信息
    private ModInfo modInfo;

    // 日期选择器
    private DatePicker datePicker;
    // 时间选择器
    private TimePicker timePicker;
    // 文本内容
    private EditText txContent;

    // 修改内容
    private ModInfoResult modInfoResult;

    // 日期修改内容
    private String dateContent;
    // 时间修改内容
    private String timeContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info);

        modInfo = (ModInfo)getIntent().getSerializableExtra("data");
        modInfoResult = new ModInfoResult();
        modInfoResult.result = modInfo.content;

        initView();
    }

    private void initView() {

        // 标题
        txTitle = (TextView)findViewById(R.id.tx_title);
        txTitle.setText(modInfo.title);

        // 完成按钮
        btnSave = (Button)findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        // 返回
        btnBack = (Button)findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        datePicker = (DatePicker)findViewById(R.id.date_picker);
        timePicker = (TimePicker)findViewById(R.id.time_picker);
        txContent = (EditText)findViewById(R.id.tx_content);

        int mYear = Calendar.getInstance().get(Calendar.YEAR);
        int mMonth = Calendar.getInstance().get(Calendar.MONTH);
        int mDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int mHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int mMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int mSecond = Calendar.getInstance().get(Calendar.SECOND);

        dateContent = ""+mYear+"-"+mMonth+"-"+mDay;
        timeContent = ""+mHour+":"+mMinute+":"+mSecond;
        datePicker.init(mYear, mMonth, mDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateContent = "" + year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
            }
        });

        timePicker.setIs24HourView(false);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeContent = "" + hourOfDay + ":" + minute + ":00";
            }
        });

        switch (modInfo.type) {
            case ModInfo.TY_DATE: {

                dateContent = modInfo.content;
                timePicker.setVisibility(View.GONE);
                txContent.setVisibility(View.GONE);

                break;
            }
            case ModInfo.TY_TIME: {

                timeContent = modInfo.content;
                datePicker.setVisibility(View.GONE);
                txContent.setVisibility(View.GONE);

                break;
            }
            case ModInfo.TY_DATEANDTIME: {

                if (modInfo.content != null && !modInfo.content.equals("")) {
                    String[] s = modInfo.content.split(" ");
                    if (s.length != 2) {
                        dateContent = "";
                        timeContent = "";
                    }
                    else {
                        dateContent = s[0];
                        timeContent = s[1];
                    }
                }
                txContent.setVisibility(View.GONE);

                break;
            }
            case ModInfo.TY_TEXT: {

                txContent.setText(modInfo.content);
                txContent.setSelection(modInfo.content.length());
                datePicker.setVisibility(View.GONE);
                timePicker.setVisibility(View.GONE);

                break;
            }
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_back: {

                Intent intent = new Intent();
                intent.putExtra("result", modInfoResult);
                setResult(1, intent);

                ModInfoActivity.this.finish();

                break;
            }
            case R.id.btn_save: {

                Intent intent = new Intent();

                switch (modInfo.type) {
                    case ModInfo.TY_DATE: {
                        modInfoResult.result = dateContent;
                        break;
                    }
                    case ModInfo.TY_TIME: {
                        modInfoResult.result = timeContent;
                        break;
                    }
                    case ModInfo.TY_DATEANDTIME: {
                        modInfoResult.result = dateContent + " " + timeContent;
                        break;
                    }
                    case ModInfo.TY_TEXT: {
                        modInfoResult.result = txContent.getText().toString();
                        break;
                    }
                }

                intent.putExtra("result", modInfoResult);
                setResult(0, intent);

                ModInfoActivity.this.finish();

                break;
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            Intent intent = new Intent();
            intent.putExtra("result", modInfoResult);
            setResult(1, intent);

            ModInfoActivity.this.finish();

            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
