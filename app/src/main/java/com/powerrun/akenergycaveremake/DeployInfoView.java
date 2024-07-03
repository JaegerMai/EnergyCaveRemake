package com.powerrun.akenergycaveremake;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.powerrun.akenergycaveremake.common.SystemConfig;

public class DeployInfoView extends LinearLayout implements View.OnClickListener {
    AlertDialog alertDialog;
    LinearLayout llDeployInfo;
    Spinner spSetTime;
    Spinner spPower0, spPower1, spPower2, spPower3;
    Spinner spPowerType;
    Button btnCancel, btnConfirm;
    Context mContext;
    public DeployInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        llDeployInfo = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.view_deploy,this,true);
        mContext = context;
        initUI();
        initParams();
    }
    /**
     * 设置AlertDialog
     * @param alertDialog AlertDialog
     */
    public void setAlertDialog(AlertDialog alertDialog) {
        this.alertDialog = alertDialog;
    }
    /**
     * 初始化参数
     */
    private void initParams() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SystemConfig.SP_ANKANG_ENERGYCAVE, MODE_PRIVATE);
        int defaultPower0 = sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER0, SystemConfig.defaultChan0Level);
        int defaultPower1 = sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER1, SystemConfig.defaultChan1Level);
        int defaultPower2 = sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER2, SystemConfig.defaultChan2Level);
        int defaultPower3 = sharedPreferences.getInt(SystemConfig.DEFAULT_AIR_POWER3, SystemConfig.defaultChan3Level);
        int defaultCostTime = sharedPreferences.getInt(SystemConfig.DEFAULT_COST_TIME, SystemConfig.defaultCostTime);
        int defaultPowerType = sharedPreferences.getInt(SystemConfig.DEFAULT_POWER_TYPE, SystemConfig.defaultPowerType);

        spPower0.setSelection(defaultPower0);
        spPower1.setSelection(defaultPower1);
        spPower2.setSelection(defaultPower2);
        spPower3.setSelection(defaultPower3);
        spSetTime.setSelection(defaultCostTime/5 - 6);
        spPowerType.setSelection(defaultPowerType);
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        spPower0 = llDeployInfo.findViewById(R.id.sp_power0);
        spPower1 = llDeployInfo.findViewById(R.id.sp_power1);
        spPower2 = llDeployInfo.findViewById(R.id.sp_power2);
        spPower3 = llDeployInfo.findViewById(R.id.sp_power3);
        spSetTime = llDeployInfo.findViewById(R.id.sp_set_time);
        spPowerType = llDeployInfo.findViewById(R.id.sp_power_type);
        btnCancel = llDeployInfo.findViewById(R.id.btn_cancel);
        btnConfirm = llDeployInfo.findViewById(R.id.btn_confirm);

        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_cancel) {
            alertDialog.dismiss();
        } else if (view.getId() == R.id.btn_confirm) {
            // 保存参数到SharedPreferences
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(SystemConfig.SP_ANKANG_ENERGYCAVE, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(SystemConfig.DEFAULT_AIR_POWER0, getPower0());
            editor.putInt(SystemConfig.DEFAULT_AIR_POWER1, getPower1());
            editor.putInt(SystemConfig.DEFAULT_AIR_POWER2, getPower2());
            editor.putInt(SystemConfig.DEFAULT_AIR_POWER3, getPower3());
            editor.putInt(SystemConfig.DEFAULT_COST_TIME, getSetTime());
            editor.putInt(SystemConfig.DEFAULT_POWER_TYPE, getPowerType());
            editor.apply();
            // 更新默认参数
            SystemConfig.defaultChan0Level = getPower0();
            SystemConfig.defaultChan1Level = getPower1();
            SystemConfig.defaultChan2Level = getPower2();
            SystemConfig.defaultChan3Level = getPower3();
            SystemConfig.defaultCostTime = getSetTime();
            SystemConfig.defaultPowerType = getPowerType();
            alertDialog.dismiss();
        }
    }

    /**
     * 获取参数
     */
    private int getPower0() {
        return Integer.parseInt(spPower0.getSelectedItem().toString());
    }
    private int getPower1() {
        return Integer.parseInt(spPower1.getSelectedItem().toString());
    }
    private int getPower2() {
        return Integer.parseInt(spPower2.getSelectedItem().toString());
    }
    private int getPower3() {
        return Integer.parseInt(spPower3.getSelectedItem().toString());
    }
    private int getSetTime() {
        return Integer.parseInt(spSetTime.getSelectedItem().toString());
    }
    private int getPowerType() {
        return spPowerType.getSelectedItem().toString().equals("new_power") ? 1 : 0;
    }
}
