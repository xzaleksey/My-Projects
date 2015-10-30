package com.valyakinaleksey.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TasksAdapter extends BaseAdapter {
    private List<Task> tasks;
    private Context c;

    public TasksAdapter(Context c, List<Task> tasks) {
        this.tasks = tasks;
        this.c = c;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Task task = tasks.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(c).inflate(R.layout.task, null);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("Дата: yyyy.MM.dd ", new Locale("ru"));
        ((TextView) convertView.findViewById(R.id.description)).setText(task.getDescription());
        ((TextView) convertView.findViewById(R.id.date)).setText(dateFormat.format(task.getDate().toDate()));
        if (task.isChecked()) {
            convertView.setBackgroundColor(c.getResources().getColor(android.R.color.holo_blue_light));
        }
        else{
            convertView.setBackgroundColor(c.getResources().getColor(android.R.color.white));
        }
        return convertView;
    }
}
