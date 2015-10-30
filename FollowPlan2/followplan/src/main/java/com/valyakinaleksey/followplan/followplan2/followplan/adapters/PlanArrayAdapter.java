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
import com.valyakinaleksey.followplan.followplan2.followplan.dialogs.PlansDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Plan;

import java.util.ArrayList;
import java.util.List;

public class PlanArrayAdapter extends ArrayAdapter<Plan> {
    public static PlanArrayAdapter instance;
    PlansDialogFragment fragment;
    private LayoutInflater inflater;
    private boolean editable = false;

    private PlanArrayAdapter(Context context, int resource, List<Plan> objects) {
        super(context, resource, objects);
        inflater = LayoutInflater.from(getContext());

    }

    public static PlanArrayAdapter getInstance(Context context, int resource, PlansDialogFragment fragment) {
        if (instance == null) {
            instance = new PlanArrayAdapter(context, resource, new ArrayList<>(Plan.getPlans().values()));
        }
        instance.fragment = fragment;
        return instance;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Plan plan = getItem(position);
        if (convertView == null) {
            convertView = inflater
                    .inflate(R.layout.list_item_plan, null);
        }
        View ivDelete = convertView.findViewById(R.id.iv_delete);
        View ivEdit = convertView.findViewById(R.id.iv_edit);
        ((IconicsImageView) convertView.findViewById(R.id.iv_color)).setIcon(new IconicsDrawable(getContext(), FontAwesome.Icon.faw_circle).color(plan.getColor()).sizeDp(10));
        TextView tvTasksCount = (TextView) convertView.findViewById(R.id.tv_tasks_count);
        if (editable) {
            ivDelete.setVisibility(View.VISIBLE);
            ivEdit.setVisibility(View.VISIBLE);
            tvTasksCount.setVisibility(View.GONE);
        } else {
            ivDelete.setVisibility(View.GONE);
            ivEdit.setVisibility(View.GONE);
            tvTasksCount.setVisibility(View.VISIBLE);
        }
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.deleteConfirmDialogShow(plan);
            }
        });
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.editPlan(plan);
            }
        });
        TextView tvPlanName = (TextView) convertView.findViewById(R.id.tv_plan_name);
        tvPlanName.setText(plan.getName());
        tvTasksCount.setText("" + plan.getTasks().size());
        return convertView;
    }


}
