package com.valyakinaleksey.followplan.followplan2.followplan.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsButton;
import com.mikepenz.iconics.view.IconicsImageView;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.dialogs.PlansDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;

import java.util.List;

public class PlanArrayAdapter extends ArrayAdapter<Plan> {
    private PlansDialogFragment fragment;
    private LayoutInflater inflater;
    private boolean editable = false;
    private final String hexColor;

    public PlanArrayAdapter(Context context, int resource, List<Plan> objects, PlansDialogFragment fragment) {
        super(context, resource, objects);
        inflater = LayoutInflater.from(getContext());
        this.fragment = fragment;
        hexColor = "\"#" + Integer.toHexString(getContext().getResources().getColor(R.color.accent)).substring(2) + "\"";
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }


    private class ViewHolder {
        IconicsButton ibDelete;
        IconicsButton ibEdit;
        IconicsImageView ivColor;
        TextView tvPlanName;
        TextView tvTasksCount;
        TextView tvPercent;
        IconicsButton ibRefresh;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Plan plan = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater
                    .inflate(R.layout.list_item_plan, null);
            viewHolder = new ViewHolder();
            viewHolder.ibDelete = (IconicsButton) convertView.findViewById(R.id.ib_delete);
            viewHolder.ibEdit = (IconicsButton) convertView.findViewById(R.id.ib_edit);
            viewHolder.ivColor = ((IconicsImageView) convertView.findViewById(R.id.iv_color));
            viewHolder.tvTasksCount = (TextView) convertView.findViewById(R.id.tv_tasks_count);
            viewHolder.tvPlanName = (TextView) convertView.findViewById(R.id.tv_plan_name);
            viewHolder.tvPercent = (TextView) convertView.findViewById(R.id.tv_plan_percent);
            viewHolder.ibRefresh = (IconicsButton) convertView.findViewById(R.id.ib_refresh);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        fillViewHolder(plan, viewHolder);
        initListeners(plan, viewHolder);
        return convertView;
    }

    private void fillViewHolder(Plan plan, ViewHolder viewHolder) {
        viewHolder.ivColor.setIcon(new IconicsDrawable(getContext(), FontAwesome.Icon.faw_circle).color(plan.getColor()).sizeDp(10));
        int completePercent = 100;
        if (plan.getTotalTasksCount() != 0) {
            completePercent = plan.getTotalDoneTasksCount() * 100 / plan.getTotalTasksCount();
        }

        viewHolder.tvPercent.setText(Html.fromHtml(getContext().getString(R.string.total) + " " + plan.getTotalDoneTasksCount() + "/" + plan.getTotalTasksCount()
                + " <b><font color=" + hexColor + ">" + completePercent + " %</font></b>"));
        viewHolder.tvTasksCount.setText("" + plan.getCurrentDoneTasksCount() + "/" + plan.getCurrentTasksCount());
        showEditable(viewHolder);
        viewHolder.tvPlanName.setText(plan.getName());
    }

    private void showEditable(ViewHolder viewHolder) {
        if (editable) {
            viewHolder.ibDelete.setVisibility(View.VISIBLE);
            viewHolder.ibEdit.setVisibility(View.VISIBLE);
            viewHolder.ibRefresh.setVisibility(View.VISIBLE);
            viewHolder.tvTasksCount.setVisibility(View.GONE);
        } else {
            viewHolder.ibDelete.setVisibility(View.GONE);
            viewHolder.ibEdit.setVisibility(View.GONE);
            viewHolder.ibRefresh.setVisibility(View.GONE);
            viewHolder.tvTasksCount.setVisibility(View.VISIBLE);
        }
    }

    private void initListeners(final Plan plan, ViewHolder viewHolder) {
        viewHolder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.deleteConfirmDialogShow(plan);
            }
        });
        viewHolder.ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.editPlan(plan);
            }
        });
        viewHolder.ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.resetConfirmDialogShow(plan);
            }
        });
    }


}
