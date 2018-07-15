package com.felix.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import com.felix.imageloader.cache.FileCache;
import com.felix.imageloader.cache.MemoryCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Administrator
 */
public class ImageLoader {

    private MemoryCache memoryCache;
    private FileCache fileCache;
    /**
     * 使用线程池去加载图片
     */
    private ExecutorService executorService;

    /**
     * 虚引用,用来存放ImageView对象
     */
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    /**
     * handler to display images in UI thread
     */
    private Handler handler = new Handler();

    public ImageLoader(Context context) {
        memoryCache = new MemoryCache();
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * 当进入listview时默认的图片，可换成你自己的默认图片
     */
    private final int stub_id = R.drawable.stub;

    public void displayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        // 先从内存缓存中查找
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            // 若没有的话则开启新线程加载图片
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(photoToLoad));
    }

    /**
     * 通过URL获取Bitmap
     *
     * @param url 图片的url
     * @return bitmap
     */
    private Bitmap getBitmap(String url) {
        File file = fileCache.getFile(url);
        /*
         * 先从文件缓存中查找是否有
         */
        //from SD cache
        Bitmap bm = decodeFile(file);
        if (bm != null) {
            return bm;
        }
        /*
         *  最后从指定的url中下载图片
         *  from web
         */
        try {
            Bitmap bitmap;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(file);
            Utils.copyStream(is, os);
            //关闭输出流,断开连接
            os.close();
            conn.disconnect();
            bitmap = decodeFile(file);
            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError) {
                memoryCache.clear();
            }
            return null;
        }
    }

    /**
     * decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
     * decodes image and scales it to reduce memory consumption
     */
    private Bitmap decodeFile(File f) {
        try {
            //decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            FileInputStream stream = new FileInputStream(f);
            BitmapFactory.decodeStream(stream, null, options);
            stream.close();

            //Find the correct scale value. It should be the power of 2.
            final int requiredSize = 70;
            int widthTmp = options.outWidth, heightTmp = options.outHeight;
            int scale = 1;
            while (true) {
                if (widthTmp / 2 < requiredSize || heightTmp / 2 < requiredSize) {
                    break;
                }
                widthTmp /= 2;
                heightTmp /= 2;
                scale *= 2;
            }

            //decode with inSampleSize
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;

            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, options2);
            stream2.close();

            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Task for the queue
     */
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        PhotoToLoad(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad)) {
                    return;
                }
                Bitmap bitmap = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bitmap);
                if (imageViewReused(photoToLoad)) {
                    return;
                }
                BitmapDisplayer bitmapDisplayer = new BitmapDisplayer(bitmap, photoToLoad);
                handler.post(bitmapDisplayer);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    /**
     * 防止图片错位
     *
     * @param photoToLoad PhotoToLoad
     * @return 重复使用
     */
    private boolean imageViewReused(PhotoToLoad photoToLoad) {
        String url = imageViews.get(photoToLoad.imageView);
        return url == null || !url.equals(photoToLoad.url);
    }

    /**
     * 用于在UI线程中更新界面(Used to display bitmap in the UI thread)
     */
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        BitmapDisplayer(Bitmap bitmap, PhotoToLoad photoToLoad) {
            this.bitmap = bitmap;
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad)) {
                return;
            }
            if (bitmap != null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
            } else {
                photoToLoad.imageView.setImageResource(stub_id);
            }
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
