package com.powerrun.akenergycaveremake;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.powerrun.akenergycaveremake.common.BaseActivity;
import com.powerrun.akenergycaveremake.common.RepeatListener;
import com.powerrun.akenergycaveremake.common.SystemConfig;
import com.powerrun.akenergycaveremake.mvc.ConsoleController;
import com.powerrun.akenergycaveremake.mvc.ConsoleModel;
import com.powerrun.akenergycaveremake.mvc.ConsoleView;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public class ConsoleActivity extends BaseActivity implements View.OnClickListener, ConsoleView {
    private static final String TAG = "ConsoleActivity";
    private Context mContext;
    private MusicHelper musicHelper;
    private ProgressDialog progressDialog;
    private ConsoleController controller;
    private ConsoleModel model;
    private static final String FONT_DIGITAL_7 = "raw" + File.separator
            + "digital-7.ttf";
    //按钮点击和长按事件,使用HashMap存储
    private final HashMap<Integer,Runnable> clickActions = new HashMap<>();
    private AppUsageLogger appUsageLogger;

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
        //记录应用使用时间
        appUsageLogger = AppUsageLogger.getInstance(mContext);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicHelper.destroy();
        findViewById(R.id.image_button_music).clearAnimation();
        appUsageLogger.stopLogging();
        controller.stopQueryData();
        controller.handleExit();
        Toast.makeText(mContext, "控制台已终止", Toast.LENGTH_SHORT).show();
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
    private final Handler reconnectHandler = new Handler();//重连蓝牙
    private final BleGattCallback bleGattCallback = new BleGattCallback() {
        @Override
        public void onStartConnect() {
            //显示连接进度条
            if(isFinishing()) {
                return;
            }
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle(null);
            progressDialog.setMessage("正在连接蓝牙设备，请稍等...");
            progressDialog.show();
        }
        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            Log.e(TAG,"BleGattCallback: onConnectFail");
            progressDialog.dismiss();
            controller.stopQueryData();
            //Toast.makeText(getApplicationContext(),"BleGattCallback: onConnectFail",Toast.LENGTH_SHORT).show();
            reconnectHandler.postDelayed(() -> connectBle(), 20000);
        }
        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            progressDialog.dismiss();
            model.setBleDevice(bleDevice);
            //开始交换数据
            BleManager.getInstance().notify(bleDevice, SystemConfig.UUID_SERVICE, SystemConfig.UUID_NOTIFY,bleNotifyCallback);
            controller.startQueryData();
        }
        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            progressDialog.dismiss();
            controller.stopQueryData();
            //清空蓝牙数据队列
            controller.clearDataQueue();
            if (isActiveDisConnected) {
                Toast.makeText(getApplicationContext(), "连接中断", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "已断开连接", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private final BleNotifyCallback bleNotifyCallback = new BleNotifyCallback() {
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
        Log.i(TAG, "短按退出程序");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("警告")
                .setMessage("确定要退出吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    //蓝牙发送退出指令
                    super.onBackPressed();
                    appUsageLogger.stopLogging();
                    controller.stopQueryData();
                    controller.handleExit();
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.create().show();
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
    /**
     * 打开灯光选择对话框
     */
    private boolean[] lightStatus = new boolean[]{false, false};
    private void openLightDialog() {
        String[] light_list = getResources().getStringArray(R.array.light_list);
        ImageButton btnLight = findViewById(R.id.image_button_light);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("设置灯光")
                .setMultiChoiceItems(light_list, lightStatus,
                        (dialog, which, isChecked) -> {
                            lightStatus[which] = isChecked;
                            controller.handleLight(which);
                            if(lightStatus[0] || lightStatus[1]){
                                btnLight.setBackgroundResource(R.drawable.light_yellow);
                            }else{
                                btnLight.setBackgroundResource(R.drawable.light);
                            }
                        });
        builder.create().show();
    }
    /**
     * 打开模式选择对话框
     */
    private void openModeDialog() {
        String[] modeList = getResources().getStringArray(R.array.mode_list);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("选择加热模式")
                .setSingleChoiceItems(modeList, 0, (dialog, which) -> {
                    controller.handleModeChange(which);
                    dialog.dismiss();
                });
        builder.create().show();
    }
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
                findViewById(R.id.image_button_temp_add_2),
                findViewById(R.id.image_button_temp_dec_2),
                findViewById(R.id.image_button_temp_add_3),
                findViewById(R.id.image_button_temp_dec_3),
                findViewById(R.id.image_button_power),
                findViewById(R.id.image_button_music),
                findViewById(R.id.image_button_mode_regulation),
                findViewById(R.id.image_button_light),
                findViewById(R.id.image_button_clear)
        };
        ImageButton[] longPressButtons = new ImageButton[]{
                findViewById(R.id.image_button_time_add),
                findViewById(R.id.image_button_time_dec),
                findViewById(R.id.image_button_temp_add_0),
                findViewById(R.id.image_button_temp_dec_0),
                findViewById(R.id.image_button_temp_add_1),
                findViewById(R.id.image_button_temp_dec_1),
                findViewById(R.id.image_button_temp_add_2),
                findViewById(R.id.image_button_temp_dec_2),
                findViewById(R.id.image_button_temp_add_3),
                findViewById(R.id.image_button_temp_dec_3)
        };
        TextView[] textViews = new TextView[]{
                findViewById(R.id.text_view_time),
                findViewById(R.id.text_view_temp_0),
                findViewById(R.id.text_view_temp_1),
                findViewById(R.id.text_view_temp_2),
                findViewById(R.id.text_view_temp_3)
        };
        int[] buttonIds = new int[]{
                R.id.image_button_time_add,
                R.id.image_button_time_dec,
                R.id.image_button_temp_add_0,
                R.id.image_button_temp_dec_0,
                R.id.image_button_temp_add_1,
                R.id.image_button_temp_dec_1,
                R.id.image_button_temp_add_2,
                R.id.image_button_temp_dec_2,
                R.id.image_button_temp_add_3,
                R.id.image_button_temp_dec_3,
                R.id.image_button_music,
                R.id.image_button_power,
                R.id.image_button_mode_regulation,
                R.id.image_button_light,
                R.id.image_button_clear
        };

        Runnable[] actions = new Runnable[]{
                controller::handleTimeAdd,
                controller::handleTimeDec,
                controller::handleTempAdd0,
                controller::handleTempDec0,
                controller::handleTempAdd1,
                controller::handleTempDec1,
                controller::handleTempAdd2,
                controller::handleTempDec2,
                controller::handleTempAdd3,
                controller::handleTempDec3,
                this::openMusicDialog,
                controller::handlePowerButton,
                this::openModeDialog,
                this::openLightDialog,
                this::clearDeviceInfo
        };

        // 设置点击事件
        for (ImageButton button : buttons) {
            button.setOnClickListener(this);
        }
        // 设置长按事件
        for(ImageButton button : longPressButtons){
            button.setOnTouchListener(new RepeatListener(400, 200, this));
        }
        // 为每个按钮ID添加对应的点击事件
        for (int i = 0; i < buttonIds.length; i++) {
            clickActions.put(buttonIds[i], actions[i]);
        }

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
    }
    /**
     * 温感温度变化回调
     * @param channel 通道
     * @param temp 温度
     */
    @Override
    public void onTempChange(ConsoleModel.Channel channel, int temp) {
        runOnUiThread(() -> {
            int[] imageViewIds = new int[]{
                    R.id.image_view_temp_0,
                    R.id.image_view_temp_1,
                    R.id.image_view_temp_2,
                    R.id.image_view_temp_3
            };

            if(temp >=127 || temp == 85){
                Toast.makeText(mContext, "通道" + channel + "温感为" + temp + "请检查线路",
                        Toast.LENGTH_SHORT).show();
                updateTempDisplayWithException(findViewById(imageViewIds[channel.ordinal()]));
            } else {
                updateTempDisplay(findViewById(imageViewIds[channel.ordinal()]), temp);
            }

        });
    }
    /**
     * 传感器温度变化回调
     * @param sensorNumber 传感器编号
     * @param temp 温度
     */
    @Override
    public void onSensorTempChange(int sensorNumber, int temp) {
        runOnUiThread(() -> {
            // 传感器温度显示
            int[] sensorIds = new int[]{
                    R.id.tv_sensor_0, R.id.tv_sensor_1,
                    R.id.tv_sensor_2, R.id.tv_sensor_3,
                    R.id.tv_sensor_4, R.id.tv_sensor_5,
                    R.id.tv_sensor_6, R.id.tv_sensor_7
            };
            if (sensorNumber >= 0 && sensorNumber < sensorIds.length) {
                TextView tv = findViewById(sensorIds[sensorNumber]);
                tv.setText(String.format(Locale.CHINA, "温感%d：%d°C", sensorNumber, temp));
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
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(mContext, "时间已到", Toast.LENGTH_SHORT).show();
        }
    };
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
            if(value == 1) {
                Toast.makeText(mContext, "剩余时间不足1分钟", Toast.LENGTH_SHORT).show();
                handler.removeCallbacks(runnable); // 取消上一次的延迟任务
                handler.postDelayed(runnable, 5 * 1000); // 创建一个新的延迟任务
            }
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
            int[] textViewIds = new int[]{
                    R.id.text_view_temp_0,
                    R.id.text_view_temp_1,
                    R.id.text_view_temp_2,
                    R.id.text_view_temp_3
            };
            TextView tv = findViewById(textViewIds[channel.ordinal()]);
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
                case POWER_STATE_RUNNING:
                    powerButton.setBackgroundResource(R.drawable.switch_grey_pressed_64_64);
                    tvPowerCn.setText(R.string.stop_cn);
                    tvPowerEn.setText(R.string.stop);
                    break;
                case POWER_STATE_OFF:
                    powerButton.setBackgroundResource(R.drawable.switch_blue_pressed_64_64);
                    tvPowerCn.setText(R.string.start_cn);
                    tvPowerEn.setText(R.string.start);
                    break;
            }
        });
    }

    /**
     * 设备同步回调
     * @param trueOrFalse 是否同步中
     */
    ProgressDialog waitDialog;
    @Override
    public void onDeviceSync(boolean trueOrFalse) {
        runOnUiThread(() -> {
            if (trueOrFalse) {
                waitDialog = new ProgressDialog(mContext);
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
        int index = temp / 5 - 6 + 1;
        if (index < 0) {
            index = 0;
        } else if (index > 4) {
            index = 4;
        }
        iv.setImageResource(tempIcons[index]);
    }
    /**
     * 在温度显示异常时更新温度显示
     * @param iv 异常通道的ImageView
     */
    void updateTempDisplayWithException(ImageView iv){

        iv.setImageResource(R.drawable.icon_console_weatherglass_0_39_104);
        Drawable originalDrawable = iv.getDrawable();
        Bitmap bitmap = Bitmap.createBitmap(originalDrawable.getIntrinsicWidth(), originalDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 在canvas上绘制原始图像
        originalDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        originalDrawable.draw(canvas);

        // 创建一个新的paint对象
        Paint paint = new Paint();
        paint.setColor(Color.RED); // 设置颜色为红色
        paint.setStrokeWidth(10); // 设置线宽

        // 在canvas上绘制叉
        canvas.drawLine(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        canvas.drawLine(canvas.getWidth(), 0, 0, canvas.getHeight(), paint);

        // 将新的bitmap设置为imageView的图像
        iv.setImageBitmap(bitmap);
    }
    /**
     * 清除已保存的设备信息
     */
    private void clearDeviceInfo() {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle("警告")
                .setMessage("确定要清除已保存的设备信息吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("save_device", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    finish();
                    dialog.dismiss();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .create();
        alertDialog.show();
    }
}

