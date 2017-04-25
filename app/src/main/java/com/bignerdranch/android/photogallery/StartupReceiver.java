package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by lnzha on 2017/4/25.
 * about broadcast intent
 * Receiver需要在配置文件中注册
 */

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";
    @Override
    //与服务和activity一样，broadcast receiver是接收intent的组件。
    //当有intent发送给StartupReceiver时，它的onReceive(...)方法会被调用。
    //在配置文件注册后，即使应用未在运行，当系统发送匹配的broadcast intent发来，broadcast receiver就会接收
    //onReceive方法在主线程上运行 不能用于网络连接等费时的工作 但是他可以用在重启后启动定时器
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        //重启后启动定时器
        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }

}
