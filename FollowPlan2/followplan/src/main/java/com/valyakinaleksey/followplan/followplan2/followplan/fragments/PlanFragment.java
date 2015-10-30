package com.valyakinaleksey.followplan.followplan2.followplan.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.melnykov.fab.FloatingActionButton;
import com.valyakinaleksey.followplan.followplan2.followplan.MainActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.activities.TaskActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.adapters.TasksArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.dialogs.PlansDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Task;

import java.util.ArrayList;
import java.util.List;

public class PlanFragment extends ListFragment {

    public static final int RESULT_CANCELED = 0;
    public static final int REQUEST_CODE_CREATE_TASK = 1;
    public static final int REQUEST_CODE_EDIT_DELETE_TASK = 2;
    public static final String PLAN_ID = "planId";
    public static final int RESULT_CREATE = 4;
    public static final int RESULT_EDIT = 5;
    public static final int RESULT_DELETE = 6;
    public static final String POSITION = "position";
    public static final int NOT_SELECTED = -1;

    private Plan plan;
    private FloatingActionButton fab;
    private TasksArrayAdapter tasksArrayAdapter;
    private int listViewPosition = -1;
    private Toolbar toolbarBottom;
    private boolean showAllTasks = true;
    private Button ibDone;
    private Task selectedTask;
    private Spinner spinner;

    public static PlanFragment newInstance(long planId) {
        PlanFragment myFragment = new PlanFragment();
        Bundle args = new Bundle();
        args.putLong(PLAN_ID, planId);
        myFragment.setArguments(args);
        return myFragment;
    }

    public Plan getPlan() {
        return plan;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        Bundle arguments = getArguments();
        plan = Plan.getPlans().get(arguments.getLong(PLAN_ID, 0));
        final ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_plan, null);
        spinner = (Spinner) viewGroup.findViewById(R.id.spinner_period);
        tasksArrayAdapter = new TasksArrayAdapter(getContext(), R.layout.task, new ArrayList<Task>());
        fab = activity.getFloatingActionButton();
        initToolbarBottom(viewGroup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TaskActivity.class);
                if (Plan.getPlanCount() != 0) {
                    if (plan != null) {
                        intent.putExtra(TaskActivity.PLAN_ID, plan.getId());
                        int spinnerPosition = spinner.getSelectedItemPosition();
                        if (spinnerPosition != 0) {
                            intent.putExtra(TaskActivity.PERIOD_POSITION, spinnerPosition - 1);
                        }
                    }
                    startActivityForResult(intent, REQUEST_CODE_CREATE_TASK);
                } else {
                    PlansDialogFragment.show(getActivity());
                }
            }
        });
        return viewGroup;
    }

    private void initToolbarBottom(ViewGroup viewGroup) {
        toolbarBottom = (Toolbar) viewGroup.findViewById(R.id.tool_bar_bottom);
        ibDone = (Button) toolbarBottom.findViewById(R.id.ib_done);
        Button ibEditTask = (Button) toolbarBottom.findViewById(R.id.ib_edit);
        ibDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tasksArrayAdapter.toggleTaskDone(listViewPosition);
                unSelectItem();
            }
        });
        ibEditTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TaskActivity.class);
                long taskId = selectedTask.getId();
                unSelectItem();
                intent.putExtra(TaskActivity.TASK_ID, taskId);
                startActivityForResult(intent, REQUEST_CODE_EDIT_DELETE_TASK);
            }
        });
    }

    private void unSelectItem() {
        hideToolbar();
        listViewPosition = NOT_SELECTED;
        tasksArrayAdapter.unHighlightItem(tasksArrayAdapter.getSelectedItem());
        tasksArrayAdapter.setSelectedItem(null);
        tasksArrayAdapter.setSelectedPosition(NOT_SELECTED);
    }

    private void initSpinner(Spinner spinner) {
        List<String> periodNames = new ArrayList<>();
        periodNames.add(getString(R.string.all_tasks));
        for (Period period : plan.getPlanPeriods().values()) {
            periodNames.add(period.toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_textview, periodNames);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {
                tasksArrayAdapter.clear();
                fillTaskArrayAdapter(selectedItemPosition);
                if (listViewPosition != NOT_SELECTED && tasksArrayAdapter.getSelectedItem() != null) {
                    unSelectItem();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void fillTaskArrayAdapter(int selectedItemPosition) {
        if (selectedItemPosition == 0) {
            tasksArrayAdapter.addAll(plan.getTasks().values());
        } else {
            tasksArrayAdapter.addAll(plan.getPeriodByIndex(selectedItemPosition - 1).getTasks().values());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();
        initSpinner(spinner);
        if (tasksArrayAdapter.getCount() == 0) {
            fillTaskArrayAdapter(spinner.getSelectedItemPosition());
        }
        listView.setAdapter(tasksArrayAdapter);
        if (savedInstanceState != null) {
            listViewPosition = savedInstanceState.getInt(POSITION);
            if (listViewPosition != NOT_SELECTED) {
                tasksArrayAdapter.setSelectedPosition(listViewPosition);
                showToolbar();
            }
        }
        ((MainActivity) getActivity()).setListViewMain(listView);
        fab.attachToListView(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                final View selectedItem = tasksArrayAdapter.getSelectedItem();
                boolean selected = false;
                if (selectedItem != null) {
                    if (selectedItem != view) {
                        selected = true;
                        tasksArrayAdapter.unHighlightItem(selectedItem);
                    }
                } else {//первый выбор
                    selected = true;
                }
                if (selected) {
                    tasksArrayAdapter.setSelectedPosition(position);
                    selectItem(view, position);
                    showToolbar();
                } else {
                    unSelectItem();
                }
            }
        });
    }

    private void hideToolbar() {
        toolbarBottom.setVisibility(View.GONE);
        fab.show();
    }

    private void selectItem(View view, int position) {
        listViewPosition = position;
        tasksArrayAdapter.highlightItem(view);
        tasksArrayAdapter.setSelectedItem(view);
    }

    private void showToolbar() {
        toolbarBottom.setVisibility(View.VISIBLE);
        selectedTask = tasksArrayAdapter.getItem(listViewPosition);
        if (selectedTask.isDone()) {
            ibDone.setText(getString(R.string.ib_undo));
        } else {
            ibDone.setText(getString(R.string.ib_done));
        }
        fab.hide();
    }

    private void notifyListViewAdapter() {
        tasksArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            if (resultCode == RESULT_CREATE) {
                tasksArrayAdapter.add(Task.getLastTask());
            } else if (resultCode == RESULT_DELETE) {

            } else {
                if (plan != null) {
                    if (plan != selectedTask.getPlan()) {
                        final long selectedTaskId = selectedTask.getId();
                        long newPlanId = selectedTask.getPlan().getId();
                        Plan.getPlans().get(newPlanId).getTasks().put(selectedTaskId, plan.getTasks().remove(selectedTaskId));
                        tasksArrayAdapter.remove(selectedTask);
                    }
                }
            }
            notifyListViewAdapter();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, listViewPosition);
        super.onSaveInstanceState(outState);
    }

}
