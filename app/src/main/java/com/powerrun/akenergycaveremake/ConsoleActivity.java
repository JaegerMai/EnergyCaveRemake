package com.powerrun.akenergycaveremake;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.powerrun.akenergycaveremake.common.BaseActivity;
import com.powerrun.akenergycaveremake.common.SystemConfig;
import com.powerrun.akenergycaveremake.mvc.ConsoleController;
import com.powerrun.akenergycaveremake.mvc.ConsoleModel;
import com.powerrun.akenergycaveremake.mvc.ConsoleView;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public class ConsoleActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener, ConsoleView {
    private static final String TAG = "ConsoleActivity";
    private Context mContext;
    private MusicHelper musicHelper;
    private ProgressDialog progressDialog;
    private Handler exitLongPressHandler = new Handler();
    private ConsoleController controller;
    private ConsoleModel model;
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
        //初始化控制器
        model = new ConsoleModel();
        controller = new ConsoleController(model,this, mContext);

        //初始化音乐播放器
        musicHelper = new MusicHelper();
        musicHelper.create(mContext);
        //初始化UI
        initUI();
        //连接蓝牙
        connectBle();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicHelper.destroy();
        findViewById(R.id.image_button_music).clearAnimation();
        controller.handleExit();
        BleManager.getInstance().disconnectAllDevice();
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
     * 连接蓝牙
     */
    private void connectBle() {
        String deviceAddress = SystemConfig.mBLEAddress;
        if(deviceAddress.isEmpty()){
            Log.e(TAG, "connectBle: empty device address");
            Toast.makeText(getApplicationContext(), "connectBle: empty device address", Toast.LENGTH_SHORT).show();
            return;
        }
        BleManager.getInstance().connect(deviceAddress, bleGattCallback);
    }
    /**
     * 蓝牙连接回调
     */
    private Handler reconnectHandler = new Handler();//重连蓝牙
    private BleGattCallback bleGattCallback = new BleGattCallback() {
        @Override
        public void onStartConnect() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle(null);
            progressDialog.setMessage("正在连接蓝牙设备，请稍等...");
            progressDialog.show();
        }
        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            Log.e(TAG,"BleGattCallback: onConnectFail");
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"BleGattCallback: onConnectFail",Toast.LENGTH_SHORT).show();
            reconnectHandler.postDelayed(() -> connectBle(), 2000);
        }
        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            progressDialog.dismiss();
            model.setBleDevice(bleDevice);
            //开始交换数据
            BleManager.getInstance().notify(bleDevice, SystemConfig.UUID_SERVICE, SystemConfig.UUID_NOTIFY, bleNotifyCallback);
            // TODO: 开一个线程记录蓝牙数据1min一次
        }
        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            progressDialog.dismiss();
            //清空蓝牙数据队列
            controller.clearDataQueue();
            if (isActiveDisConnected) {
                Toast.makeText(getApplicationContext(), "连接中断", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "已断开连接", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private BleNotifyCallback bleNotifyCallback = new BleNotifyCallback() {
        @Override
        public void onNotifySuccess() {
            Log.i(TAG, "onNotifySuccess: ");
        }
        @Override
        public void onNotifyFailure(BleException exception) {
            Log.e(TAG, "onNotifyFailure: ");
        }
        @Override
        public void onCharacteristicChanged(byte[] data) {
            controller.addData(data);
        }
    };
    /**
     * 重写返回键，退出时弹出提示框
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showExitDialog();
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
                        //清除蓝牙设备连接，并关机
                        controller.clearDeviceInfo(mContext);
                        controller.handleExit();
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

        // 为每个按钮ID添加对应的点击事件
        clickActions.put(R.id.image_button_time_add, controller::handleTimeAdd);
        clickActions.put(R.id.image_button_time_dec, controller::handleTimeDec);
        clickActions.put(R.id.image_button_temp_add_0, controller::handleTempAdd0);
        clickActions.put(R.id.image_button_temp_dec_0, controller::handleTempDec0);
        clickActions.put(R.id.image_button_temp_add_1, controller::handleTempAdd1);
        clickActions.put(R.id.image_button_temp_dec_1, controller::handleTempDec1);
        clickActions.put(R.id.image_button_music, this::openMusicDialog);
        clickActions.put(R.id.image_button_power, controller::handlePowerButton);

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
                    //蓝牙发送退出指令
                    controller.handleExit();
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }

    /**
     * 温感温度变化回调
     * @param channel 通道
     * @param temp 温度
     */
    @Override
    public void onTempChange(ConsoleModel.Channel channel, int temp) {
        runOnUiThread(() -> {
            if (channel == ConsoleModel.Channel.CHANNEL_0) {
                updateTempDisplay(findViewById(R.id.image_view_temp_0), temp);
            } else if (channel == ConsoleModel.Channel.CHANNEL_1) {
                updateTempDisplay(findViewById(R.id.image_view_temp_1), temp);
            }
            if(temp >=127 || temp == 85){
                Toast.makeText(mContext, "通道" + channel + "温感为" + temp + "请检查线路",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 发送消息回调
     * @param msg 消息
     */
    @Override
    public void onMessage(String msg) {
        runOnUiThread(() -> Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show());
    }

    /**
     * 设置时间回调
     * @param value 时间
     */
    @Override
    public void onTimeSet(int value) {
        runOnUiThread(() -> {
            TextView tv = findViewById(R.id.text_view_time);
            tv.setText(String.format(Locale.CHINA, "%d分", value));
            //剩余时间不足1分钟时提示
            if(value == 1)
                Toast.makeText(mContext, "剩余时间不足1分钟", Toast.LENGTH_SHORT).show();
            if(value == 0)
                finish();
        });
    }

    /**
     * 设置温度回调
     * @param channel 通道
     * @param value 温度
     */
    @Override
    public void onTempSet(ConsoleModel.Channel channel, int value) {
        runOnUiThread(() -> {
            TextView tv = null;
            if (channel == ConsoleModel.Channel.CHANNEL_0) {
                tv = findViewById(R.id.text_view_temp_0);
            } else if (channel == ConsoleModel.Channel.CHANNEL_1) {
                tv = findViewById(R.id.text_view_temp_1);
            }
            assert tv != null;
            tv.setText(String.format(Locale.CHINA, "%d°C", value));
        });
    }

    @Override
    public void onPowerStateChange(ConsoleModel.PowerState powerState) {
        runOnUiThread(() -> {
            ImageButton powerButton = findViewById(R.id.image_button_power);
            TextView tvPowerCn = findViewById(R.id.tv_power_cn);
            TextView tvPowerEn = findViewById(R.id.tv_power_en);
            switch (powerState) {
                case POWER_STATE_RUNNIG:
                    powerButton.setImageResource(R.drawable.icon_console_pause_red_64_64);
                    tvPowerCn.setText("暂停");
                    tvPowerEn.setText("Pause");
                    break;
                case POWER_STATE_PAUSE:
                    powerButton.setImageResource(R.drawable.icon_console_resume_64_64);
                    tvPowerCn.setText("继续");
                    tvPowerEn.setText("Resume");
                    break;
                case POWER_STATE_OFF:
                    powerButton.setImageResource(R.drawable.switch_blue_pressed_64_64);
                    tvPowerCn.setText("开始");
                    tvPowerEn.setText("Start");
                    break;
            }
        });
    }

    /**
     * 设备同步回调
     * @param trueOrFalse 是否同步中
     */
    @Override
    public void onDeviceSync(boolean trueOrFalse) {
        runOnUiThread(() -> {
            ProgressDialog waitDialog = new ProgressDialog(mContext);
            if (trueOrFalse) {
                waitDialog.setTitle(null);
                waitDialog.setMessage("设备同步中，请稍等...");
                waitDialog.setCancelable(false);
                waitDialog.show();
            } else {
                new Handler().postDelayed(waitDialog::dismiss, 1000);//延迟1s关闭对话框
            }
        });
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
}