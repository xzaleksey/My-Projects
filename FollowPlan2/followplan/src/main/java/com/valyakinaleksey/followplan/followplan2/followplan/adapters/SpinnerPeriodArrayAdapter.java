package com.valyakinaleksey.followplan.followplan2.followplan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Period;

import java.util.List;

public class SpinnerPeriodArrayAdapter extends ArrayAdapter<Period> {
    private final LayoutInflater inflater;
    private List<Period> periods;
    private Context context;
    private boolean done = false;

    public SpinnerPeriodArrayAdapter(Context context, int resource, List<Period> objects) {
        super(context, resource, objects);
        this.context = context;
        periods = objects;
        inflater = LayoutInflater.from(this.context);
    }


    @Override
    public int getCount() {
        return periods.size();
    }

    @Override
    public Period getItem(int i) {
        return periods.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater
                    .inflate(R.layout.spinner_textview, viewGroup, false);
        }
        Period period = getItem(i);
        ((TextView) view).setText(period.getName());
        return view;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
