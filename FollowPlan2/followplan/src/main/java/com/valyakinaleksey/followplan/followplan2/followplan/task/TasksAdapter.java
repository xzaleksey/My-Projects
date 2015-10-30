package com.valyakinaleksey.followplan.followplan2.followplan.task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.valyakinaleksey.followplan.followplan2.followplan.R;

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
        ((TextView) convertView.findViewById(R.id.tv_task_name)).setText(task.getName());
        ((TextView) convertView.findViewById(R.id.tv_period_name)).setText(dateFormat.format(task.getDateCreated().toDate()));
        if (task.isDone()) {
            convertView.setBackgroundColor(c.getResources().getColor(android.R.color.holo_blue_light));
        } else {
            convertView.setBackgroundColor(c.getResources().getColor(android.R.color.white));
        }
        return convertView;
    }
}
