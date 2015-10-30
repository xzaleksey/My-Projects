package com.valyakinaleksey.followplan.followplan2.followplan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Plan;

import java.util.List;

public class SpinnerPlanArrayAdapter extends ArrayAdapter<Plan> {
    private final LayoutInflater inflater;
    private List<Plan> plans;
    private Context context;
    private boolean done = false;

    public SpinnerPlanArrayAdapter(Context context, int resource, List<Plan> objects) {
        super(context, resource, objects);
        this.context = context;
        plans = objects;
        inflater = LayoutInflater.from(this.context);
    }


    @Override
    public int getCount() {
        return plans.size();
    }

    @Override
    public Plan getItem(int i) {
        return plans.get(i);
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
        Plan plan = getItem(i);
        ((TextView) view).setText(plan.getName());
        return view;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
