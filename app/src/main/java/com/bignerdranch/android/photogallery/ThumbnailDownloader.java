package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lnzha on 2017/4/9.
 * 创建并启动后台线程
 */

public class ThumbnailDownloader<T> extends HandlerThread {  //线程
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;      //标识下载请求信息

    private Boolean mHasQuit = false;
    private Handler mRequestHandler;            //后台进程的Handler*(就是此进程)
    private ConcurrentHashMap<T,String> mRequestMap = new ConcurrentHashMap<>();            //用于并发的HashMap
    private Handler mResponseHandler;           //传递至给前台的Handler
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;


    public interface ThumbnailDownloadListener<T> {                     //监听器接口
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }
    //用于处理已经下载的图片 交给PhotoGalleryFragment代理处理 因为Handler会默认与所在线程相联系
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {                   //该方法用于把下载的图片传给别的视图
        super(TAG);
        mResponseHandler = responseHandler;
    }

    //Message包含三个对象
    //what(用户定义的int型消息代码，用来描述消息) obj(随消息处理的对象) target(处理信息的handle)
    @Override
    protected void onLooperPrepared() {             //线程内置方法
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);              //自己写的下载方法 线程执行后执行
                }
            }
        };
    }

    @Override
    public boolean quit() {         //线程结束
        mHasQuit = true;
        return super.quit();
    }


    public void queueThumbnail(T target, String url) {
        //参数target标识具体哪次下载并确定下载图片更新了哪些元素 target就是photoHolder
        // url代表链接
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            //消息自身是不带URL信息的。我们的做法是使用PhotoHolder和URL的对应关系更新mRequestMap
            mRequestMap.put(target, url);           //将target与url绑定
            //.obtainMessage(...)方法。当传入其他消息字段给它时，该方法会自动设置目标给Handler对象
            //就可以调用sendToTarget()方法将其发送给它的Handler。然后Handler会将这个Message（就是handle本身）放置在Looper消息队列的尾部
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    //如果用户旋转屏幕，因PhotoHolder视图的失效，ThumbnailDownloader可能会挂起。如果点击这些ImageView，就会发生异常
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);//清除队列中的所有请求
    }

    private void handleRequest(final T target) {            //执行下载行为
        try {
            final String url = mRequestMap.get(target);     //通过HashMap获取url
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            //使用BitmapFactory把getUrlBytes(...)返回的字节数组转换为位图
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            mResponseHandler.post(new Runnable() {          //Handler.post(Runnable)是一个发布Message的便利方法
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
}
