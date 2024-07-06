package com.powerrun.akenergycaveremake.mvc;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.powerrun.akenergycaveremake.AppUsageLogger;
import com.powerrun.akenergycaveremake.BluetoothDataProcessor;
import com.powerrun.akenergycaveremake.MyMessage;
import com.powerrun.akenergycaveremake.common.SystemConfig;

import java.util.Calendar;

public class ConsoleController {
    private static final String TAG = "ConsoleController";
    private ConsoleModel model;
    private ConsoleView view;
    private BluetoothDataProcessor processor;
    private AppUsageLogger appUsageLogger;
    //环境温度，用于调节温度模式
    private Context mContext;
    //每隔1s向设备发送一次数据查询
    private android.os.Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            writeDataToDevice(MyMessage.SENT);
            mHandler.postDelayed(this, 1000);
        }
    };
    public ConsoleController(ConsoleModel model, ConsoleView view, Context context) {
        this.model = model;
        this.view = view;
        this.mContext = context;
        initParams(context);
        processor = new BluetoothDataProcessor(this::updateStatus);
        appUsageLogger = AppUsageLogger.getInstance();
    }
    /**
     * 每隔1s向设备发送一次数据查询
     */
    public void startQueryData(){
        mHandler.postDelayed(mRunnable, 1000);
    }
    public void stopQueryData(){
        mHandler.removeCallbacks(mRunnable);
    }
    /**
     * 初始化能量仓参数
     */
    private void initParams(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SystemConfig.SP_ANKANG_ENERGYCAVE, MODE_PRIVATE);
        model.setPower(ConsoleModel.Channel.CHANNEL_0,sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER0, SystemConfig.defaultChan0Level));
        model.setPower(ConsoleModel.Channel.CHANNEL_1,sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER1, SystemConfig.defaultChan1Level));
        model.setPower(ConsoleModel.Channel.CHANNEL_2,sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER2, SystemConfig.defaultChan2Level));
        model.setPower(ConsoleModel.Channel.CHANNEL_3,sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER3, SystemConfig.defaultChan3Level));
        model.setTimeRemain(sharedPreferences.getInt(SystemConfig.DEFAULT_COST_TIME, SystemConfig.defaultCostTime));
        model.setPowerType(sharedPreferences.getInt(SystemConfig.DEFAULT_POWER_TYPE, SystemConfig.defaultPowerType));
    }

    /**
     * 温度控制处理逻辑
     */
    public void handleTempAdd0(){
        tempChange(ConsoleModel.ADD, ConsoleModel.Channel.CHANNEL_0);
    }
    public void handleTempDec0(){
        tempChange(ConsoleModel.DEC, ConsoleModel.Channel.CHANNEL_0);
    }
    public void handleTempAdd1(){
        tempChange(ConsoleModel.ADD, ConsoleModel.Channel.CHANNEL_1);
    }
    public void handleTempDec1(){
        tempChange(ConsoleModel.DEC, ConsoleModel.Channel.CHANNEL_1);
    }
    public void handleTempAdd2(){
        tempChange(ConsoleModel.ADD, ConsoleModel.Channel.CHANNEL_2);
    }
    public void handleTempDec2(){
        tempChange(ConsoleModel.DEC, ConsoleModel.Channel.CHANNEL_2);
    }
    public void handleTempAdd3(){
        tempChange(ConsoleModel.ADD, ConsoleModel.Channel.CHANNEL_3);
    }
    public void handleTempDec3(){
        tempChange(ConsoleModel.DEC, ConsoleModel.Channel.CHANNEL_3);
    }
    private void tempChange(int action, ConsoleModel.Channel channel){
        Log.i(TAG, "tempChange: " + action + ", Channel" + channel);
        //初始化目标温度和功率数组
        int[] targetTemps = new int[]{
                model.getTargetTemp(ConsoleModel.Channel.CHANNEL_0),
                model.getTargetTemp(ConsoleModel.Channel.CHANNEL_1),
                model.getTargetTemp(ConsoleModel.Channel.CHANNEL_2),
                model.getTargetTemp(ConsoleModel.Channel.CHANNEL_3)
        };
        int[] powerTemps = new int[]{
                model.getPower(ConsoleModel.Channel.CHANNEL_0),
                model.getPower(ConsoleModel.Channel.CHANNEL_1),
                model.getPower(ConsoleModel.Channel.CHANNEL_2),
                model.getPower(ConsoleModel.Channel.CHANNEL_3)
        };

        //初始化消息数组
        String[] tAddCodes = new String[]{MyMessage.T_ADD_CH_CODES[0], MyMessage.T_ADD_CH_CODES[1], MyMessage.T_ADD_CH_CODES[2], MyMessage.T_ADD_CH_CODES[3]};
        String[] tDecCodes = new String[]{MyMessage.T_DEC_CH_CODES[0], MyMessage.T_DEC_CH_CODES[1], MyMessage.T_DEC_CH_CODES[2], MyMessage.T_DEC_CH_CODES[3]};
        String[] pwAddCodes = new String[]{MyMessage.PW_ADD_CODES[0], MyMessage.PW_ADD_CODES[1], MyMessage.PW_ADD_CODES[2], MyMessage.PW_ADD_CODES[3]};
        String[] pwDecCodes = new String[]{MyMessage.PW_DEC_CODES[0], MyMessage.PW_DEC_CODES[1], MyMessage.PW_DEC_CODES[2], MyMessage.PW_DEC_CODES[3]};

        //获取当前通道的索引
        int channelIndex = channel.ordinal();

        //获取当前温度和挡位
        int targetTemp = targetTemps[channelIndex];
        int powerTemp = powerTemps[channelIndex];

        //判断当前温度是否已经达到上下限
        switch (action){
            case ConsoleModel.ADD:
                if(targetTemp >= MyMessage.TARGET_TEMP_MAX){
                    view.onMessage("温度已达上限");
                    break;
                }
                targetTemp += 1;
                //发送温度数据到设备
                writeDataToDevice(tAddCodes[channelIndex]);
                //发送挡位数据到设备，如果已经是最大值则不发送
                if(powerTemp < MyMessage.PW_MAX){
                    powerTemp += 1;
                    writeDataToDevice(pwAddCodes[channelIndex]);
                }

                break;
            case ConsoleModel.DEC:
                if(targetTemp <= MyMessage.TARGET_TEMP_MIN){
                    view.onMessage("温度已达下限");
                    break;
                }
                targetTemp -= 1;
                //发送温度数据到设备
                writeDataToDevice(tDecCodes[channelIndex]);
                //发送挡位数据到设备，如果已经是最小值则不发送
                if(powerTemp > MyMessage.PW_MIN){
                    powerTemp -= 1;
                    writeDataToDevice(pwDecCodes[channelIndex]);
                }
                break;
        }
        //更新数据
        targetTemps[channelIndex] = targetTemp;
        powerTemps[channelIndex] = powerTemp;
        //更新模型数据
        model.setTargetTemp(ConsoleModel.Channel.CHANNEL_0,targetTemps[0]);
        model.setTargetTemp(ConsoleModel.Channel.CHANNEL_1,targetTemps[1]);
        model.setTargetTemp(ConsoleModel.Channel.CHANNEL_2,targetTemps[2]);
        model.setTargetTemp(ConsoleModel.Channel.CHANNEL_3,targetTemps[3]);
        model.setPower(ConsoleModel.Channel.CHANNEL_0,powerTemps[0]);
        model.setPower(ConsoleModel.Channel.CHANNEL_1,powerTemps[1]);
        model.setPower(ConsoleModel.Channel.CHANNEL_2,powerTemps[2]);
        model.setPower(ConsoleModel.Channel.CHANNEL_3,powerTemps[3]);

        view.onTempSet(channel, targetTemp);
    }
    /**
     * 时间控制处理逻辑
     */
    public void handleTimeAdd(){
        timeChange(ConsoleModel.ADD);
    }
    public void handleTimeDec(){
        timeChange(ConsoleModel.DEC);
    }
    private void timeChange(int action){
        Log.i(TAG, "timeChange: " + action + "," + model.getPowerState());
        if(model.getPowerState() == ConsoleModel.PowerState.POWER_STATE_RUNNING){
            //按照能量仓逻辑应先暂停机器才能设置时间
            writeDataToDevice(MyMessage.STOP_CODE);
        }

        int timeRemain = model.getTimeRemain();
        switch (action){
            case ConsoleModel.ADD:
                if(timeRemain >= MyMessage.TM_MAX){
                    view.onMessage("时间已达上限");
                    break;
                }
                model.setTimeRemain(timeRemain + 5);
                writeDataToDevice(model.getPowerType()==ConsoleModel.POWER_TYPE_NEW?
                        MyMessage.TM_ADD_CODE:MyMessage.OLD_TM_ADD_CODE);
                break;
            case ConsoleModel.DEC:
                if(timeRemain <= MyMessage.TM_MIN){
                    view.onMessage("时间已达下限");
                    break;
                }
                model.setTimeRemain(timeRemain - 5);
                writeDataToDevice(model.getPowerType()==ConsoleModel.POWER_TYPE_NEW?
                        MyMessage.TM_DEC_CODE:MyMessage.OLD_TM_DEC_CODE);
                break;
        }
        //继续运行
        writeDataToDevice(MyMessage.STOP_CODE);
        view.onTimeSet(model.getTimeRemain());
    }
    /**
     * 开关处理逻辑
     */
    public void handlePowerButton(){
        ConsoleModel.PowerState currentState = model.getPowerState();
        switch (currentState){
            case POWER_STATE_OFF:
                if(model.getPowerType() == ConsoleModel.POWER_TYPE_NEW){
                    writeDataToDevice(MyMessage.SHDN_CODE);//开机->进入on_stop
                } else {
                    writeDataToDevice(MyMessage.SHDN_CODE);
                    writeDataToDevice(MyMessage.SHDN_CODE);//开机->进入on_stop
                }
                appUsageLogger.startLogging();
                break;
            case POWER_STATE_RUNNING:
                if (model.getPowerState() != ConsoleModel.PowerState.POWER_STATE_OFF) {
                    writeDataToDevice(MyMessage.SHDN_CODE);
                    model.setPowerState(ConsoleModel.PowerState.POWER_STATE_OFF);
                }
                appUsageLogger.stopLogging();
                break;
        }
        view.onPowerStateChange(model.getPowerState());
    }
    /**
     * 退出控制台处理逻辑
     */
    public void handleExit() {
        if (!BleManager.getInstance().isConnected(model.getBleDevice())) {
            return;
        }
        if (model.getPowerState() != ConsoleModel.PowerState.POWER_STATE_OFF) {
            writeDataToDevice(MyMessage.SHDN_CODE);
        }
    }
    /**
     * 灯光控制处理逻辑
     */
    public void handleLight(int lightNumber){
        if (!BleManager.getInstance().isConnected(model.getBleDevice())) {
            view.onMessage("设备未连接");
            return;
        }
        if(model.getPowerState() != ConsoleModel.PowerState.POWER_STATE_RUNNING){
            view.onMessage("请先启动设备");
            return;
        }
        if(lightNumber == 0){
            writeDataToDevice(MyMessage.LIGHT_CMD_0);
        } else if(lightNumber == 1){
            writeDataToDevice(MyMessage.LIGHT_CMD_1);
        }
    }
    /**
     * 添加数据到队列
     * @param data 蓝牙接收到的数据
     */
    public void addData(byte [] data){
        Log.i(TAG, "addData");
        processor.addData(data);
    }
    /**
     * 清空数据队列
     */
    public void clearDataQueue(){
        processor.clearQueue();
    }
    /**
     * 获取温感温度
     * @param channelA 通道的温感1
     * @param channelB 通道的温感2
     * @return 返回温度较低的值
     */
    public int getCurrentTemp(int channelA, int channelB){
        int maxTemp = 127;  //未插温感线
        int exceptionTemp = 85; //温感异常值
        //判断温感是否异常
        if(maxTemp == channelA || exceptionTemp == channelA){
            Log.e(TAG, "getCurrentTemp: channel0温感异常");
            return channelB;
        }
        if(maxTemp == channelB || exceptionTemp == channelB){
            Log.e(TAG, "getCurrentTemp: channel1温感异常");
            return channelA;
        }
        //返回温度较低的值
        return Math.min(channelA, channelB);
    }
    /**更新设备状态
     *  第1 2字节是功率门限制
        第3字节是剩余时间计数器
        第4字节bit0是暂停控制，bit1是开关机
        第5 6字节是温度门限 =>舱内温度
        第7-14字节是温感实时温度  =>石墨烯温度
        第15-18预留给能量房用的功率和温度门限
     * //00 关机暂停，01关机运行，10开机暂停，11开机运行
     */
    //是否需要同步数据到能量仓
    static boolean needSync = true;
    //储存上一次调整温度挡位的时间
    private long lastAdjustTime = 0;
    public void updateStatus(byte[] data){
        //判断数据是否异常
        if(null == data || data.length < 16){
            Log.e(TAG, "processBleData: 数据异常");
            return;
        }

        //bit 1 开关机 10开机暂停，11开机运行
        int powerState = (data[3] & 0x02) >> 1;
        int isRunning = (data[3] & 0x01);
        //判断运行状态
        if(powerState == 1 && isRunning == 1){
            model.setPowerState(ConsoleModel.PowerState.POWER_STATE_RUNNING);
            needSync = false;
        } else if(powerState == 1 && isRunning == 0){
            model.setPowerState(ConsoleModel.PowerState.POWER_STATE_PAUSE);
        } else {
            model.setPowerState(ConsoleModel.PowerState.POWER_STATE_OFF);
            needSync = true;
            return;
        }
        view.onPowerStateChange(model.getPowerState());//同步电源UI

        //开机后先同步设备数据到能量仓
        if(needSync){
            syncDataToDevice(data);
        }

        //解析数据
        int[] devicePowers = new int[]{data[0], data[1], data[14], data[15]};
        int[] deviceTemps = new int[]{data[4], data[5], data[16], data[17]};
        int[] sensorTemps = new int[]{data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13]};

        ConsoleModel.Channel[] channels = ConsoleModel.Channel.values();
        for (int i = 0; i < channels.length; i++) {
            ConsoleModel.Channel channel = channels[i];
            model.setPower(channel, devicePowers[i]);
            model.setCurrentTemp(channel, getCurrentTemp(sensorTemps[i*2], sensorTemps[i*2+1]));
            //同步目标温度
            model.setTargetTemp(channel, deviceTemps[i]);
            //同步UI
            view.onTempChange(channel, model.getCurrentTemp(channel));
            view.onTempSet(channel, model.getTargetTemp(channel));
            //显示各温感数据，维护用
            view.onSensorTempChange(i*2, sensorTemps[i*2]);
            view.onSensorTempChange(i*2+1, sensorTemps[i*2+1]);
        }

        if(!needSync) {
            model.setTimeRemain(data[2]);//剩余时间，同步完成后才更新
        }
        view.onTimeSet(model.getTimeRemain());

        //调整温度挡位, 60s调整一次
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastAdjustTime > 60 * 1000){
            int[] currentTemps = new int[]{
                    model.getCurrentTemp(ConsoleModel.Channel.CHANNEL_0),
                    model.getCurrentTemp(ConsoleModel.Channel.CHANNEL_1),
                    model.getCurrentTemp(ConsoleModel.Channel.CHANNEL_2),
                    model.getCurrentTemp(ConsoleModel.Channel.CHANNEL_3)
            };
            adjustAllTempLevels(currentTemps);
            lastAdjustTime = currentTime;
        }
        //记录环境温度
        saveEnvTemp(getCurrentTemp(data[6], data[7]));
    }
    /**
     * 重新连接蓝牙时同步数据到设备
     */
    private void syncDataToDevice(byte[] data){
        Log.i(TAG, "syncDataToDevice: 同步数据到设备");
        if(model.getPowerState() != ConsoleModel.PowerState.POWER_STATE_PAUSE){
            //等待到开机了，同步完之后才可以正常运行
            return;
        }
        view.onDeviceSync(true);

        int[] devicePowers = new int[]{data[0], data[1], data[14], data[15]};//设备功率
        int deviceTime = data[2];//设备时间
        int[] deviceTemps = new int[]{data[4], data[5], data[16], data[17]};//设备目标温度

        ConsoleModel.Channel[] channels = ConsoleModel.Channel.values();
        for (int i = 0; i < channels.length; i++) {
            ConsoleModel.Channel channel = channels[i];
            int devicePower = devicePowers[i];
            int deviceTemp = deviceTemps[i];
            sendSignalToDevice(model.getPower(channel) - devicePower, MyMessage.PW_ADD_CODES[i], MyMessage.PW_DEC_CODES[i]);
            sendSignalToDevice(model.getTargetTemp(channel) - deviceTemp, MyMessage.T_ADD_CH_CODES[i], MyMessage.T_DEC_CH_CODES[i]);
        }

        sendSignalToDevice((model.getTimeRemain() - deviceTime)/5, MyMessage.TM_ADD_CODE, MyMessage.TM_DEC_CODE);

        Log.i(TAG, "syncDataToDevice: 同步数据到设备完成");
        //转到运行状态pause->running
        writeDataToDevice(MyMessage.STOP_CODE);
        view.onDeviceSync(false);
    }
    /**
     * 同步数据到设备，包括功率、时间、温度
     * @param difference 参数差值
     * @param addCode 增加指令
     * @param decCode 减少指令
     */
    private void sendSignalToDevice(int difference, String addCode, String decCode){
        String signalCode = difference > 0 ? addCode : decCode;
        int times = Math.abs(difference);
        for(int i = 0; i < times; i++){
            writeDataToDevice(signalCode);
        }
    }
    /**
     * 调整温度挡位
     * 当前温度与目标温度相差一定值时，调整挡位
     */
    private int adjustTempLevel(int currentTemp, int targetTemp, int power, String powerDecCode, String powerAddCode) {
        int temperatureTolerance = 2; // 温度误差裕度

        if(currentTemp > targetTemp + temperatureTolerance && power > MyMessage.PW_MIN){
            power -= 1;
            writeDataToDevice(powerDecCode);
        } else if(currentTemp < targetTemp - temperatureTolerance && power < MyMessage.PW_MAX){
            power += 1;
            writeDataToDevice(powerAddCode);
        }
        return power;
    }

    /**
     * 调整所有通道的温度挡位
     * @param currentTemps 各通道当前温度
     */
    private void adjustAllTempLevels(int[] currentTemps){
        ConsoleModel.Channel[] channels = ConsoleModel.Channel.values();
        for (ConsoleModel.Channel channel : channels){
            int currentTemp = currentTemps[channel.ordinal()];
            int targetTemp = model.getTargetTemp(channel);
            int power = adjustTempLevel(currentTemp, targetTemp, model.getPower(channel),
                    MyMessage.PW_DEC_CODES[channel.ordinal()], MyMessage.PW_ADD_CODES[channel.ordinal()]);
            model.setPower(channel, power);
        }
    }
    /**
     * 向设备写入数据
     */
    public void writeDataToDevice(String data) {
        if(!BleManager.getInstance().isConnected(model.getBleDevice())) {
            Log.e(TAG, "writeDataToDevice: 设备未连接");
            return;
        }
        BleManager.getInstance().write(
                model.getBleDevice(),
                SystemConfig.UUID_SERVICE,
                SystemConfig.UUID_NOTIFY,
                data.getBytes(),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.i(TAG, "onWriteSuccess: 写入成功：" + MyMessage.getMessageFromByte(justWrite));
                    }
                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e(TAG, "onWriteFailure: 写入失败");
                    }
                });
    }
    /**
     * 处理模式切换
     */
    public void handleModeChange(int which) {
        if(model.getPowerState() != ConsoleModel.PowerState.POWER_STATE_RUNNING){
            view.onMessage("请先启动设备");
            return;
        }
        int envTemp = readEnvTemp();
        int setTemp = 30;
        //根据环境温度调整目标温度
        int[] tempThreshold = {10, 20, 30, 40, Integer.MAX_VALUE};
        int[] setTemps = {31, 33, 36, 40, 42};
        for(int i = 0; i < tempThreshold.length; i++){
            if(envTemp < tempThreshold[i]){
                setTemp = setTemps[i];
                break;
            }
        }
        //根据温度模式设置补偿
        switch (which){
            case 1:
                //中温模式
                setTemp += 2;
                break;
            case 2:
                //高温模式
                setTemp += 5;
                break;
            default:
                //默认下为低温模式
                break;
        }
        //在子线程中设置温度
        int finalSetTemp = setTemp;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ConsoleModel.Channel[] channels = ConsoleModel.Channel.values();
                while (true) {
                    boolean isAllSet = true;
                    for (ConsoleModel.Channel channel : channels) {
                        int targetTemp = model.getTargetTemp(channel);
                        if (targetTemp < finalSetTemp) {
                            tempChange(ConsoleModel.ADD, channel);
                            isAllSet = false;
                        } else if (targetTemp > finalSetTemp) {
                            tempChange(ConsoleModel.DEC, channel);
                            isAllSet = false;
                        }
                    }
                    if (isAllSet) {
                        break;
                    }

                    // 暂停一段时间以允许设备处理指令
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * 保存环境温度，每天只更新一次，用于调节温度模式
     * @param temp
     */
    private void saveEnvTemp(int temp) {
        //获取当前日期
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);

        //读取上次保存的日期
        int lastDay = readEnvDay();
        if(currentDay != lastDay){
            //保存温度和日期
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("environment_temp", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("temp", temp);
            editor.putInt("day", currentDay);
            editor.apply();
        }
    }

    // 读取环境温度
    private int readEnvTemp() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("environment_temp", MODE_PRIVATE);
        return sharedPreferences.getInt("temp", 0);
    }
    //读取温度记录的日期
    private int readEnvDay() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("environment_temp", MODE_PRIVATE);
        return sharedPreferences.getInt("day", 0);
    }
}
