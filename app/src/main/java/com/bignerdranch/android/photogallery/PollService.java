package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by lnzha on 2017/4/19.
 * 关于服务(四大组件之一 在后台运行) IntentService
 * 服务与activity一样需要在AndroidManifest里面注册
 * activity在前台运行
 */

public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60 ; // 60 seconds

    public static Intent newIntent(Context context) {           //android规范 无论谁想用这个服务都必须使用这个方法 规定但是原理是啥???
        return new Intent(context, PollService.class);
    }

    //为了能够在后台持续运行服务 最小时间间隔为60s
    public static void setServiceAlarm(Context context, boolean isOn) {             //启停定时器方法
        Intent i = PollService.newIntent(context);                              //引用newIntent()方法

        //*****pendingintent 将intent打包通知给系统其他组件(如AlarmManager)*****
        //pendingintent.getService()四个参数分别是用来
        //1.发送intent的Context
        //2.区分pendingintent的请求代码
        //3.一个待发送intent的对象
        //4.一组决定如何创建pendingintent的标志符->啥意思????

        //PendingIntent.getService()创建一个用来启动PollService的PendingIntent
        //PendingIntent.getService()打包了一个Context.startService(Intent)方法的调用
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); //获取AlamManager
        //设置或取消计时器
        if (isOn) {
            //设置计时器的方法setInexactRepeating()
            //里面也是4个参数
            //1.描述定时器的常量(具体见sdk参考文档) 下面的是基准时间值
            //2.定时器启动的时间
            //3.定时器循环的时间间隔
            //4.到时要发送的PendingIntent
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        } else {
            //取消计时器
            //取消方法AlarmManager.cancel();
            alarmManager.cancel(pi);
            //一般情况下也需要取消PendingIntent
            pi.cancel();
        }
    }

    //判断PendingIntent是否激活
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        //第四个参数:指的是如果PendingIntent不存在 返回null 而不是创建他
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {              //针对每一个命令在后台运行这方法
        if(!isNetworkAvailableAndConnected())                   //如果连不上网 直接结束服务
            return;

        String query = QueryPreferences.getStoredQuery(this);               //获取该context的最后查询数据
        String lastResultId = QueryPreferences.getLastResultId(this);       //获取最后返回的图片id
        List<GalleryItem> items;
        if (query == null) {
            items = new FlickrFetchr().fetchRecentPhotos();         //如果没有获取到最新的图片 则用抓取数据的方法进行下载
        } else {
            items = new FlickrFetchr().searchPhotos(query);         //如果有则返回最后查询的图片
        }
        if (items.size() == 0) {                                    //如果最新查询的数据对象为0 则结束
            return;
        }
        String resultId = items.get(0).getId();                     //获取最后返回的图片
        if (resultId.equals(lastResultId)) {                        //这个不必解释 很容哟看出结果
            Log.i(TAG, "Got an old result: " + resultId);
        } else {
            Log.i(TAG, "Got a new result: " + resultId);
        }

        Resources resources = getResources();                       //获取资源文件
        Intent i = PhotoGalleryActivity.newIntent(this);            //获取PhotoGalleryActivity的intent
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);        //和上面PendingIntent方法一致
        //通知信息
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.new_pictures_title))        //配置状态栏文字
                .setSmallIcon(android.R.drawable.ic_menu_report_image)              //配置小图标
                .setContentTitle(resources.getString(R.string.new_pictures_title))  //设置标题
                .setContentText(resources.getString(R.string.new_pictures_text))    //显示的文字
                .setContentIntent(pi)                                               //再点击消息的时候 该方法里面的PendingIntent会触发
                .setAutoCancel(true)                                                //触发消息后 Notification自动消失
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);   //从当前context获取NotificationManagerCompat对象
        //贴出消息 第一个参数是标识符 如果是同一标志符那么新的消息会覆盖旧的 在开发中进度条以及其他视觉效果实现的方式
        notificationManager.notify(0, notification);

        QueryPreferences.setLastResultId(this, resultId);           //将最新的数据写入QueryPreferences
    }

    private boolean isNetworkAvailableAndConnected() {          //判断是否能够联网
        //用于判断是否能够联网ConnectivityManager
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE); //？？？具体咋用没有解释
        //getActiveNetworkInfo()使用该方法需要在配置文件中申请权限
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;                       //如果没有联网getActiveNetworkInfo()会返回null
        //isConnected()检查网络是否完全连接 即是否真正联网
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
