package com.felix.lazylist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.felix.imageloader.ImageLoader;

/**
 * @author Administrator
 */
public class LazyAdapter extends BaseAdapter {

    private String[] data;
    private LayoutInflater inflater;
    public ImageLoader imageLoader;

    LazyAdapter(Activity activity, String[] data) {
        this.data = data;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //这里使用applicationcontent,防止内存泄漏
        this.imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.item, null);
        }

        TextView text = vi.findViewById(R.id.text);
        ImageView image = vi.findViewById(R.id.image);
        text.setText("item " + position);
        imageLoader.displayImage(data[position], image);
        return vi;
    }
}