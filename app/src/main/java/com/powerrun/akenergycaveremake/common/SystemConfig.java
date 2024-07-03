package com.powerrun.akenergycaveremake.common;

public class SystemConfig {
    public static String mBLEName="AnKangConsole";
    public static String mBLEAddress = "";
    /**
     *   参数存储的sp名
     */
    public final static String SP_ANKANG_ENERGYCAVE = "SpAnKangEnergyCave";
    public final static String DEFAULT_AIR_POWER0 = "defaultAirPower0";
    public final static String DEFAULT_AIR_POWER1 = "defaultAirPower1";
    public final static String DEFAULT_AIR_POWER2 = "defaultAirPower2";
    public final static String DEFAULT_AIR_POWER3 = "defaultAirPower3";
    public final static String DEFAULT_COST_TIME = "defaultCostTime";
    public final static String DEFAULT_POWER_TYPE = "defaultPowerType";
    /**
     *   参数存储的默认值
     */
    public static int defaultCostTime = 30;
    public static int defaultChan0Level = 3;
    public static int defaultChan1Level = 3;
    public static int defaultChan2Level = 3;
    public static int defaultChan3Level = 3;
    public static int defaultPowerType = 1;//new_power为1,old_power为0
    /**
     *  蓝牙UUID
     */
    public final static String UUID_NOTIFY ="0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static String UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
}
