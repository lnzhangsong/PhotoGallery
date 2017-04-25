package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by lnzha on 2017/4/18.
 * 用于存入和读写文件 适合写轻量级的数据(除密码外的)
 */

public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";          //用于activity
    private static final String PREF_LAST_RESULT_ID = "lastResultId";       //用于后台服务 存储图片id
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";             //用于重启后接收信息 关于boardcast intent

    //下面这两个方法用于存储上一次查询的数据
    //context指的值系统组件 eg:activity
    //要获取定制的SharedPreferences需要getSharedPreferences(context,int)方法定制
    public static String getStoredQuery(Context context) {                  //获取所存信息的方法
        //这个应用不需要定制的SharedPreferences所以只需要这个方法就足够了  该方法会返回具有私有权限和默认名称的实例（仅在当前应用内可用）
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);            //获取所存信息 如果没有相应的信息直接传回null(即第二个参数)
    }
    public static void setStoredQuery(Context context, String query) {      //写入信息的方法
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()         ///获取一个SharedPreferences.Editor实例 用于写入数据
                .putString(PREF_SEARCH_QUERY, query)                        //将键值传入SharedPreferences
                .apply();           //完成异步写入
    }

    //以下两个方法用于存储最近一张的图片id
    public static String getLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID, null);
    }
    public static void setLastResultId(Context context, String lastResultId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultId)
                .apply();
    }

    //以下两个方法是用于boardcast intent
    public static boolean isAlarmOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_ALARM_ON, false);
    }
    public static void setAlarmOn(Context context, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }
}
