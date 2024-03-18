package com.powerrun.akenergycaveremake;

public class MyMessage {
    public static final String SHDN_CODE = "a";          //开/关机
    public static final String STOP_CODE = "b";          //继续/暂停
    public static final String PW_ADD_CODE_0 = "c";  //通道0温度加码 上舱
    public static final String PW_DEC_CODE_0 = "d";  //通道0温度减码 上舱
    public static final String PW_ADD_CODE_1 = "e";  //通道1温度加码 e    下舱
    public static final String PW_DEC_CODE_1 = "f";  //通道1温度减码 d    下舱
    public static final String TM_ADD_CODE = "g";      //时间加10码
    public static final String TM_DEC_CODE = "h";     //时间减10码
    public static final String OLD_TM_ADD_CODE = "w";      //时间加10码
    public static final String OLD_TM_DEC_CODE = "x";     //时间减10码
    public static final String SENT = "i";               //发送温度、时间的查询

    /**---------------------------------------------------**/
    public static final String T_ADD_CH_0 = "l";          //10.ch0温度设定增：l
    public static final String T_DEC_CH_0 = "m";          //11.ch0温度设定减：m
    public static final String T_ADD_CH_1 = "n";          //12.ch1温度设定增：n
    public static final String T_DEC_CH_1 = "o";          //13.ch1温度设定减：o

    public static final int PW_ch0_MAX = 60;
    public static final int PW_ch0_MIN = 30;
    public static final int PW_ch1_MAX = 60;
    public static final int PW_ch1_MIN = 30;

    /**---------------------------------------------------**/

    public static final int PW_0_MAX = 5;
    public static final int PW_0_MIN = 0;
    public static final int PW_1_MAX = 5;
    public static final int PW_1_MIN = 0;
    public static final int TM_MAX = 90;
    public static final int TM_MIN = 0;

}
