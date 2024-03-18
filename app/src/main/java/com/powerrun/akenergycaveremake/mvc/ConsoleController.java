package com.powerrun.akenergycaveremake.mvc;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.powerrun.akenergycaveremake.common.SystemConfig;

public class ConsoleController {
    private static final String TAG = "ConsoleController";
    private ConsoleModel model;
    private ConsoleView view;
    public ConsoleController(ConsoleModel model, ConsoleView view) {
        this.model = model;
        this.view = view;
    }
    /**
     * 开关处理逻辑
     */
    public void handlePowerButton(){
        //TODO: 电源按钮处理逻辑
        ConsoleModel.PowerState currentState = model.getPowerState();
        switch (currentState){
            case POWER_STATE_OFF:
            case POWER_STATE_PAUSE:
                model.setPowerState(ConsoleModel.PowerState.POWER_STATE_RUNNIG);
                break;
            case POWER_STATE_RUNNIG:
                model.setPowerState(ConsoleModel.PowerState.POWER_STATE_PAUSE);
                break;
        }
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
    private void clearDeviceInfo(Context mConext) {
        SharedPreferences sharedPreferences = mConext.getSharedPreferences("save_device", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    /**处理蓝牙数据
     *  第1 2字节是功率门限制
        第3字节是剩余时间计数器
        第4字节bit0是暂停控制，bit1是开关机
        第5 6字节是温度门限 =>舱内温度
        第7-14字节是温感实时温度  =>石墨烯温度
        第15-18预留给能量房用的功率和温度门限
     *
     * //00 关机暂停，01关机运行，10开机暂停，11开机运行
     */
    public void processBleData(byte[] data){
        //判断数据是否异常
        if(null == data || data.length < 16){
            Log.e(TAG, "processBleData: 数据异常");
            return;
        }
        //TODO: 处理蓝牙数据
    }
    /**
     * 向设备写入数据
     */
    public void writeDataToDevice(byte[] data) {
        BleManager.getInstance().write(
                model.getBleDevice(),
                SystemConfig.UUID_SERVICE,
                SystemConfig.UUID_NOTIFY,
                data,
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
}
