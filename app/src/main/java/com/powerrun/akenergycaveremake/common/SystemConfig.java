package com.powerrun.akenergycaveremake.common;

import java.util.UUID;

public class SystemConfig {
    public static String mBLEName="AnKangConsole";
    public static String mBLEAddress = "";
    /**
     *   参数存储的sp名
     */
    public final static String SP_ANKANG_EnergyCave = "SpAnKangEnergyCave";
    public final static String SPBLENAME = "BLEAddress";
    public final static String DEFAULT_AIR_POWER0 = "defaultAirPower0";
    public final static String DEFAULT_AIR_POWER1 = "defaultAirPower1";
    public final static String DEFAULT_COST_TIME = "defaultCostTime";
    public final static String DEFAULT_POWER_TYPE = "defaultPowerType";
    /**
     *   参数存储的默认值
     */
    public static int defaultCostTime = 30;
    public static int defaultChan0Level = 3;
    public static int defaultChan1Level = 3;
    public static String defaultPowerType = "new_power";
    /**
     *  蓝牙UUID
     */
    public final static String UUID_NOTIFY ="0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static String UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
}
