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
import android.widget.Filterable;
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
import java.util.Collection;
import java.util.List;

import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.DD_MM_YYYY;
import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.HH_MM;

public class TasksArrayAdapter extends ArrayAdapter<Task> implements Filterable {
    public static final String UNCOMPLETED_TASKS = "uncompleted_tasks";
    public static final String COMPLETED_TASKS = "completed_tasks";
    public static final float ALPHA_DONE = 0.5f;
    public static final int ALPHA_UNDONE = 1;
    private final LayoutInflater inflater;
    private List<Task> tasks;
    private List<Task> originalList = new ArrayList<>();
    private Context context;
    private View selectedItem;
    private int selectedPosition = -1;
    private MyFilter myFilter;
    private boolean taskFull = false;
    private boolean searchFilter = false;
    private static String currentFilter = "";

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

//    @Override
//    public void addAll(Collection<? extends Task> collection) {
//        originalList.clear();
//        originalList.addAll(collection);
//        Collection<Task> tasks = new ArrayList<>();
//        for (Task task : collection) {
//            boolean done = task.isDone();
//            if (currentFilter.equals(COMPLETED_TASKS)) {
//                if (!done) {
//                    continue;
//                }
//            } else if (currentFilter.equals(UNCOMPLETED_TASKS)) {
//                if (done) {
//                    continue;
//                }
//            }
//            tasks.add(task);
//        }
//        super.addAll(tasks);
//    }

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
        if (currentFilter.equals(UNCOMPLETED_TASKS)) {
            remove(task);
        }
        task.setDone(true);
        toggleDone(selectedItem, (ViewHolder) selectedItem.getTag(), true);
        task.getPlan().increaseTasksDoneCount(new DatabaseHelper(context));
    }

    public void setTaskUnDone(Task task) {
        if (currentFilter.equals(COMPLETED_TASKS)) {
            remove(task);
        }

        task.setDone(false);
        toggleDone(selectedItem, (ViewHolder) selectedItem.getTag(), false);
        task.getPlan().decreaseTasksDoneCount(new DatabaseHelper(context));
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

    public void setSearchFilter(boolean searchFilter) {
        this.searchFilter = searchFilter;
    }

    private class ViewHolder {
        TextView taskName;
        TextView periodName;
        TextView dateNotification;
    }

    public class ViewHolderFull extends ViewHolder {
        TextView planName;
        IconicsImageView planColor;
    }

    public void setOriginalList(Collection<? extends Task> collection) {
        originalList = new ArrayList<>(collection);
        clear();
        if (searchFilter) {
            addAll(originalList);
        } else {
            tasks = new ArrayList<>();
            for (Task task : collection) {
                boolean done = task.isDone();
                if (currentFilter.equals(COMPLETED_TASKS)) {
                    if (!done) {
                        continue;
                    }
                } else if (currentFilter.equals(UNCOMPLETED_TASKS)) {
                    if (done) {
                        continue;
                    }
                }
                tasks.add(task);
            }
            addAll(tasks);
        }
    }

    @Override
    public void add(Task object) {
        if (!currentFilter.equals(COMPLETED_TASKS)) {
            super.add(object);
        }
        originalList.add(object);
    }


    private class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults result = new FilterResults();
            List<Task> filteredItems = new ArrayList<>();
            if (searchFilter) {

                String query = charSequence.toString().toLowerCase();
                for (Task task : originalList) {
                    if (task.getName().toLowerCase().contains(query)) {
                        filteredItems.add(task);
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                currentFilter = String.valueOf(charSequence);
                if (charSequence.toString().equals(UNCOMPLETED_TASKS)) {
                    fillFilteredList(filteredItems, false);
                } else if (charSequence.equals(COMPLETED_TASKS)) {
                    fillFilteredList(filteredItems, true);
                } else {
                    filteredItems = originalList;
                }
            }
            result.count = filteredItems.size();
            result.values = filteredItems;
            return result;
        }

        private void fillFilteredList(List<Task> filteredItems, boolean b) {
            for (Task task : originalList) {
                if (task.isDone() == b) {
                    filteredItems.add(task);
                }
            }

        }


        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            tasks = (ArrayList<Task>) filterResults.values;
            addAll(tasks);
            notifyDataSetChanged();
        }
    }

    public Filter getFilter() {
        if (myFilter == null) {
            myFilter = new MyFilter();
        }
        return myFilter;
    }
}
