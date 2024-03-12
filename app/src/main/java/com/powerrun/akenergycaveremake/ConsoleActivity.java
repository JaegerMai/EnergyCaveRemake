package com.powerrun.akenergycaveremake;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.powerrun.akenergycaveremake.common.BaseActivity;

public class ConsoleActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "ConsoleActivity";
    private Context mContext;
    private TextView tvTime;
    private TextView tvTemp0;
    private TextView tvTemp1;
    private ImageView ivCurrentTemp0;
    private ImageView ivCurrentTemp1;
    private MusicHelper musicHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        mContext = this;
        musicHelper = new MusicHelper();
        musicHelper.create(mContext);
        initUI();
    }

    /**
     * 打开音乐选择对话框
     */
    private void openMusicDialog() {
        String[] musicList = getResources().getStringArray(R.array.bg_music_list);
        final int position = musicHelper.getPosition();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("选择背景音乐")
                .setSingleChoiceItems(musicList, position, (dialog, which) -> {
                    musicHelper.playSong(which);
                    dialog.dismiss();
                })
                .setPositiveButton("停止音乐", (dialog, which) -> {
                    musicHelper.pause();
                    dialog.dismiss();
                });
        builder.create().show();
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.image_button_music) {
            openMusicDialog();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        // TODO: 实现长按逻辑
        Toast.makeText(getApplicationContext(), "长按了"+view.getId(), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicHelper.destroy();
        findViewById(R.id.image_button_music).clearAnimation();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        // 获取控件
        ImageButton imgBtnTimeAdd = findViewById(R.id.image_button_time_add);
        ImageButton imgBtnTimeDec = findViewById(R.id.image_button_time_dec);
        ImageButton imgBtnTempAdd0 = findViewById(R.id.image_button_temp_add_0);
        ImageButton imgBtnTempDec0 = findViewById(R.id.image_button_temp_dec_0);
        ImageButton imgBtnTempAdd1 = findViewById(R.id.image_button_temp_add_1);
        ImageButton imgBtnTempDec1 = findViewById(R.id.image_button_temp_dec_1);
        ImageButton imgBtnSwitch   = findViewById(R.id.image_button_switch);
        ImageButton imgBtnExit     = findViewById(R.id.image_button_exit);
        ImageButton imgBtnMusic    = findViewById(R.id.image_button_music);
        tvTime = findViewById(R.id.text_view_time);
        tvTemp0 = findViewById(R.id.text_view_temp_0);
        tvTemp1 = findViewById(R.id.text_view_temp_1);
        ivCurrentTemp0 = findViewById(R.id.image_view_temp_0);
        ivCurrentTemp1 = findViewById(R.id.image_view_temp_1);
        // 设置旋转动画
        Animation rotateAnimation  = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(3000);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        imgBtnMusic.startAnimation(rotateAnimation);
        // 设置点击事件
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
}