package com.powerrun.akenergycaveremake;

import android.os.Build;

import java.util.HashMap;
import java.util.Map;

public class MyMessage {
    public static final String SHDN_CODE = "a";          //开/关机
    public static final String STOP_CODE = "b";          //继续/暂停
    public static final String[] PW_ADD_CODES = new String[]{"c", "e", "3", "5"};  //通道温度加码
    public static final String[] PW_DEC_CODES = new String[]{"d", "f", "4", "6"};  //通道温度减码
    public static final String TM_ADD_CODE = "g";      //时间加10码
    public static final String TM_DEC_CODE = "h";     //时间减10码
    public static final String OLD_TM_ADD_CODE = "w";      //时间加10码
    public static final String OLD_TM_DEC_CODE = "x";     //时间减10码
    public static final String SENT = "i";               //发送温度、时间的查询
    public static final String LIGHT_CM1="`";   //0x60
    public static final String LIGHT_CM2="p";   //0x70

    /**---------------------------------------------------**/
    public static final String[] T_ADD_CH_CODES = new String[]{"l", "n", "<", ">"};          //温度设定增
    public static final String[] T_DEC_CH_CODES = new String[]{"m", "o", "=", "?"};          //温度设定减

    public static final int TARGET_TEMP_MAX = 60;
    public static final int TARGET_TEMP_MIN = 30;

    /**---------------------------------------------------**/

    public static final int PW_MAX = 5;
    public static final int PW_MIN = 0;
    public static final int TM_MAX = 90;
    public static final int TM_MIN = 0;

    private static final Map<String, String> MESSAGE_MAP = new HashMap<>();

    static {
        MESSAGE_MAP.put(SHDN_CODE, "开/关机");
        MESSAGE_MAP.put(STOP_CODE, "继续/暂停");
        MESSAGE_MAP.put(PW_ADD_CODES[0], "通道0温度加码");
        MESSAGE_MAP.put(PW_DEC_CODES[0], "通道0温度减码");
        MESSAGE_MAP.put(PW_ADD_CODES[1], "通道1温度加码");
        MESSAGE_MAP.put(PW_DEC_CODES[1], "通道1温度减码");
        MESSAGE_MAP.put(PW_ADD_CODES[2], "通道2温度加码");
        MESSAGE_MAP.put(PW_DEC_CODES[2], "通道2温度减码");
        MESSAGE_MAP.put(PW_ADD_CODES[3], "通道3温度加码");
        MESSAGE_MAP.put(PW_DEC_CODES[3], "通道3温度减码");
        MESSAGE_MAP.put(TM_ADD_CODE, "时间加10码");
        MESSAGE_MAP.put(TM_DEC_CODE, "时间减10码");
        MESSAGE_MAP.put(SENT, "发送温度、时间的查询");
    }
    /**
     * 通过byte获取发送的信息
     */
    public static String getMessageFromByte(byte[] data){
        String dataStr = new String(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return MESSAGE_MAP.getOrDefault(dataStr, "未知信息");
        }
        return dataStr;
    }
}
