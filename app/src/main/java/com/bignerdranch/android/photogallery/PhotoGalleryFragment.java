package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lnzha on 2017/4/9.
 */

/*获取inflater的三种方法
LayoutInflater的作用找res/layout的xml文件
1.LayoutInflater inflater = getLayoutInflater();  //调用Activity的getLayoutInflater()
2.LayoutInflater localinflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
3.LayoutInflater inflater = LayoutInflater.from(context);
*/
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private RecyclerView mPhotoRecyclerView;        //RecyclerView类的任务就是回收再利用以及定位屏幕上的TextView视图
    private List<GalleryItem> mItems = new ArrayList<>();       //modle

    private class PhotoHolder extends RecyclerView.ViewHolder {         //photoholder 是容纳视图的  //更新视图
        //同样第9章  RecyclerView容纳他 他容纳view的
        private ImageView mItemImageView;
        private TextView mItemTextView;
        public PhotoHolder(View itemView) {//view不可以重新写两次
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            //mItemTextView = (TextView) itemView;
        }
        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);//设置图片
        }
        public void bindGalleryItem(GalleryItem item){
            mItemTextView.setText(item.toString());     //设置标题
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        //adpeter  RecyclerView不会自动创建ViewHolder 而是由adapter处理
        //通过调用adapter的getItemCount()方法， RecyclerView询问数组列表中包含多少个对象。
        //RecyclerView调用adapter的createViewHolder(ViewGroup, int)方法创建ViewHolder以及ViewHolder要显示的视图
        //RecyclerView会传入ViewHolder及其位置，调用onBindViewHolder(ViewHolder,int)方法。 adapter会找到目标位置的数据并绑定到ViewHolder的视图上。


        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }
        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            //RecyclerView需要新的View视图来显示列表项时，会调用onCreateViewHolder方法。
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item,viewGroup,false);
            //TextView textView = new TextView(getActivity());
            //return new PhotoHolder(textView);//调用Holder方法 填充视图
            return new PhotoHolder(view);
        }
        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            //onBindViewHolder方法。该方法会把ViewHolder的View视图和模型层数据绑定起来
            GalleryItem galleryItem = mGalleryItems.get(position);
            //Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);//getResources()获取资源方法
            //photoHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());
            //photoHolder.bindGalleryItem(galleryItem);

        }

    }

    public static PhotoGalleryFragment newInstance() {
        //打上newInstance编译器自动生成该方法 应该是固定方法 用于activity调用
        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){        //更新数据
        super.onCreate(savedInstanceState);
        //设置setRetainInstance方法为true后，可以让fragment在activity被重建时保持实例不变。
        setRetainInstance(true);
        //让fragment接收回调方法 关于菜单的回调方法
        setHasOptionsMenu(true);
        /*
        //execute()这个方法啥意思？
        // 解答：调用execute()方法会启动AsyncTask，继而触发后台线程并调用doInBackground(...)方法
        new FetchItemsTask().execute();         //该方法是获取网络数据的接口 获取网络数据 启动后台进程
        */
        updateItems();

        /*
        Intent i = PollService.newIntent(getActivity());        //启动PollService服务
        getActivity().startService(i);
        */
        /*
        该方法被挪到菜单栏 菜单栏里面可以控制服务的启动与停止
        //为了在没有activity的情况下 依然能够启动服务
        PollService.setServiceAlarm(getActivity(),true);       //应用AlarmManager延迟启动服务的方法
        */

        Handler respondseHandler = new Handler();//Handler默认与当前线程的Looper相关联
        mThumbnailDownloader = new ThumbnailDownloader<>(respondseHandler);             //前台Handler与后台相连
        mThumbnailDownloader.setThumbnailDownloadListener(                              //更新UI
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);         //将获取的资源文件转为Drawable
                        photoHolder.bindDrawable(drawable);                                 //将图片放入Holder里面
                    }
                }
        );
        mThumbnailDownloader.start();               //线程启动方法
        mThumbnailDownloader.getLooper();           //Looper控制着整个消息队列
        Log.i(TAG, "Background thread started");
    }

    //菜单有两个方法
    //public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)  此为创建菜单的方法 此方法是由FragmentManager调用的 具体见书P221
    //public boolean onOptionsItemSelected(MenuItem item) 响应菜单选项
    //
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);              //实例化布局

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);             //获取MenuItem 将其保存在searchItem变量中
        final SearchView searchView = (SearchView) searchItem.getActionView();  //使用getActionView()方法从这个变量中取出SearchView对象
        //只要SearchView文本框里的文字有变化onQueryTextChange(String)回调方法就会执行 预览用
        //用户提交搜索查询时， onQueryTextSubmit(String)回调方法就会执行 执行搜索任务
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(),s);           //查询时将字符串写入QueryPreferences
                updateItems();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });


        //更改菜单的标题要在onCreateOptionsMenu()方法里面更改
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {       //实现菜单的业务代码
        switch (item.getItemId()) {
            case R.id.menu_item_clear:                                      //菜单第一个按钮
                QueryPreferences.setStoredQuery(getActivity(), null);       //清除调用其初始化函数
                updateItems();                                              //数据更新
                return true;
            case R.id.menu_item_toggle_polling:                             //菜单的第二个按钮
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);

                getActivity().invalidateOptionsMenu();//让菜单失效??? 让PhotoGalleryActivity更新菜单栏视图
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());      //从QueryPreferences获取数据
        new FetchItemsTask(query).execute();                 //后台联网线程的启动
    }

    @Override
    public void onDestroyView() {           //视图销毁后 调用清空队列的方法
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {               //销毁时将线程销毁 即：调用quit()方法
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }
    @Override
    //初始化布局
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        // LayoutInflater的作用是，把一个View的对象与XML布局文件关联并实例化
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        // View的对象实例化之后，可以通过findViewById()查找布局文件中的指定Id的组件
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        //RecyclerView定位的任务被委托给了LayoutManager 除了在屏幕上定位列表项， LayoutManager还负责定义屏幕滚动行为
        setupAdapter();//绑定数据

        return v;
    }



    private void setupAdapter() {

        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
            //通过setAdapter方法来调用一个listAdapter来绑定数据
        }
    }

    //AssyncTask 线程的第三个参数返回结果数据类型
    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {          //真正获取网络数据的方法 此时调用里另一个线程
        private String Query;
        //AsyncTask这个类啥意思？
        //解答：即创建一个后台线程，然后从该线程访问网络，因而使用AsyncTask工具类 但是AsyncTask有其局限性
        public FetchItemsTask(String query){
            this.Query = query;
        }
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            /*这个方法被FlickrFetchr().fetchItems()代替 里面已经将flickAPI写入
            try {
                String result = new FlickrFetchr().getUrlString("https://www.bignerdranch.com");    //调用联网方法
                Log.i(TAG, "Fetched contents of URL: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch URL: ", ioe);
            }*/
            //return new FlickrFetchr().fetchItems();  fetchItems()方法被替换成为下面的方法

            if (Query == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(Query);
            }

        }
        //该方法在doInBackground后被启用 并且是在主线程上
        //为了防止发生冲突 UI更新不容许在后台线程上更新
        @Override
        protected void onPostExecute(List<GalleryItem> items){          //线程方法
            mItems = items;             //更新UI数据
            setupAdapter();
        }
    }
}
