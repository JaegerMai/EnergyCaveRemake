package com.powerrun.akenergycaveremake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.powerrun.akenergycaveremake.common.BaseActivity;

public class ConsoleActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "ConsoleActivity";
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        mContext = this;
        initUI();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        ImageButton imgBtnTimeAdd = findViewById(R.id.image_button_time_add);
        ImageButton imgBtnTimeDec = findViewById(R.id.image_button_time_dec);
        ImageButton imgBtnTempAdd0 = findViewById(R.id.image_button_temp_add_0);
        ImageButton imgBtnTempDec0 = findViewById(R.id.image_button_temp_dec_0);
        ImageButton imgBtnTempAdd1 = findViewById(R.id.image_button_temp_add_1);
        ImageButton imgBtnTempDec1 = findViewById(R.id.image_button_temp_dec_1);
        ImageButton imgBtnSwitch   = findViewById(R.id.image_button_switch);
        ImageButton imgBtnExit     = findViewById(R.id.image_button_exit);
        ImageButton imgBtnMusic    = findViewById(R.id.image_button_music);
        imgBtnTimeAdd.setOnClickListener(this);
        imgBtnTimeDec.setOnClickListener(this);
        imgBtnTempAdd0.setOnClickListener(this);
        imgBtnTempDec0.setOnClickListener(this);
        imgBtnTempAdd1.setOnClickListener(this);
        imgBtnTempDec1.setOnClickListener(this);
        imgBtnSwitch.setOnClickListener(this);
        imgBtnExit.setOnClickListener(this);
        imgBtnMusic.setOnClickListener(this);
        imgBtnTimeAdd.setOnLongClickListener(this);
        imgBtnTimeDec.setOnLongClickListener(this);
        imgBtnTempAdd0.setOnLongClickListener(this);
        imgBtnTempDec0.setOnLongClickListener(this);
        imgBtnTempAdd1.setOnLongClickListener(this);
        imgBtnTempDec1.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(getApplicationContext(), "点击了按钮"+view.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(View view) {
        // TODO: 实现长按逻辑
        Toast.makeText(getApplicationContext(), "长按了"+view.getId(), Toast.LENGTH_SHORT).show();
        return false;
    }
}