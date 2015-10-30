package com.valyakinaleksey.followplan.followplan2.followplan.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.adapters.SpinnerPeriodArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.adapters.SpinnerPlanArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.dialogs.PlansDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.fragments.PlanFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Task;
import org.joda.time.DateTime;

import java.util.ArrayList;

public class TaskActivity extends AppCompatActivity implements ISimpleDialogListener {
    public static final String TASK_ID = "taskId";
    public static final String PLAN_ID = "planId";
    public static final String PERIOD_POSITION = "periodPosition";
    private static final long EMPTY = 0;
    private EditText etTaskName;
    private Spinner planSpinner;
    private Spinner periodSpinner;
    private CheckBox cbDisposable;
    private Plan plan;
    private Period period;
    private Task task;
    private SpinnerPeriodArrayAdapter spinnerPeriodArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        etTaskName = (EditText) findViewById(R.id.et_task_name);
        planSpinner = (Spinner) findViewById(R.id.spinner_plan);
        periodSpinner = (Spinner) findViewById(R.id.spinner_period);
        cbDisposable = (CheckBox) findViewById(R.id.cb_disposable);

        Intent intent = getIntent();
        long taskId = intent.getLongExtra(TASK_ID, EMPTY);
        long planId;
        int periodPosition;
        if (taskId != EMPTY) {
            task = Task.getTasks().get(taskId);
            etTaskName.setText(task.getName());
            cbDisposable.setChecked(!task.isDisposable());
            period = task.getPeriod();
            plan = task.getPlan();
            periodPosition = plan.getIndexByPeriod(period);
        } else {
            planId = intent.getLongExtra(PLAN_ID, EMPTY);
            if (planId != EMPTY) {
                plan = Plan.getPlans().get(planId);
            } else {
                plan = Plan.getPlanByIndex(0);
            }
            periodPosition = intent.getIntExtra(PERIOD_POSITION, 0);
        }
        spinnerPeriodArrayAdapter = new SpinnerPeriodArrayAdapter(getBaseContext(), R.layout.spinner_textview, new ArrayList<>(plan.getPlanPeriods().values()));
        initPlanSpinner();
        initPeriodSpinner(periodPosition);
        initToolBar();
    }

    private void initPlanSpinner() {
        final SpinnerPlanArrayAdapter spinnerPlanArrayAdapter = new SpinnerPlanArrayAdapter(getBaseContext(), R.layout.spinner_textview, new ArrayList<>(Plan.getPlans().values()));
        planSpinner.setAdapter(spinnerPlanArrayAdapter);
        planSpinner.setSelection(spinnerPlanArrayAdapter.getPosition(plan));
        planSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {
                spinnerPeriodArrayAdapter.clear();
                spinnerPeriodArrayAdapter.addAll(spinnerPlanArrayAdapter.getItem(selectedItemPosition).getPlanPeriods().values());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initPeriodSpinner(int periodPosition) {
        periodSpinner.setAdapter(spinnerPeriodArrayAdapter);
        periodSpinner.setSelection(periodPosition);
    }

    private void initToolBar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        ((TextView) findViewById(R.id.title)).setText(task == null ? R.string.create_task : R.string.edit_task);
//        IconicsImageView iconicsImageView = (IconicsImageView) findViewById(R.id.tool_bar_iv_send);
//        iconicsImageView.setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_send_o).sizeDp(24));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.tool_bar_iv_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
                String taskName = etTaskName.getText().toString();
                boolean disposable = !((CheckBox) findViewById(R.id.cb_disposable)).isChecked();
                plan = (Plan) planSpinner.getSelectedItem();
                period = (Period) periodSpinner.getSelectedItem();
                if (!taskName.equals("")) {
                    if (task == null) {
                        int orderNum = databaseHelper.getTaskMaxOrder() + 1;
                        DateTime dateCreated = DateTime.now();
                        long id = databaseHelper.createTask(taskName, dateCreated, null, orderNum, plan.getId(), period.getId(), disposable ? 1 : 0);
                        Task task = new Task(id, taskName, dateCreated, new DateTime(0), orderNum, plan, period, false, disposable);
                        Task.addTask(task);
                        plan.getTasks().put(id, task);
                        period.getTasks().put(id, task);
                        setResult(PlansDialogFragment.RESULT_CREATE);
                    } else {
                        task.setName(taskName);
                        task.setPlan(plan);
                        task.setPeriod(period);
                        task.setDisposable(disposable);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Task.NAME, taskName);
                        contentValues.put(Task.PLAN, plan.getId());
                        contentValues.put(Task.PERIOD, period.getId());
                        contentValues.put(Task.DISPOSABLE, disposable);
                        databaseHelper.updateTask(task.getId(), contentValues);
//                        currentPlan.setName(taskName);
//                        currentPlan.setColor(color);
//                        ContentValues contentValues = new ContentValues();
//                        contentValues.put(Plan.NAME, taskName);
//                        contentValues.put(Plan.COLOR, color);
//                        databaseHelper.updatePlan(currentPlan.getId(), contentValues);
                        setResult(PlansDialogFragment.RESULT_EDIT);
                    }
                    databaseHelper.close();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.input_task_name, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (task != null) {
            menu.add(R.string.delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(getResources().getString(R.string.delete))) {
            SimpleDialogFragment.createBuilder(getBaseContext(), getSupportFragmentManager())
                    .setTitle(R.string.delete_plan_question)
                    .setPositiveButtonText(R.string.delete)
                    .setNegativeButtonText(R.string.cancel).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNegativeButtonClicked(int i) {

    }

    @Override
    public void onNeutralButtonClicked(int i) {

    }

    @Override
    public void onPositiveButtonClicked(int i) {
        setResult(PlanFragment.RESULT_DELETE);
        finish();
    }
}
