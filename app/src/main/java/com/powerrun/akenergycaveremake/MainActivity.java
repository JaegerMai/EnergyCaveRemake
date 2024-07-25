package com.powerrun.akenergycaveremake;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.powerrun.akenergycaveremake.common.ActivityCollector;
import com.powerrun.akenergycaveremake.common.BaseActivity;
import com.powerrun.akenergycaveremake.common.SystemConfig;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private ListView bleListView;
    private AlertDialog scanListDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set full screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);
        //初始化权限
        initPermissions();
        //初始化蓝牙
        initBluetooth();
        ImageButton imageButtonConsole = findViewById(R.id.image_button_console);
        imageButtonConsole.setOnClickListener(view -> {
            if (readDeviceName().equals("")) {
                BleManager.getInstance().scan(bleScanCallback);
            } else {
                SystemConfig.mBLEName = readDeviceName();
                SystemConfig.mBLEAddress = readDeviceAddr();
                Log.i(TAG, "mBLEName:" + SystemConfig.mBLEName + ",mBLEAddress:" + SystemConfig.mBLEAddress);
                Intent intent = new Intent(MainActivity.this, ConsoleActivity.class);
                startActivity(intent);
            }
        });
        //长按进行默认参数设置
        imageButtonConsole.setOnLongClickListener(view -> {
            clearDeviceInfo();
            Toast.makeText(MainActivity.this, "请重新配置蓝牙设备", Toast.LENGTH_SHORT).show();
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("配置参数").create();
            DeployInfoView deployInfoView = new DeployInfoView(MainActivity.this, null);
            dialog.setView(deployInfoView);
            deployInfoView.setAlertDialog(dialog);
            dialog.show();
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanListDialog != null && scanListDialog.isShowing()) {
            scanListDialog.dismiss();
        }
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    private void saveDeviceInfo(String name, String addr) {
        SharedPreferences sharedPreferences = getSharedPreferences("save_device", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("device_name", name);
        editor.putString("device_addr", addr);
        Log.i(TAG, "saveDeviceInfo:device_name:" + name + ",device_addr:" + addr + "\n");
        editor.apply();
    }

    private String readDeviceName() {
        SharedPreferences sharedPreferences = getSharedPreferences("save_device", MODE_PRIVATE);
        String name = sharedPreferences.getString("device_name", "");
        Log.i(TAG, "readDeviceName:" + name + "\n");
        return name;
    }

    private String readDeviceAddr() {
        SharedPreferences sharedPreferences = getSharedPreferences("save_device", MODE_PRIVATE);
        String addr = sharedPreferences.getString("device_addr", "");
        Log.i(TAG, "readDeviceAddr:" + addr + "\n");
        return addr;
    }
    private void clearDeviceInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("save_device", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
//                .setServiceUuids(serviceUuids)
//                .setDeviceName(true, names)
//                .setDeviceMac(mac)
//                .setAutoConnect(isAutoConnect)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    //扫描到的蓝牙设备列表
    private final List<BleDevice> mScanResults = new ArrayList<>();
    //蓝牙相关回调
    BleScanCallback bleScanCallback = new BleScanCallback() {

        @Override
        public void onScanStarted(boolean success) {
            Log.i(TAG, "onScanStarted: 开始扫描");
            if (success) {
                mScanResults.clear();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_scanlist, null);
                bleListView = view.findViewById(R.id.listview);
                bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        BleManager.getInstance().cancelScan();
                        SystemConfig.mBLEName = mScanResults.get(position).getDevice().getName();
                        SystemConfig.mBLEAddress = mScanResults.get(position).getDevice().getAddress();
                        saveDeviceInfo(SystemConfig.mBLEName, SystemConfig.mBLEAddress);
                        Log.i(TAG, "onItemClick: mBLEName:"+SystemConfig.mBLEName+",mBLEAddress:"+SystemConfig.mBLEAddress);
                        //跳转到控制界面
                        Intent intent = new Intent(MainActivity.this, ConsoleActivity.class);
                        startActivity(intent);
                        scanListDialog.dismiss();
                    }
                });
                builder.setView(view);
                builder.setCancelable(true);
                scanListDialog = builder.show();
            }
        }

        @Override
        public void onScanning(BleDevice bleDevice) {
            //过滤掉名字为空的设备
            if(bleDevice.getName() == null || bleDevice.getName().isEmpty() || bleDevice.getName().equals("null")) {
                return;
            }
            //过滤掉重复的设备
            if (mScanResults.contains(bleDevice)) {
                return;
            }
            mScanResults.add(bleDevice);
            //将扫描到的设备名字放入数组，用于显示在ListView中
            final String[] deviceNames = new String[mScanResults.size()];
            for (int i = 0; i < mScanResults.size(); i++) {
                deviceNames[i] = mScanResults.get(i).getName();
            }
            //将设备名字显示在ListView中
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1, deviceNames);
                bleListView.setAdapter(adapter);
            });
        }

        @Override
        public void onScanFinished(List<BleDevice> scanResultList) {
            Log.i(TAG, "onScanFinished: 扫描结束");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //蓝牙开启成功
                Log.i(TAG, "onActivityResult: 蓝牙开启成功");
            } else {
                //蓝牙开启失败
                Log.e(TAG, "onActivityResult: 蓝牙开启失败");
                Toast.makeText(this, "蓝牙开启失败！", Toast.LENGTH_SHORT).show();
                ActivityCollector.finishAll();
            }
        }
    }

    //权限申请列表
    String[] permissions = new String[] {
//            android.Manifest.permission.READ_EXTERNAL_STORAGE,
//            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
    };
    //权限申请码
    private static final int PERMISSION_REQUEST_CODE = 100;
    /**
     * 初始化权限
     */
    private void initPermissions() {
        for(String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //权限申请失败
                    Log.e(TAG, "Permission denied: " + permissions[i]);
                    Toast.makeText(this, "权限：" + permissions[i] + "未开启", Toast.LENGTH_LONG).show();
                    ActivityCollector.finishAll();
                    break;
                }
            }
        }
    }
}