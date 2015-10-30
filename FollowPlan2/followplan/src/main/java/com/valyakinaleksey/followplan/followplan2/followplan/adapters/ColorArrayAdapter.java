package com.valyakinaleksey.followplan.followplan2.followplan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.valyakinaleksey.followplan.followplan2.followplan.R;

public class ColorArrayAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final Context context;
    private int[] colors;

    public ColorArrayAdapter(Context context, int[] colors) {
        this.context = context;
        inflater = LayoutInflater.from(this.context);
        this.colors = colors;
    }


    @Override
    public int getCount() {
        return colors.length;
    }

    @Override
    public Object getItem(int i) {
        return colors[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int color = (int) getItem(position);
        if (convertView == null) {
            convertView = inflater
                    .inflate(R.layout.color_choose, null);
        }
        IconicsImageView convertView1 = (IconicsImageView) convertView;
        convertView1.setIcon(new IconicsDrawable(context, FontAwesome.Icon.faw_circle).color(color).sizeDp(10));
        return convertView;
    }
}
