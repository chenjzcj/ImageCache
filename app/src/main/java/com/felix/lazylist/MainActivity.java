package com.felix.lazylist;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * 异步加载图片基本思想：
 * 1. 先从内存缓存中获取图片显示（内存缓冲）
 * 2. 获取不到的话从SD卡里获取（SD卡缓冲）
 * 3. 都获取不到的话从网络下载图片并保存到SD卡同时加入内存并显示
 * 4、采用线程池
 * 5、内存缓存+文件缓存
 * 6、内存缓存中网上很多是采用SoftReference来防止堆溢出，这儿严格限制只能使用最大JVM内存的1/4
 * 7、对下载的图片进行按比例缩放，以减少内存的消耗
 *
 * @author Administrator
 */
public class MainActivity extends Activity {

    private ListView list;
    private LazyAdapter adapter;

    public OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            adapter.imageLoader.clearCache();
            adapter.notifyDataSetChanged();
        }
    };

    private final String[] mStrings = {
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091633.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091634.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091635.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091636.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091637.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_1.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_2.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_2.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_3.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_4.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_5.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_6.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_7.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_8.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_9.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091633.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091634.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091635.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091636.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091637.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_1.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_2.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_2.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_3.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_4.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_5.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_6.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_7.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_8.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_9.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091633.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091634.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091635.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091636.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091637.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_1.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_2.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_2.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_3.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_4.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_5.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_6.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_7.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_8.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_9.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091633.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091634.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091635.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091636.jpg",
            "http://www.sifangtu.net/uploads/allimg/150601/1-150601091637.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_1.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_2.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_2.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_3.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_4.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_5.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_6.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_7.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_8.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_9.jpg",
            "http://www.sifangtu.net/uploads/allimg/120330/1_120330100712_9.jpg"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        list = findViewById(R.id.list);
        adapter = new LazyAdapter(this, mStrings);
        list.setAdapter(adapter);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(listener);
    }

    @Override
    public void onDestroy() {
        list.setAdapter(null);
        super.onDestroy();
    }
}