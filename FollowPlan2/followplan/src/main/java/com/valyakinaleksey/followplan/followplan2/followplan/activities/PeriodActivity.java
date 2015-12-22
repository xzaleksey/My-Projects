package com.valyakinaleksey.followplan.followplan2.followplan.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Locale;

import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.DATE_NOTIFICATION;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.PLAN_ID;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.RESULT_CREATE;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.RESULT_EDIT;

public class PeriodActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    public static final String PERIOD_ID = "periodId";
    public static final int EMPTY = 0;
    private Plan plan;
    private Period period;
    private TextView tvDateStart;
    private EditText etPeriodDays;
    private DateTime dateStart;
    private Locale locale;
    private int interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);
        tvDateStart = (TextView) findViewById(R.id.tv_date_start);
        etPeriodDays = (EditText) findViewById(R.id.et_period_days);
        locale = getResources().getConfiguration().locale;
        initPlanAndPeriod(savedInstanceState);
        initDateStart(savedInstanceState);
        initTvDateStartListener();
        initToolBar();
    }

    private void initToolBar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        ((TextView) findViewById(R.id.title)).setText(period == null ? R.string.create_period : R.string.edit_period);
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

    //Todo Notifications dateNotification add
    private void initBtnSendListener() {
        findViewById(R.id.tool_bar_iv_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInput(etPeriodDays.getText().toString())) {
                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getBaseContext());
                    DateTime dateEnd = dateStart.plusDays(interval).withHourOfDay(0).withMinuteOfHour(0);
                    final String name = "Every " + interval + " days";
                    if (period == null) {
                        long id = databaseHelper.createPeriod(name, interval, plan.getId(), dateStart, dateEnd, null);
                        Period period = new Period(id, name, plan, interval, dateStart, dateEnd, null);
                        Period.addPeriod(period);
                        plan.addPeriod(period);
                        setResult(RESULT_CREATE);
                    } else {
                        ContentValues contentValues = new ContentValues();
                        if (interval != period.getInterval()) {
                            contentValues.put(Period.NAME, name);
                            contentValues.put(Period.INTERVAL, interval);
                            period.setInterval(interval);
                            period.setName(name);
                        }
                        if (dateStart != period.getDateStart()) {
                            contentValues.put(Period.DATE_START, dateStart.getMillis());
                            period.setDateStart(dateStart);
                        }
                        if (dateEnd != period.getDateEnd()) {
                            contentValues.put(Period.DATE_END, dateEnd.getMillis());
                            period.setDateEnd(dateEnd);
                        }
                        databaseHelper.updatePeriod(period.getId(), contentValues);
                        setResult(RESULT_EDIT);
                    }
                    databaseHelper.close();
                    finish();
                }
            }
        });
    }

    private void initDateStart(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (period == null) {
                dateStart = DateTime.now();
            } else {
                dateStart = period.getDateStart();
            }
        } else {
            dateStart = (DateTime) savedInstanceState.getSerializable(DATE_NOTIFICATION);
        }
        tvDateStart.setText(dateStart.toString(TimePreference.DD_MM_YYYY));
    }

    private void initPlanAndPeriod(Bundle savedInstanceState) {
        long planId;
        long periodId;
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            planId = intent.getLongExtra(PLAN_ID, EMPTY);
            periodId = intent.getLongExtra(PERIOD_ID, EMPTY);
        } else {
            planId = savedInstanceState.getLong(PLAN_ID);
            periodId = savedInstanceState.getLong(PERIOD_ID, EMPTY);
        }
        plan = Plan.getPlans().get(planId);
        if (periodId != EMPTY) {
            period = Period.getPeriods().get(periodId);
            etPeriodDays.setText("" + period.getInterval());
            etPeriodDays.setSelection(etPeriodDays.getText().length());
        }
    }

    private void initTvDateStartListener() {
        tvDateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInput(etPeriodDays.getText().toString())) {
                    final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(PeriodActivity.this, dateStart.getYear(), dateStart.getMonthOfYear() - 1, dateStart.getDayOfMonth());
                    Calendar startDate = DateTime.now().minusDays(interval - 1).toCalendar(locale);
                    datePickerDialog.setMinDate(startDate);
                    datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
                }
            }
        });
    }

    private boolean checkInput(String s) {
        String result = "";
        if (!s.equals("")) {
            interval = Integer.parseInt(s);
            if (interval == 0) {
                result = getString(R.string.period_number_of_days_not_zero);
            }
        } else {
            result = getString(R.string.input_number_of_days);
        }
        if (result.equals("")) return true;
        Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(PLAN_ID, plan.getId());
        outState.putSerializable(DATE_NOTIFICATION, dateStart);
        if (period != null) {
            outState.putLong(PERIOD_ID, period.getId());
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
        tvDateStart.setText(String.format("%02d.%02d.%d", i2, i1 + 1, i));
        dateStart = dateStart.withDate(i, i1 + 1, i2);
    }
}
