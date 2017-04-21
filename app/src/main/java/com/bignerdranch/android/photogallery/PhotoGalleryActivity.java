package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {//利用single直接调用的fragment


    //不懂这段代码的作用
    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleryActivity.class);
    }
    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
