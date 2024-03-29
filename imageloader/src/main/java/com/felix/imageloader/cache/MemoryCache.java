package com.felix.imageloader.cache;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author felix.zhong
 * 内存缓存
 */
public class MemoryCache {

    private static final String TAG = "MemoryCache";
    /**
     * 放入缓存时是个同步操作
     * LinkedHashMap构造方法的最后一个参数true代表这个map里的元素将按照最近使用次数由少到多排列，即LRU
     * 这样的好处是如果要将缓存中的元素替换，则先遍历出最近最少使用的元素来替换以提高效率
     */
    private final Map<String, Bitmap> cache = Collections.synchronizedMap(
            //Last argument true for LRU ordering
            new LinkedHashMap<String, Bitmap>(10, 1.5f, true));
    /**
     * 缓存中图片所占用的字节，初始0，将通过此变量严格控制缓存所占用的堆内存
     * current allocated size
     */
    private long size = 0;
    /**
     * 缓存只能占用的最大堆内存
     * max memory in bytes
     */
    private long limit = 1000000;

    public MemoryCache() {
        //use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory() / 4);
    }

    private void setLimit(long newLimit) {
        limit = newLimit;
        Log.i(TAG, "MemoryCache will use up to " + limit / 1024.0 / 1024.0 + "MB");
    }

    public Bitmap get(String url) {
        try {
            if (!cache.containsKey(url)) {
                return null;
            }
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            return cache.get(url);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void put(String url, Bitmap bitmap) {
        try {
            if (cache.containsKey(url)) {
                size -= getSizeInBytes(cache.get(url));
            }
            cache.put(url, bitmap);
            size += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    /**
     * 严格控制堆内存，如果超过将首先替换最近最少使用的那个图片缓存
     */
    private void checkSize() {
        Log.i(TAG, "cache size=" + size + " length=" + cache.size());
        if (size > limit) {
            /*
              先遍历最近最少使用的元素
              least recently accessed item will be the first one iterated
             */
            Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, Bitmap> entry = iter.next();
                size -= getSizeInBytes(entry.getValue());
                iter.remove();
                if (size <= limit) {
                    break;
                }
            }
            Log.i(TAG, "Clean cache. New size " + cache.size());
        }
    }

    public void clear() {
        try {
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            cache.clear();
            size = 0;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 图片占用的内存
     */
    private long getSizeInBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}