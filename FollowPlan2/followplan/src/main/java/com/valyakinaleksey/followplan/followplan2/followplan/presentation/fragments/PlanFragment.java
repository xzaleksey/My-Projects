package com.valyakinaleksey.followplan.followplan2.followplan.presentation.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.melnykov.fab.FloatingActionButton;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.data.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.domain.adapters.TasksArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.activities.MainActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.activities.PeriodActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.activities.SearchableActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.activities.TaskActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.dialogs.NotificationDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.dialogs.PlansDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.model.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.model.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.PLAN_ID;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.REQUEST_CODE_EDIT_DELETE;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.REQUEST_CODE_SET_NOTIFICATION;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_CANCELED;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_CREATE;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_DELETE;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_EDIT;

public class PlanFragment extends ListFragment implements ISimpleDialogListener {

    public static final String TYPE = "type";
    public static final String POSITION = "position";
    public static final int NOT_SELECTED = -1;
    public static final int TASKS_ALL = 10;
    public static final int TASKS_TODAY = 20;
    public static final int TASKS_PLAN = 30;
    public static final int REQUEST_CODE_TASK = 100;
    public static final int REQUEST_CODE_PERIOD = 200;

    private Plan plan;
    private Task selectedTask;
    private boolean toolBarShown;
    private int listViewPosition = -1;

    private Toolbar toolbarBottom;
    private Button ibDone;
    private Spinner spinner;
    private FloatingActionButton fab;
    private TasksArrayAdapter tasksArrayAdapter;
    private Button ibCreatePeriod;
    private Button ibEditPeriods;
    private int type;
    private ArrayAdapter<Period> periodAdapter;
    private boolean mainActivity;

