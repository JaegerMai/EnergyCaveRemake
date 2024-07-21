package com.powerrun.akenergycaveremake.mvc;

import com.clj.fastble.data.BleDevice;
import com.powerrun.akenergycaveremake.common.SystemConfig;

public class ConsoleModel {
    public enum Channel{
        CHANNEL_0,
        CHANNEL_1,
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
    private int targetTemp0 = 30;//通道0目标温度
    private int targetTemp1 = 30;//通道1目标温度
    private int currentTemp0 = 0;//通道0当前温度
    private int currentTemp1 = 0;//通道1当前温度
    private int power0 = SystemConfig.defaultChan0Level;//通道0功率
    private int power1 = SystemConfig.defaultChan1Level;//通道1功率
    private int powerType = SystemConfig.defaultPowerType;//功率类型

    public int getTimeRemain() {
        return timeRemain;
    }

    public int getTargetTemp0() {
        return targetTemp0;
    }

    public int getTargetTemp1() {
        return targetTemp1;
    }

    public int getCurrentTemp0() {
        return currentTemp0;
    }

    public int getCurrentTemp1() {
        return currentTemp1;
    }

    public int getPower0() {
        return power0;
    }

    public int getPower1() {
        return power1;
    }

    public int getPowerType() {
        return powerType;
    }

    public void setTimeRemain(int timeRemain) {
        this.timeRemain = timeRemain;
    }

    public void setTargetTemp0(int targetTemp0) {
        this.targetTemp0 = targetTemp0;
    }

    public void setTargetTemp1(int targetTemp1) {
        this.targetTemp1 = targetTemp1;
    }

    public void setCurrentTemp0(int currentTemp0) {
        //如果当前温度为0，表示温感未连接，设置为默认温度60
        if (currentTemp0 == 0){
            currentTemp0 = 60;
        }
        this.currentTemp0 = currentTemp0;
    }

    public void setCurrentTemp1(int currentTemp1) {
        //如果当前温度为0，表示温感未连接，设置为默认温度60
        if (currentTemp0 == 0){
            currentTemp0 = 60;
        }
        this.currentTemp1 = currentTemp1;
    }

    public void setPower0(int power0) {
        this.power0 = power0;
    }

    public void setPower1(int power1) {
        this.power1 = power1;
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
