package com.powerrun.akenergycaveremake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.powerrun.akenergycaveremake.common.BaseActivity;

public class ConsoleActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "ConsoleActivity";
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        // TODO: 实现长按逻辑
        return false;
    }
}