    public static PlanFragment newInstance(long planId) {
        PlanFragment myFragment = new PlanFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, TASKS_PLAN);
        args.putLong(PLAN_ID, planId);
        myFragment.setArguments(args);
        return myFragment;
    }

    public static PlanFragment newInstance(int type) {
        PlanFragment myFragment = new PlanFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        myFragment.setArguments(args);
        return myFragment;
    }

    public Plan getPlan() {
        return plan;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivity = getActivity() instanceof MainActivity;
        if (mainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            initBtnFab(activity);
        }
        Bundle arguments = getArguments();
        type = arguments.getInt(TYPE);
        plan = Plan.getPlans().get(arguments.getLong(PLAN_ID, 0));
        final ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_plan, null);
        spinner = (Spinner) viewGroup.findViewById(R.id.spinner_period);
        ibCreatePeriod = (Button) viewGroup.findViewById(R.id.ib_create_period);
        ibEditPeriods = (Button) viewGroup.findViewById(R.id.ib_edit_periods);
        if (type == TASKS_PLAN) {
            tasksArrayAdapter = new TasksArrayAdapter(getContext(), new ArrayList<Task>());
        } else {
            tasksArrayAdapter = new TasksArrayAdapter(getContext(), new ArrayList<Task>(), true);
        }
        initToolbarBottom(viewGroup);
        initPeriodListeners();
        return viewGroup;
    }

    private void initPeriodListeners() {
        ibCreatePeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PeriodActivity.class);
                startActivity(intent, PLAN_ID, plan.getId(), REQUEST_CODE_PERIOD);
            }
        });
        ibEditPeriods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PeriodActivity.class);
                if (spinner.getSelectedItemPosition() != 0) {
                    intent.putExtra(PeriodActivity.PERIOD_ID, ((Period) spinner.getSelectedItem()).getId());
                } else {
                    Toast.makeText(getContext(), "Редактировать можно только периоды с интервалом", Toast.LENGTH_SHORT).show();
                }
                startActivity(intent, PLAN_ID, plan.getId(), REQUEST_CODE_PERIOD);
            }
        });
    }

    private void startActivity(Intent intent, String tag, long id, int requestCode) {
        intent.putExtra(tag, id);
        startActivityForResult(intent, requestCode);
    }

    private void initBtnFab(MainActivity activity) {
        fab = activity.getFloatingActionButton();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TaskActivity.class);
                if (Plan.getPlanCount() != 0) {
                    if (type == TASKS_PLAN) {
                        intent.putExtra(PLAN_ID, plan.getId());
                        int spinnerPosition = spinner.getSelectedItemPosition();
                        if (spinnerPosition != 0) {
                            intent.putExtra(TaskActivity.PERIOD_POSITION, spinnerPosition - 1);
                        }
                    }
                    startActivityForResult(intent, REQUEST_CODE_TASK);
                } else {
                    PlansDialogFragment.show(getActivity());
                }
            }
        });
    }

    private void initToolbarBottom(ViewGroup viewGroup) {
        toolbarBottom = (Toolbar) viewGroup.findViewById(R.id.tool_bar_bottom);
        ibDone = (Button) toolbarBottom.findViewById(R.id.ib_done);
        Button ibEditTask = (Button) toolbarBottom.findViewById(R.id.ib_edit);
        Button ibNotificationDate = (Button) viewGroup.findViewById(R.id.ib_notification_date);

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
                startActivity(intent, TaskActivity.TASK_ID, taskId, REQUEST_CODE_TASK);
            }
        });
        ibNotificationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationDialogFragment.show(getActivity(), PlanFragment.this, selectedTask);
            }
        });
    }

    public void unSelectItem() {
        if (listViewPosition != NOT_SELECTED) {
            hideToolbar();
            listViewPosition = NOT_SELECTED;
            tasksArrayAdapter.unHighlightItem(tasksArrayAdapter.getSelectedItem());
            tasksArrayAdapter.setSelectedItem(null);
            tasksArrayAdapter.setSelectedPosition(NOT_SELECTED);
        }
    }

    private void initSpinner() {
        List<Period> periods = new ArrayList<>();
        periods.add(new Period(0, getString(R.string.all_tasks), plan, 0));
        periods.addAll(plan.getPeriods().values());
        periodAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_textview, periods);
        spinner.setAdapter(periodAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {
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
            fillTaskArrayAdapter(plan.getTasks().values());
        } else {
            fillTaskArrayAdapter(((Period) spinner.getSelectedItem()).getTasks().values());
        }
    }

    private void fillTaskArrayAdapter(Collection<Task> tasks) {
        tasksArrayAdapter.setOriginalList(tasks);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();
        MainActivity activity = null;
        if (mainActivity) {
            activity = (MainActivity) getActivity();
            fab.attachToListView(listView);
            fab.show();
        } else {
            tasksArrayAdapter.setSearchFilter(true);
            ((SearchableActivity) getActivity()).setTasksArrayAdapter(tasksArrayAdapter);
        }
        if (type == TASKS_PLAN) {
            initSpinner();
        } else {
            spinner.setVisibility(View.GONE);
            ibCreatePeriod.setVisibility(View.GONE);
            ibEditPeriods.setVisibility(View.GONE);
        }
        if (tasksArrayAdapter.getCount() == 0) {
            switch (type) {
                case TASKS_PLAN:
                    fillTaskArrayAdapter(spinner.getSelectedItemPosition());
                    break;
                case TASKS_ALL:
                    fillTaskArrayAdapter(Task.getTasks().values());
                    break;
                case TASKS_TODAY:
                    fillTaskArrayAdapter(Task.getTasksToday());
                    break;
            }
        }
        listView.setAdapter(tasksArrayAdapter);
        if (activity != null) {
            activity.initSpinnerFilter(tasksArrayAdapter);
        }
        restoreSelection(savedInstanceState);
        initOnItemClickListener(listView);
        initOnBackListener();
    }

    private void restoreSelection(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            listViewPosition = savedInstanceState.getInt(POSITION);
            if (listViewPosition != NOT_SELECTED) {
                tasksArrayAdapter.setSelectedPosition(listViewPosition);
                showToolbar();
            }
        }
    }

    private void initOnItemClickListener(ListView listView) {
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

    private void initOnBackListener() {
        final View view = getView();
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK) {
                        if (isToolBarShown()) {
                            unSelectItem();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }


    private void hideToolbar() {
        toolbarBottom.setVisibility(View.GONE);
        toolbarBottom.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.abc_slide_out_bottom));
        if (mainActivity) {
            fab.show();
        }
        toolBarShown = false;
        getActivity().invalidateOptionsMenu();
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
            ibDone.setText(getString(R.string.faw_undo));
        } else {
            ibDone.setText(getString(R.string.faw_check));
        }
        if (mainActivity) {
            fab.hide();
        }
        toolBarShown = true;
        toolbarBottom.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.abc_slide_in_bottom));
        getActivity().invalidateOptionsMenu();
    }

    private void notifyListViewAdapter() {
        tasksArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TASK) {
            if (resultCode != RESULT_CANCELED) {
                final Task lastTask = Task.getLastTask();
                if (resultCode == RESULT_CREATE) {
                    createTask(lastTask);
                } else if (resultCode == RESULT_DELETE) {
                    deleteTask(lastTask);
                } else if (resultCode == RESULT_EDIT) {
                    editTask();
                }
                notifyListViewAdapter();
            }
        } else if (requestCode == REQUEST_CODE_PERIOD) {
            if (resultCode == RESULT_CREATE) {
                periodAdapter.add(Period.getLastPeriod());
            } else if (resultCode == RESULT_DELETE) {
                periodAdapter.remove(Period.getLastPeriod());
            } else if (resultCode == RESULT_EDIT) {
                periodAdapter.notifyDataSetChanged();
            }
        }
    }

    private void createTask(Task lastTask) {
        if (tasksArrayAdapter.getPosition(lastTask) == -1) {
            tasksArrayAdapter.add(lastTask);
        }
    }

    private void editTask() {
        if (plan != null && plan != selectedTask.getPlan()) {
            tasksArrayAdapter.remove(selectedTask);
        }
    }

    private void deleteTask(Task lastTask) {
        Task.deleteTask(DatabaseHelper.getInstance(getContext()), lastTask);
        tasksArrayAdapter.remove(lastTask);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, listViewPosition);
        super.onSaveInstanceState(outState);
    }

    public boolean isToolBarShown() {
        return toolBarShown;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        int menuID;
        if (toolBarShown) {
            menu.clear();
            inflater.inflate(R.menu.menu_selected_item, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            deleteConfirmDialogShow();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Show delete confirmation dialogue */
    public void deleteConfirmDialogShow() {
        SimpleDialogFragment.createBuilder(getContext(), getFragmentManager())
                .setTitle(R.string.delete_task_question)
                .setPositiveButtonText(R.string.delete)
                .setNegativeButtonText(R.string.cancel).show().setTargetFragment(this, REQUEST_CODE_EDIT_DELETE);
    }

    /* Delete task on dialog confirm*/
    @Override
    public void onPositiveButtonClicked(int i) {
        if (i == REQUEST_CODE_EDIT_DELETE) {
            Task.setLastTask(selectedTask);
            unSelectItem();
            onActivityResult(REQUEST_CODE_TASK, RESULT_DELETE, null);
        } else if (i == REQUEST_CODE_SET_NOTIFICATION) {
            notifyListViewAdapter();
        }
    }

    @Override
    public void onNeutralButtonClicked(int i) {

    }

    @Override
    public void onNegativeButtonClicked(int i) {

    }
}
