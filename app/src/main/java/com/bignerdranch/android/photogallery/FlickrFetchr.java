package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lnzha on 2017/4/9.
 * 抓取网页的方法
 * 下载并解析JSON
 */

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "5aec15b77afbb0fe6272d1a6577a5762";

    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";           //获取最新图片的字段
    private static final String SEARCH_METHOD = "flickr.photos.search";                     //搜索的字段
    private static final Uri ENDPOINT = Uri                     //解析JSON
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()//？
            .appendQueryParameter("api_key", API_KEY)               //附加数据appendQueryParameter()
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();                                               //生成相应的url

    public byte[] getUrlBytes(String urlSpec) throws IOException{               //利用输入输出流  标准方法（模板）
        URL url =new URL(urlSpec);              //获取url
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();     //将url转变为openConnect对象
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos() {                  //返回最新图片的方法
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(String query) {           //搜素图片的方法
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            //Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);           //将得到的JSON的字符串转变为JSON对象 被用于parseItems方法
            parseItems(items,jsonBody);     //将对选存入list里面
        } catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    private String buildUrl(String method, String query) {                  //判断是搜素还是直接获取最新图片，能够自动添加字段
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()                       //uri和上面的那个一样 biuldUpon().appendQueryParameter(所要附加的数据)
                .appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD)) {                                 //如果判断是搜索则在网址申请上添加text字段
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();                               //如果没有则直接返回最新图片
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        //目标 奖json对象数据存入list中
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");         //photos是一个总的对象 将其转变为JSON对象
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");      //photo是图片数组对象
        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();

            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) {            //不是每一个图片都有小图片 因此需要检查
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
}
