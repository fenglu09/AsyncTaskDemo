package com.pinger.asynctaskdemo;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.pinger.asynctaskdemo.utils.SmsUtil;
import com.pinger.asynctaskdemo.utils.StatusBarUtil;
import com.pinger.asynctaskdemo.utils.ToastUtil;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 填充状态栏
        StatusBarUtil.setColor(this, Color.parseColor("#3F51B5"));

        // 先插入一些短信到手机中
        SmsUtil.insertSms(this);
    }


    /**
     * 点击备份短信
     * @param view
     */
    public void backupSms(View view){
        // 直接调用工具类的回调方法来弹出吐司
        new SmsUtil().backUpSms(this, new SmsUtil.OnTaskListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(MainActivity.this,"备份成功");
            }

            @Override
            public void onFailure() {
                ToastUtil.showShort(MainActivity.this,"备份失败");
            }
        });
    }

    /**
     * 点击还原短信
     * @param view
     */
    public void restoreSms(View view){
        new SmsUtil().restoreSms(this, new SmsUtil.OnTaskListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(MainActivity.this,"还原成功");
            }

            @Override
            public void onFailure() {
                ToastUtil.showShort(MainActivity.this,"还原失败");
            }
        });
    }
}
