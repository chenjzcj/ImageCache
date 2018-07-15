package com.felix.imageloader.cache;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * @author Administrator
 * 文件缓存(SD卡缓存)
 */
public class FileCache {

    /**
     * 缓存目录
     */
    private File cacheDir;

    /**
     * 创建文件缓存对象的时候,初始化缓存目录
     *
     * @param context 上下文
     */
    public FileCache(Context context) {
        /*
         *  如果有SD卡则在SD卡中建一个LazyList的目录存放缓存的图片,没有SD卡就放在系统的缓存目录中
         *  Find the dir to save cached images
         */
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(Environment.getExternalStorageDirectory(), "LazyList");
        } else {
            cacheDir = context.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    @SuppressWarnings("AlibabaRemoveCommentedCode")
    public File getFile(String url) {
        //将url的hashCode作为缓存的文件名
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename = String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        //String filename1 = URLEncoder.encode(url,"utf-8");
        return new File(cacheDir, filename);
    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }
}