package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {//利用single直接调用的fragment
    //intent(意图) 不用于fragment
    // android使用异步的消息传递机制 intent用来将任务匹配到合适的Activity
    // intent是英勇程序的一个组件 Activity和Activity或者Service之间通信的主要方法
    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleryActivity.class);
    }
    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
