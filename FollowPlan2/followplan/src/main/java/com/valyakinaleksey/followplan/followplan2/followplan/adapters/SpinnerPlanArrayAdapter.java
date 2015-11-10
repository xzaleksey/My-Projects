package com.valyakinaleksey.followplan.followplan2.followplan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;

import java.util.List;

public class SpinnerPlanArrayAdapter extends ArrayAdapter<Plan> {
    private final LayoutInflater inflater;
    private Context context;
    private boolean done = false;

    public SpinnerPlanArrayAdapter(Context context, List<Plan> objects) {
        super(context, R.layout.spinner_textview, objects);
        this.context = context;
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Plan plan = getItem(i);
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater
                    .inflate(R.layout.spinner_plan, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.planName = (TextView) view.findViewById(R.id.tv_plan_name);
            viewHolder.color = (IconicsImageView) view.findViewById(R.id.iv_color);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.planName.setText(plan.getName());
        viewHolder.color.setIcon(new IconicsDrawable(context, FontAwesome.Icon.faw_circle).color(plan.getColor()).sizeDp(10));
        return view;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public class ViewHolder {
        TextView planName;
        IconicsImageView color;
    }
}
