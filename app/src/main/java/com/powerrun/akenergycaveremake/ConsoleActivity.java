package com.powerrun.akenergycaveremake;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.powerrun.akenergycaveremake.common.BaseActivity;

import java.io.File;
import java.util.HashMap;

public class ConsoleActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "ConsoleActivity";
    private Context mContext;
    private MusicHelper musicHelper;
    private Handler exitLongPressHandler = new Handler();
    private static final String FONT_DIGITAL_7 = "raw" + File.separator
            + "digital-7.ttf";
    //按钮点击和长按事件,使用HashMap存储
    private HashMap<Integer,Runnable> clickActions = new HashMap<>();
    private HashMap<Integer,Runnable> longPressActions = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        mContext = this;
        //初始化音乐播放器
        musicHelper = new MusicHelper();
        musicHelper.create(mContext);
        //初始化UI
        initUI();
        //TODO: 初始化蓝牙
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicHelper.destroy();
        findViewById(R.id.image_button_music).clearAnimation();
        //TODO: 关闭蓝牙
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        //从HashMap获取并执行对应的操作
        Runnable action = clickActions.get(id);
        if(action != null) {
            action.run();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        // TODO: 实现长按逻辑
        Toast.makeText(getApplicationContext(), "长按了"+view.getId(), Toast.LENGTH_SHORT).show();
        return true;
    }
    /**
     * 重写返回键，退出时弹出提示框
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showExitDialog();
    }
    /**
     * 根据传感器返回的数据修改温度显示
     * 分别在30,35,40,45度时显示不同的图标
     */
    void updateTempDisplay(ImageView iv, int temp) {
        int[] tempIcons = {
                R.drawable.icon_console_weatherglass_1_39_104,
                R.drawable.icon_console_weatherglass_2_39_104,
                R.drawable.icon_console_weatherglass_3_39_104,
                R.drawable.icon_console_weatherglass_4_39_104,
                R.drawable.icon_console_weatherglass_5_39_104
        };
        // 30-45度之间显示不同的图标
        int index = temp / 5;
        if (index < 0) {
            index = 0;
        } else if (index > 4) {
            index = 4;
        }
        iv.setImageResource(tempIcons[index]);
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
    //退出按钮长按标志
    private boolean isExitLongPress = false;
    /**
     * 退出按钮长按监听
     * 长按6秒会清楚设备连接并退出程序
     * 短按会弹出退出提示框
     */
    private View.OnTouchListener exitLongPressListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isExitLongPress = false;
                    exitLongPressHandler.postDelayed(exitLongPressRunnable, 6000);//长按六秒后执行
                    break;
                case MotionEvent.ACTION_UP:
                    if(!isExitLongPress) {
                        exitLongPressHandler.removeCallbacks(exitLongPressRunnable);
                        showExitDialog();
                    }
                    break;
            }
            return true;
        }
    };
    //退出按钮长按事件
    private Runnable exitLongPressRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "长按退出程序");
            isExitLongPress = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                    .setTitle("警告")
                    .setMessage("是否要清除与该设备的链接并退出？")
                    .setNegativeButton("取消", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton("确定", (dialog, which) -> {
                        //TODO: 清除蓝牙设备连接，并关机
                        dialog.dismiss();
                        finish();
                    });
            builder.create().show();
        }
    };

    /**
     * 初始化UI
     */
    private void initUI() {
        // 获取控件
        ImageButton[] buttons = new ImageButton[]{
                findViewById(R.id.image_button_time_add),
                findViewById(R.id.image_button_time_dec),
                findViewById(R.id.image_button_temp_add_0),
                findViewById(R.id.image_button_temp_dec_0),
                findViewById(R.id.image_button_temp_add_1),
                findViewById(R.id.image_button_temp_dec_1),
                findViewById(R.id.image_button_power),
                findViewById(R.id.image_button_exit),
                findViewById(R.id.image_button_music)
        };
        TextView[] textViews = new TextView[]{
                findViewById(R.id.text_view_time),
                findViewById(R.id.text_view_temp_0),
                findViewById(R.id.text_view_temp_1)
        };

        // 设置点击事件
        for (ImageButton button : buttons) {
            button.setOnClickListener(this);
            button.setOnLongClickListener(this);
        }
        findViewById(R.id.image_button_exit).setOnTouchListener(exitLongPressListener);

        // 为每个按钮ID添加对应的操作
        clickActions.put(R.id.image_button_music, this::openMusicDialog);
        // TODO: 添加其他按钮的点击事件

        // 设置字体样式
        AssetManager assets = getApplication().getAssets();
        final Typeface font = Typeface.createFromAsset(assets, FONT_DIGITAL_7);
        for (TextView textView : textViews) {
            textView.setTypeface(font);
        }

        // 设置音乐按钮旋转动画
        Animation rotateAnimation  = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(3000);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        findViewById(R.id.image_button_music).startAnimation(rotateAnimation);
        //TODO: 蓝牙图标，显示连接状态
    }
    /**
     * 退出提示框
     */
    private void showExitDialog() {
        Log.i(TAG, "短按退出程序");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("警告")
                .setMessage("确定要退出吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    //TODO: 蓝牙发送推出指令
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }
}