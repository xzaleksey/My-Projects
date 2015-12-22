package com.valyakinaleksey.followplan.followplan2.followplan.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.MyService;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.adapters.SpinnerPeriodArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.adapters.SpinnerPlanArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Task;
import com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.DATE_NOTIFICATION;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.PLAN_ID;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.RESULT_CREATE;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.RESULT_DELETE;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.RESULT_EDIT;
import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.DD_MM_YYYY;
import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.DEFAULT_NOTIFICATION_VALUE;
import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.HH_MM;

public class TaskActivity extends AppCompatActivity implements ISimpleDialogListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final String TASK_ID = "taskId";
    public static final String PERIOD_POSITION = "periodPosition";

    public static final String DATE_NOTIFICATION_SET = "dateNotificationSet";
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
    private DateTime dateNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        etTaskName = (EditText) findViewById(R.id.et_period_days);
        planSpinner = (AppCompatSpinner) findViewById(R.id.spinner_plan);
        periodSpinner = (Spinner) findViewById(R.id.spinner_period);
        cbDisposable = (CheckBox) findViewById(R.id.cb_disposable);
        tvDateNotification = (TextView) findViewById(R.id.tv_date_start);
        tvTimeNotification = (TextView) findViewById(R.id.tv_time_notification);
        btnSetCancelDate = (Button) findViewById(R.id.btn_set_cancel_date);
        locale = getResources().getConfiguration().locale;


        Intent intent = getIntent();
        long taskId = getTaskId(savedInstanceState, intent);
        long planId;
        final int periodPosition;
        task = Task.getTasks().get(taskId);
        initDateTimeNotification(savedInstanceState);
        if (task != null) {
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
        spinnerPeriodArrayAdapter = new SpinnerPeriodArrayAdapter(getBaseContext(), R.layout.spinner_textview, new ArrayList<>(plan.getPeriods().values()));
        initToolBar();
        initPlanSpinner();
        initPeriodSpinner(periodPosition);
        initBtnSetCancelListener();
        initTvDateNotificationListener();
        initTvTimeNotificationListener();
    }

    private void initDateTimeNotification(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (task != null && task.getDateNotification().getMillis() != 0) {
                dateNotificationSet = true;
                dateNotification = task.getDateNotification();
            } else {
                dateNotification = DateTime.now();
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String[] timeNotification = sp.getString(getString(R.string.time_notification), DEFAULT_NOTIFICATION_VALUE).split(":");
                dateNotification = dateNotification.withHourOfDay(Integer.parseInt(timeNotification[0])).withMinuteOfHour(Integer.parseInt(timeNotification[1]));
            }

        } else {
            dateNotification = (DateTime) savedInstanceState.getSerializable(DATE_NOTIFICATION);
            dateNotificationSet = savedInstanceState.getBoolean(DATE_NOTIFICATION_SET);
        }
        if (dateNotificationSet) {
            tvTimeNotification.setEnabled(true);
            btnSetCancelDate.setText(R.string.faw_close);
            tvDateNotification.setText(dateNotification.toString(TimePreference.DD_MM_YYYY));
        }
        tvTimeNotification.setText(dateNotification.toString(TimePreference.HH_MM));
    }

    private long getTaskId(Bundle savedInstanceState, Intent intent) {
        long taskId;
        if (savedInstanceState != null) {
            taskId = savedInstanceState.getLong(TASK_ID);
        } else {
            taskId = intent.getLongExtra(TASK_ID, EMPTY);
        }
        return taskId;
    }


    private void initBtnSetCancelListener() {
        btnSetCancelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id;
                dateNotificationSet = !dateNotificationSet;
                if (dateNotificationSet) {
                    id = R.string.faw_close;
                    tvDateNotification.setText(dateNotification.toString(TimePreference.DD_MM_YYYY));
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

    private void initTvTimeNotificationListener() {
        tvTimeNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(TaskActivity.this, dateNotification.getHourOfDay(), dateNotification.getMinuteOfHour(), true);
                timePickerDialog.show(getFragmentManager(), "TimePickerDialog");
            }
        });
    }

    private void initTvDateNotificationListener() {
        tvDateNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                period = (Period) periodSpinner.getSelectedItem();
                final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(TaskActivity.this, dateNotification.getYear(), dateNotification.getMonthOfYear() - 1, dateNotification.getDayOfMonth());
                DateTime dateStart = period.getDateStart();
                Calendar startDate = dateStart.toCalendar(locale);
                Calendar endDate = dateStart.plusDays(period.getInterval() - 1).toCalendar(locale);
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
                spinnerPeriodArrayAdapter.addAll(spinnerPlanArrayAdapter.getItem(selectedItemPosition).getPeriods().values());
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
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getBaseContext());
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
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.input_task_name, Toast.LENGTH_SHORT).show();
                }
            }

            private void editTask(DatabaseHelper databaseHelper, String taskName, boolean disposable, DateTime dateTimeNotification) {
                ContentValues contentValues = new ContentValues();
                Plan previousPlan = task.getPlan();
                Period previousPeriod = task.getPeriod();
                final long millis = dateTimeNotification.getMillis();
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
                contentValues.put(Task.DATE_NOTIFICATION, millis);
                contentValues.put(Task.NAME, taskName);
                contentValues.put(Task.DISPOSABLE, disposable);
                task.setDateNotification(dateTimeNotification);
                task.setName(taskName);
                task.setDisposable(disposable);
                databaseHelper.updateTask(task.getId(), contentValues);
                databaseHelper.close();
                checkNotification(millis);
            }

            private void createTask(DatabaseHelper databaseHelper, String taskName, boolean disposable, DateTime dateTimeNotification) {
                int orderNum = databaseHelper.getTaskMaxOrder() + 1;
                DateTime dateCreated = DateTime.now();
                long id = databaseHelper.createTask(taskName, dateCreated, dateTimeNotification, orderNum, plan.getId(), period.getId(), disposable ? 1 : 0, 4);

                Task task = new Task(id, taskName, dateCreated, dateTimeNotification, orderNum, plan, period, false, disposable, 4);
                databaseHelper.close();
                Task.addTask(task);
                plan.addTask(task);
                period.addTask(task);
                Log.d(Constants.LOG_TAG, "" + Task.getTasks().containsKey(id));
                checkNotification(dateTimeNotification.getMillis());
            }

            private void checkNotification(long dateTime) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (prefs.getBoolean(getString(R.string.notifications_on), true)) {
                    if (dateTime != 0 && DateUtils.isToday(dateTime) &&
                            new DateTime(dateTime).isAfter(DateTime.now())) {
                        MyService.scheduleNotificationCheck(getApplicationContext(), dateTime);
                    }
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
        dateNotification = dateNotification.withDate(i, i1 + 1, i2);
        if (!dateNotificationSet) {
            dateNotificationSet = true;
            btnSetCancelDate.setText(getString(R.string.faw_close));
            tvTimeNotification.setEnabled(true);
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i1) {
        dateNotification = dateNotification.withHourOfDay(i).withMinuteOfHour(i1);
        tvTimeNotification.setText(String.format(locale, "%d:%02d", i, i1));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (task != null) {
            outState.putLong(TASK_ID, task.getId());
        }
        outState.putSerializable(DATE_NOTIFICATION, dateNotification);
        outState.putBoolean(DATE_NOTIFICATION_SET, dateNotificationSet);
        super.onSaveInstanceState(outState);
    }
}
