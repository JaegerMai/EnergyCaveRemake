package com.powerrun.akenergycaveremake.mvc;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.powerrun.akenergycaveremake.BluetoothDataProcessor;
import com.powerrun.akenergycaveremake.MyMessage;
import com.powerrun.akenergycaveremake.common.SystemConfig;

public class ConsoleController {
    private static final String TAG = "ConsoleController";
    private ConsoleModel model;
    private ConsoleView view;
    private BluetoothDataProcessor processor;
    public ConsoleController(ConsoleModel model, ConsoleView view) {
        this.model = model;
        this.view = view;
        processor = new BluetoothDataProcessor(new BluetoothDataProcessor.DataListener() {
            @Override
            public void onDataReceived(byte[] data) {
                updateStatus(data);
            }
        });
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
        if(model.getPowerState() == ConsoleModel.PowerState.POWER_STATE_RUNNIG){
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
        //TODO: 电源按钮处理逻辑
        ConsoleModel.PowerState currentState = model.getPowerState();
        switch (currentState){
            case POWER_STATE_OFF:
                if(model.getPowerType() == ConsoleModel.POWER_TYPE_NEW){
                    writeDataToDevice(MyMessage.SHDN_CODE);//开机->进入on_stop
                } else {
                    writeDataToDevice(MyMessage.SHDN_CODE);
                    writeDataToDevice(MyMessage.SHDN_CODE);//开机->进入on_stop
                }
            case POWER_STATE_PAUSE:

                break;
            case POWER_STATE_RUNNIG:

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
     * @param channel0 通道0
     * @param channel1 通道1
     * @return 返回温度较低的值
     */
    public int getCurrentTemp(int channel0, int channel1){
        int maxTemp = 127;  //未插温感线
        int exceptionTemp = 85; //温感异常值
        //判断温感是否异常
        if(maxTemp == channel0 || exceptionTemp == channel0){
            Log.e(TAG, "getCurrentTemp: channel0温感异常");
            return channel1;
        }
        if(maxTemp == channel1 || exceptionTemp == channel1){
            Log.e(TAG, "getCurrentTemp: channel1温感异常");
            return channel0;
        }
        //返回温度较低的值
        if(channel0 < channel1) {
            return channel0;
        } else {
            return channel1;
        }
    }
    /**
     * 清除设备信息
     */
    public void clearDeviceInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("save_device", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    /**更新设备状态
     *  第1 2字节是功率门限制
        第3字节是剩余时间计数器
        第4字节bit0是暂停控制，bit1是开关机
        第5 6字节是温度门限 =>舱内温度
        第7-14字节是温感实时温度  =>石墨烯温度
        第15-18预留给能量房用的功率和温度门限
     *
     * //00 关机暂停，01关机运行，10开机暂停，11开机运行
     */
    public void updateStatus(byte[] data){
        //判断数据是否异常
        if(null == data || data.length < 16){
            Log.e(TAG, "processBleData: 数据异常");
            return;
        }
        //TODO: 处理蓝牙数据
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
    private void adjustBothTempLevels(int currentTemp0, int currentTemp1){
        //TODO 60s调整一次
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
                        Log.i(TAG, "onWriteSuccess: 写入成功");
                    }
                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e(TAG, "onWriteFailure: 写入失败");
                    }
                });
    }
    //TODO: 开一个线程在开始控制的时候记录温感数据，1s一次，原代码里有getStatus方法
}
