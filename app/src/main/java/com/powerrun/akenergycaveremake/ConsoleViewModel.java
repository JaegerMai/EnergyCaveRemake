package com.powerrun.akenergycaveremake;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

public class ConsoleViewModel extends ViewModel {
    private static final String TAG = "ConsoleViewModel";
    private MutableLiveData<String> mText;
    private Context mContext;
    public ConsoleViewModel(Context context, Application application) {
        mContext = context;
        BleManager.getInstance().init(application);
    }
    public void start(){
        //连接蓝牙
        String deviceAddress = getDeviceAddress();
        if(deviceAddress.isEmpty()){
            Log.e(TAG, "start: 未连接设备");
            return;
        }
        BleManager.getInstance().connect(deviceAddress, bleGattCallback);
    }
    public void stop(){

    }
    /**
     * 蓝牙连接回调
     */
    BleGattCallback bleGattCallback = new BleGattCallback() {
        @Override
        public void onStartConnect() {

        }

        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {

        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {

        }
    };
    /**
     * 获取设备地址
     */
    private String getDeviceAddress() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("save_device", MODE_PRIVATE);
        return sharedPreferences.getString("device_address", "");
    }
    /**
     * 清楚设备信息
     */
    private void clearDeviceInfo() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("save_device", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
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
    //TODO： 记录温感历史数据
}
