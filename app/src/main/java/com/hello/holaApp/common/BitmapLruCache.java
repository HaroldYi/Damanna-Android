package com.hello.holaApp.common;

/**
 * Created by lji5317 on 09/01/2018.
 */

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import com.android.volley.toolbox.ImageLoader;

public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache{

    private static int getDefaultLruCacheSize(){
        int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        int cacheSize = maxMemory /8;
        return cacheSize;
    }

    public BitmapLruCache(){
        super(getDefaultLruCacheSize());
    }

    public BitmapLruCache(int maxSize) {
        super(maxSize);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }



    @Override
    public Bitmap getBitmap(String url) {
        // TODO Auto-generated method stub
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        // TODO Auto-generated method stub
        put(url,bitmap);

    }
}