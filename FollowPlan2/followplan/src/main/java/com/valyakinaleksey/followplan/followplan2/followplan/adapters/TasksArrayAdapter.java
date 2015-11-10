package com.valyakinaleksey.followplan.followplan2.followplan.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Task;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.DD_MM_YYYY;
import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.HH_MM;

public class TasksArrayAdapter extends ArrayAdapter<Task> {
    public static final String TRUE = "true";
    public static final float ALPHA_DONE = 0.5f;
    public static final int ALPHA_UNDONE = 1;
    private final LayoutInflater inflater;
    private List<Task> tasks;
    private List<Task> originalList = new ArrayList<>();
    private Context context;
    private View selectedItem;
    private int selectedPosition = -1;
    private UncompletedTasksFilter uncompletedTasksFilter;
    private boolean taskFull = false;

    public TasksArrayAdapter(Context context, List<Task> objects) {
        super(context, R.layout.list_item_task, objects);
        this.context = context;
        tasks = objects;
        originalList.addAll(tasks);
        inflater = LayoutInflater.from(this.context);
    }

    public TasksArrayAdapter(Context context, List<Task> objects, boolean taskFull) {
        this(context, objects);
        this.taskFull = taskFull;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    public boolean toggleTaskDone(int selectedPosition) {
        Task task = getItem(selectedPosition);
        final boolean done = task.isDone();
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.setTaskDone(task.getId(), !done);
        if (done) {
            setTaskUnDone(task);
            return false;
        } else {
            setTaskDone(task);
            return true;
        }
    }

    public void setTaskDone(Task task) {
        task.setDone(true);
        toggleDone(selectedItem, (ViewHolder) selectedItem.getTag(), true);
    }

    public void setTaskUnDone(Task task) {
        task.setDone(false);
        toggleDone(selectedItem, (ViewHolder) selectedItem.getTag(), false);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Task task = getItem(i);
        final ViewHolder viewHolder;
        if (view == null) {
            if (taskFull) {
                view = inflater
                        .inflate(R.layout.list_item_task_full, viewGroup, false);
                viewHolder = new ViewHolderFull();
                ((ViewHolderFull) viewHolder).planName = (TextView) view.findViewById(R.id.tv_plan_name);
                ((ViewHolderFull) viewHolder).planColor = (IconicsImageView) view.findViewById(R.id.iv_color);
            } else {
                view = inflater
                        .inflate(R.layout.list_item_task, viewGroup, false);
                viewHolder = new ViewHolder();
            }
            viewHolder.taskName = ((TextView) view.findViewById(R.id.tv_task_name));
            viewHolder.periodName = ((TextView) view.findViewById(R.id.tv_period_name));
            viewHolder.dateNotification = ((TextView) view.findViewById(R.id.tv_date_notification_header));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.taskName.setText(task.getName());
        toggleDone(view, viewHolder, task.isDone());
        viewHolder.periodName.setText(task.getPeriod().getName());
        DateTime dateNotification = task.getDateNotification();
        if (dateNotification.getMillis() != 0) {
            viewHolder.dateNotification.setText("{faw-clock-o}" + dateNotification.toString(DateTimeFormat.forPattern(DD_MM_YYYY + " " + HH_MM)));
        } else {
            viewHolder.dateNotification.setText("");
        }
        if (taskFull) {
            final Plan plan = task.getPlan();
            ((ViewHolderFull) viewHolder).planName.setText(plan.getName());
            ((ViewHolderFull) viewHolder).planColor.setIcon(new IconicsDrawable(context, FontAwesome.Icon.faw_circle).color(plan.getColor()).sizeDp(10));
        }
        if (i == selectedPosition) {
            highlightItem(view);
            setSelectedItem(view);
        } else {
            unHighlightItem(view);
        }
        return view;
    }

    private void toggleDone(View view, ViewHolder viewHolder, boolean done) {
        if (done) {
            viewHolder.taskName.setPaintFlags(viewHolder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            view.setAlpha(ALPHA_DONE);
        } else {
            viewHolder.taskName.setPaintFlags(0);
            view.setAlpha(ALPHA_UNDONE);
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void highlightItem(View view) {
        view.setBackgroundColor(context.getResources().getColor(R.color.primary_light));
    }

    public void unHighlightItem(View view) {
        if (view != null) {
            view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
    }

    public View getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(View selectedItem) {
        this.selectedItem = selectedItem;
    }

    public class ViewHolder {
        TextView taskName;
        TextView periodName;
        TextView dateNotification;
    }

    public class ViewHolderFull extends ViewHolder {
        TextView planName;
        IconicsImageView planColor;
    }

    private class UncompletedTasksFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults result = new FilterResults();
            ArrayList<Task> filteredItems = new ArrayList<>();
            if (charSequence.toString().equals(TRUE)) {
                for (Task task : originalList) {
                    if (!task.isDone()) {
                        filteredItems.add(task);
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                result.count = originalList.size();
                result.values = originalList;
            }
            return result;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            tasks = (ArrayList<Task>) filterResults.values;
            clear();
            addAll(tasks);
            notifyDataSetChanged();
        }
    }
}
