package com.powerrun.akenergycaveremake.common;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d("BaseActivity",getClass().getSimpleName() + " onCreate");
        //添加Activity到堆栈
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("BaseActivity",getClass().getSimpleName() + " onDestroy");
        //结束Activity从栈中移除该Activity
        ActivityCollector.removeActivity(this);
    }
}
