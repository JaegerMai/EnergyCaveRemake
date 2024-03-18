package com.powerrun.akenergycaveremake.mvc;

import com.clj.fastble.data.BleDevice;

import java.util.UUID;

public class ConsoleModel {
    /**--------------------电源状态----------------------------**/
    public enum PowerState{
        POWER_STATE_RUNNIG,
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
    /**--------------------蓝牙设备----------------------------**/
    private BleDevice bleDevice;
    public BleDevice getBleDevice() {
        return bleDevice;
    }
    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }
}
