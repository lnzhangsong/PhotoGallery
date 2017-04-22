package com.bignerdranch.android.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;import com.bignerdranch.android.photogallery.R;

/**
 * Created by lnzha on 2017/4/9.
 * 这个方法 通用的activity调用fragment的方法
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();


    @Override
    //onCreate在activity第一词启动的时候被调用 因此用于**初始化静态数据**
    //适合执行任何设置 譬如布局和数据的绑定
    protected void onCreate(Bundle savedInstanceState) {
        //bundle在Activity销毁后 将保存先前的状态信息 具体方法是利用onSaveInstanceState()方法保存
        super.onCreate(savedInstanceState);

        //引入fragment布局到Activity
        setContentView(R.layout.activity_fragment);//布局绑定

        //Activity与fragment之间的合作是通过FragmentManager完成的 该方法在Activity和Fragment中都有
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            /*管理Fragment的更改 方法时FragmentTransaction()
            * 该方法有许多操作Fragment
            * 1.可以连接或重新连接到他的父Activity
            * 2.Fragment可以从视图中隐藏取消隐藏
            */
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

}
