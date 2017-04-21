package com.bignerdranch.android.photogallery;

/**
 * Created by lnzha on 2017/4/9.
 * modle层
 */

public class GalleryItem {
    private String mCaption;
    private String mId;
    private String mUrl;
    @Override
    public String toString() {
        return mCaption;
    }
    public String getId(){
        return mId;
    }
    public String getUrl(){
        return mUrl;
    }
    public void setId(String mId){
        this.mId=mId;
    }
    public void setCaption(String mCaption){
        this.mCaption=mCaption;
    }
    public void setUrl(String mUrl){
        this.mUrl=mUrl;
    }

}
