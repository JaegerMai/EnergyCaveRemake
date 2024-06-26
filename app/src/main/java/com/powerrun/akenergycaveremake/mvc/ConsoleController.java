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

public class ConsoleController {
    private static final String TAG = "ConsoleController";
    private ConsoleModel model;
    private ConsoleView view;
    private BluetoothDataProcessor processor;
    private AppUsageLogger appUsageLogger;
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
        model.setPower0(sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER0, SystemConfig.defaultChan0Level));
        model.setPower1(sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER1, SystemConfig.defaultChan1Level));
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
    private void tempChange(int action, ConsoleModel.Channel channel){
        Log.i(TAG, "tempChange: " + action + ", Channel" + channel);
        //获取当前温度和挡位
        int targetTemp = (channel == ConsoleModel.Channel.CHANNEL_0)
                ? model.getTargetTemp0() : model.getTargetTemp1();
        int powerTemp = (channel == ConsoleModel.Channel.CHANNEL_0)
                ? model.getPower0() : model.getPower1();
        //判断当前温度是否已经达到上下限
        switch (action){
            case ConsoleModel.ADD:
                if(targetTemp >= MyMessage.TARGET_TEMP_MAX){
                    view.onMessage("温度已达上限");
                    break;
                }
                targetTemp += 1;
                //发送温度数据到设备
                writeDataToDevice((channel == ConsoleModel.Channel.CHANNEL_0)
                        ? MyMessage.T_ADD_CH_0 : MyMessage.T_ADD_CH_1);
                //发送挡位数据到设备，如果已经是最大值则不发送
                if(powerTemp < MyMessage.PW_MAX){
                    powerTemp += 1;
                    writeDataToDevice((channel == ConsoleModel.Channel.CHANNEL_0)
                            ? MyMessage.PW_ADD_CODE_0 : MyMessage.PW_ADD_CODE_1);
                }

                break;
            case ConsoleModel.DEC:
                if(targetTemp <= MyMessage.TARGET_TEMP_MIN){
                    view.onMessage("温度已达下限");
                    break;
                }
                targetTemp -= 1;
                //发送温度数据到设备
                writeDataToDevice((channel == ConsoleModel.Channel.CHANNEL_0)
                        ? MyMessage.T_DEC_CH_0 : MyMessage.T_DEC_CH_1);
                //发送挡位数据到设备，如果已经是最小值则不发送
                if(powerTemp > MyMessage.PW_MIN){
                    powerTemp -= 1;
                    writeDataToDevice((channel == ConsoleModel.Channel.CHANNEL_0)
                            ? MyMessage.PW_DEC_CODE_0 : MyMessage.PW_DEC_CODE_1);
                }
                break;
        }
        //更新数据
        switch (channel){
            case CHANNEL_0:
                model.setTargetTemp0(targetTemp);
                model.setPower0(powerTemp);
                break;
            case CHANNEL_1:
                model.setTargetTemp1(targetTemp);
                model.setPower1(powerTemp);
                break;
        }
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
            model.setPower0(data[0]);//通道0功率
            model.setPower1(data[1]);//通道1功率
            if(!needSync) {
                model.setTimeRemain(data[2]);//剩余时间，同步完成后才更新
            }
            model.setCurrentTemp0(getCurrentTemp(data[6], data[7]));//通道0温度
            model.setCurrentTemp1(getCurrentTemp(data[8], data[9]));//通道1温度
            view.onTimeSet(model.getTimeRemain());//同步时间UI
            view.onTempChange(ConsoleModel.Channel.CHANNEL_0, model.getCurrentTemp0());//同步温度UI
            view.onTempChange(ConsoleModel.Channel.CHANNEL_1, model.getCurrentTemp1());
            //同步温度设定值
            model.setTargetTemp0(data[4]);//通道0目标温度
            model.setTargetTemp1(data[5]);//通道1目标温度
            view.onTempSet(ConsoleModel.Channel.CHANNEL_0, model.getTargetTemp0());//同步温度UI
            view.onTempSet(ConsoleModel.Channel.CHANNEL_1, model.getTargetTemp1());//同步温度UI

            //调整温度挡位, 60s调整一次
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastAdjustTime > 60 * 1000){
            adjustBothTempLevels(model.getCurrentTemp0(), model.getCurrentTemp1());
            lastAdjustTime = currentTime;
        }
    }
    /**
     * 重新连接蓝牙时同步数据到设备
     */
    private void syncDataToDevice(byte[] data){
        Log.i(TAG, "syncDataToDevice: 同步数据到设备");
        if(model.getPowerState() != ConsoleModel.PowerState.POWER_STATE_PAUSE){ //等待到开机了，同步完之后才可以正常运行
            return;
        }
        view.onDeviceSync(true);
        int devicePower0 = data[0];//设备通道0功率
        int devicePower1 = data[1];//设备通道1功率
        int deviceTime = data[2];//设备剩余时间
        int deviceTemp0 = data[4];//设备通道0目标温度
        int deviceTemp1 = data[5];//设备通道1目标温度
        Log.i(TAG, String.format("syncDataToDevice: device data: power0:%d, power1:%d, time:%d, temp0:%d, temp1:%d\n",
            devicePower0, devicePower1, deviceTime, deviceTemp0, deviceTemp1));
        //同步数据到设备
        sendSignalToDevice(model.getPower0() - devicePower0, MyMessage.PW_ADD_CODE_0, MyMessage.PW_DEC_CODE_0);
        sendSignalToDevice(model.getPower1() - devicePower1, MyMessage.PW_ADD_CODE_1, MyMessage.PW_DEC_CODE_1);
        sendSignalToDevice((model.getTimeRemain() - deviceTime)/5, MyMessage.TM_ADD_CODE, MyMessage.TM_DEC_CODE);
        sendSignalToDevice(model.getTargetTemp0() - deviceTemp0, MyMessage.T_ADD_CH_0, MyMessage.T_DEC_CH_0);
        sendSignalToDevice(model.getTargetTemp1() - deviceTemp1, MyMessage.T_ADD_CH_1, MyMessage.T_DEC_CH_1);
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
     * 调整两个通道的温度挡位
     * @param currentTemp0 通道0当前温度
     * @param currentTemp1 通道1当前温度
     */
    private void adjustBothTempLevels(int currentTemp0, int currentTemp1){

        int power0 = adjustTempLevel(currentTemp0, model.getTargetTemp0(), model.getPower0(), MyMessage.PW_DEC_CODE_0, MyMessage.PW_ADD_CODE_0);
        int power1 = adjustTempLevel(currentTemp1, model.getTargetTemp1(), model.getPower1(), MyMessage.PW_DEC_CODE_1, MyMessage.PW_ADD_CODE_1);

        model.setPower0(power0);
        model.setPower1(power1);
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
}
