package com.valyakinaleksey.followplan.followplan2.followplan.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
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

import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Task;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.*;
import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.*;

public class TaskActivity extends AppCompatActivity implements ISimpleDialogListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final String TASK_ID = "taskId";
    public static final String PLAN_ID = "planId";
    public static final String PERIOD_POSITION = "periodPosition";
    private static final long EMPTY = 0;

    private EditText etTaskName;
    private AppCompatSpinner planSpinner;
    private Spinner periodSpinner;
    private CheckBox cbDisposable;
    private Plan plan;
    private Period period;
    private Task task;
    private SpinnerPeriodArrayAdapter spinnerPeriodArrayAdapter;
    private TextView tvTimeNotification;
    private TextView tvDateNotification;
    private Locale locale;
    private Button btnSetCancelDate;
    private boolean dateNotificationSet = false;
    private String stringDateNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        etTaskName = (EditText) findViewById(R.id.et_task_name);
        planSpinner = (AppCompatSpinner) findViewById(R.id.spinner_plan);
        periodSpinner = (Spinner) findViewById(R.id.spinner_period);
        cbDisposable = (CheckBox) findViewById(R.id.cb_disposable);
        tvDateNotification = (TextView) findViewById(R.id.tv_date_notification);
        tvTimeNotification = (TextView) findViewById(R.id.tv_time_notification);
        btnSetCancelDate = (Button) findViewById(R.id.btn_set_cancel_date);
        locale = getResources().getConfiguration().locale;
        DateTime dateNotification=DateTime.now();

        Intent intent = getIntent();
        long taskId = intent.getLongExtra(TASK_ID, EMPTY);
        long planId;
        final int periodPosition;
        if (savedInstanceState != null) {
            taskId = savedInstanceState.getLong(TASK_ID);
        }
        if (taskId != EMPTY) {
            task = Task.getTasks().get(taskId);
            etTaskName.setText(task.getName());
            cbDisposable.setChecked(!task.isDisposable());
            period = task.getPeriod();
            plan = task.getPlan();
            periodPosition = plan.getIndexByPeriod(period);
            if (task.getDateNotification().getMillis() != 0) {
                dateNotification = task.getDateNotification();
                tvTimeNotification.setEnabled(true);
                dateNotificationSet = true;
                btnSetCancelDate.setText(R.string.faw_close);
            }
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
        initToolBar();
        initPlanSpinner();
        initPeriodSpinner(periodPosition);
        initNotificationValues(dateNotification);
        initBtnSetCancelListener();
    }

    private void initNotificationValues(DateTime dateNotification) {
        stringDateNotification = dateNotification.toString(DD_MM_YYYY);
        if (dateNotificationSet) {
            tvDateNotification.setText(stringDateNotification);
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern(HH_MM)
                .withLocale(locale);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        DateTime timeNotification = formatter.parseDateTime(sp.getString(getString(R.string.time_notification), DEFAULT_NOTIFICATION_VALUE));
        tvTimeNotification.setText(timeNotification.toString(formatter));
        initTvDateNotificationListener(dateNotification);
        initTvTimeNotificationListener(timeNotification);
    }

    private void initBtnSetCancelListener() {
        btnSetCancelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id;
                dateNotificationSet = !dateNotificationSet;
                if (dateNotificationSet) {
                    id = R.string.faw_close;
                    tvDateNotification.setText(stringDateNotification);
                    tvTimeNotification.setEnabled(true);
                } else {
                    id = R.string.faw_calendar_check;
                    tvDateNotification.setText(getString(R.string.no_notification));
                    tvTimeNotification.setEnabled(false);
                }
                btnSetCancelDate.setText(getString(id));
            }
        });
    }

    private void initTvTimeNotificationListener(final DateTime time) {
        tvTimeNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(TaskActivity.this, time.getHourOfDay(), time.getMinuteOfHour(), true);
                timePickerDialog.show(getFragmentManager(), "TimePickerDialog");
            }
        });
    }

    private void initTvDateNotificationListener(final DateTime dateTime) {
        tvDateNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                period = (Period) periodSpinner.getSelectedItem();
                final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(TaskActivity.this, dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());
                Calendar startDate = Calendar.getInstance();
                Calendar endDate = dateTime.plusDays(period.getInterval() - 1).toCalendar(locale);
                datePickerDialog.setMinDate(startDate);
                datePickerDialog.setMaxDate(endDate);
                datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
            }
        });
    }

    private void initPlanSpinner() {
        final SpinnerPlanArrayAdapter spinnerPlanArrayAdapter = new SpinnerPlanArrayAdapter(getBaseContext(), new ArrayList<>(Plan.getPlans().values()));
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
        initBtnSendListener();
    }

    private void initBtnSendListener() {
        findViewById(R.id.tool_bar_iv_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
                String taskName = etTaskName.getText().toString();
                boolean disposable = !((CheckBox) findViewById(R.id.cb_disposable)).isChecked();
                plan = (Plan) planSpinner.getSelectedItem();
                period = (Period) periodSpinner.getSelectedItem();
                DateTime dateTimeNotification;
                if (dateNotificationSet) {
                    dateTimeNotification = DateTime.parse(tvDateNotification.getText().toString() + tvTimeNotification.getText().toString(),
                            DateTimeFormat.forPattern(DD_MM_YYYY + HH_MM).withLocale(locale));
                } else {
                    dateTimeNotification = new DateTime(0);
                }
                if (!taskName.equals("")) {
                    if (task == null) { //Create Task
                        createTask(databaseHelper, taskName, disposable, dateTimeNotification);

                        setResult(RESULT_CREATE);
                    } else { // Edit Task
                        editTask(databaseHelper, taskName, disposable, dateTimeNotification);

                        setResult(RESULT_EDIT);
                    }
                    databaseHelper.close();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.input_task_name, Toast.LENGTH_SHORT).show();
                }
            }

            private void editTask(DatabaseHelper databaseHelper, String taskName, boolean disposable, DateTime dateTimeNotification) {
                ContentValues contentValues = new ContentValues();
                Plan previousPlan = task.getPlan();
                Period previousPeriod = task.getPeriod();
                if (previousPlan != plan) {
                    plan.addTask(previousPlan.removeTask(task));
                    contentValues.put(Task.PLAN, plan.getId());
                    task.setPlan(plan);
                }
                if (previousPeriod != period) {
                    period.addTask(previousPeriod.removeTask(task));
                    contentValues.put(Task.PERIOD, period.getId());
                    task.setPeriod(period);
                }
                contentValues.put(Task.DATE_NOTIFICATION, dateTimeNotification.getMillis());
                contentValues.put(Task.NAME, taskName);
                contentValues.put(Task.DISPOSABLE, disposable);
                task.setDateNotification(dateTimeNotification);
                task.setName(taskName);
                task.setDisposable(disposable);
                databaseHelper.updateTask(task.getId(), contentValues);
            }

            private void createTask(DatabaseHelper databaseHelper, String taskName, boolean disposable, DateTime dateTimeNotification) {
                int orderNum = databaseHelper.getTaskMaxOrder() + 1;
                DateTime dateCreated = DateTime.now();
                long id = databaseHelper.createTask(taskName, dateCreated, dateTimeNotification, orderNum, plan.getId(), period.getId(), disposable ? 1 : 0);
                Task task = new Task(id, taskName, dateCreated, dateTimeNotification, orderNum, plan, period, false, disposable);
                Task.addTask(task);
                plan.addTask(task);
                period.addTask(task);
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
                    .setTitle(R.string.delete_task_question)
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
        Task.setLastTask(task);
        setResult(RESULT_DELETE);
        finish();
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
        tvDateNotification.setText(String.format(locale, "%02d.%02d.%d", i2, i1 + 1, i));
        if (!dateNotificationSet) {
            dateNotificationSet = true;
            btnSetCancelDate.setText(getString(R.string.faw_close));
            tvTimeNotification.setEnabled(true);
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i1) {
        tvTimeNotification.setText(String.format(locale, "%d:%02d", i, i1));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (task != null) {
            outState.putLong(TASK_ID, task.getId());
        }
        super.onSaveInstanceState(outState);
    }
}