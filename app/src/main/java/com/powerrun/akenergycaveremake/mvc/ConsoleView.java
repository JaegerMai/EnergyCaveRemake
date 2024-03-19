package com.powerrun.akenergycaveremake.mvc;

public interface ConsoleView {
    void onTempChange(int channel, int temp);
    void onMessage(String msg);
    void onTimeSet(int value);
    void onTempSet(ConsoleModel.Channel channel, int value);
}
