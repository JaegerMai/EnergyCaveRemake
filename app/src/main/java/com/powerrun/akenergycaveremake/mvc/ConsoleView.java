package com.powerrun.akenergycaveremake.mvc;

public interface ConsoleView {
    void onTempChange(ConsoleModel.Channel channel, int temp);
    void onMessage(String msg);
    void onTimeSet(int value);
    void onTempSet(ConsoleModel.Channel channel, int value);
    void onPowerStateChange(ConsoleModel.PowerState powerState);
    void onDeviceSync(boolean trueOrFalse);
}
