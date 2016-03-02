package com.valyakinaleksey.followplan.followplan2.followplan.domain.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.valyakinaleksey.followplan.followplan2.followplan.R;

public class SpinnerToolbarAdapter extends ArrayAdapter<String> {
    private String title;
    private LayoutInflater inflater;

    public SpinnerToolbarAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(getContext());
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_toolbar_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.title);
            viewHolder.tvFilter = (TextView) convertView.findViewById(R.id.tv_filter);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(title);
        viewHolder.tvFilter.setText(getItem(position));
        return convertView;
    }

    private class ViewHolder {
        TextView tvTitle;
        TextView tvFilter;
    }
}