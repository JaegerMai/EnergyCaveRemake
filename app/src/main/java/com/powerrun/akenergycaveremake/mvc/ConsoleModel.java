package com.powerrun.akenergycaveremake.mvc;

import com.clj.fastble.data.BleDevice;
import com.powerrun.akenergycaveremake.common.SystemConfig;

public class ConsoleModel {
    public enum Channel{
        CHANNEL_0,
        CHANNEL_1,
        CHANNEL_2,
        CHANNEL_3
    }
    public static final int ADD = 0;
    public static final int DEC = 1;
    public static final int POWER_TYPE_NEW = 1;
    public static final int POWER_TYPE_OLD = 0;
    /**--------------------电源状态----------------------------**/
    public enum PowerState{
        POWER_STATE_RUNNING,
        POWER_STATE_PAUSE,
        POWER_STATE_OFF,
    }

    private PowerState powerState = PowerState.POWER_STATE_OFF;
    public PowerState getPowerState(){
        return powerState;
    }
    public void setPowerState(PowerState powerState){
        this.powerState = powerState;
    }
    /**--------------------能量仓参数----------------------------**/
    private int timeRemain = SystemConfig.defaultCostTime;//剩余时间
    private int[] targetTemps = new int[]{30,30,30,30};//目标温度
    private int[] currentTemps = new int[]{0,0,0,0};//当前温度
    private int[] powers = new int[]{SystemConfig.defaultChan0Level, SystemConfig.defaultChan1Level,
            SystemConfig.defaultChan2Level, SystemConfig.defaultChan3Level};//功率
    private int powerType = SystemConfig.defaultPowerType;//功率类型

    public int getTimeRemain() {
        return timeRemain;
    }

    public int getTargetTemp(Channel channel){
        return targetTemps[channel.ordinal()];
    }

    public int getCurrentTemp(Channel channel){
        return currentTemps[channel.ordinal()];
    }
    public int getPower(Channel channel){
        return powers[channel.ordinal()];
    }

    public int getPowerType() {
        return powerType;
    }

    public void setTimeRemain(int timeRemain) {
        this.timeRemain = timeRemain;
    }

    public void setTargetTemp(Channel channel, int targetTemp){
        targetTemps[channel.ordinal()] = targetTemp;
    }
    public void setCurrentTemp(Channel channel, int currentTemp){
        //如果当前温度为0，表示温感未连接，设置为默认温度60
        if (currentTemp == 0){
            currentTemp = 60;
        }
        currentTemps[channel.ordinal()] = currentTemp;
    }
    public void setPower(Channel channel, int power){
        powers[channel.ordinal()] = power;
    }

    public void setPowerType(int powerType) {
        this.powerType = powerType;
    }

    /**--------------------蓝牙设备----------------------------**/
    private BleDevice bleDevice;
    public BleDevice getBleDevice() {
        return bleDevice;
    }
    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }
}
