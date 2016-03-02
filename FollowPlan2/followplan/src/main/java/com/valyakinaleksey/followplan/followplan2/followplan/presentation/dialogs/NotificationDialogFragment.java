package com.valyakinaleksey.followplan.followplan2.followplan.presentation.dialogs;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.IPositiveButtonDialogListener;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.data.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.domain.services.MyService;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.model.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.model.Task;
import com.valyakinaleksey.followplan.followplan2.followplan.util.Constants;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;
import java.util.Locale;

import static com.valyakinaleksey.followplan.followplan2.followplan.presentation.dialogs.TimePreference.DD_MM_YYYY;
import static com.valyakinaleksey.followplan.followplan2.followplan.presentation.dialogs.TimePreference.DEFAULT_NOTIFICATION_VALUE;
import static com.valyakinaleksey.followplan.followplan2.followplan.presentation.dialogs.TimePreference.HH_MM;

public class NotificationDialogFragment extends SimpleDialogFragment implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    public static final String DATE_NOTIFICATION = "dateNotification";
    public static final String DATE_NOTIFICATION_SET = "dateNotificationSet";
    private static final String TASK_ID = "taskId";
    public static String TAG = "NotificationDialogFragment";
    private Task task;
    private boolean dateNotificationSet = false;
    private TextView tvDateNotification;
    private TextView tvTimeNotification;
    private Locale locale;
    private Button btnSetCancelDate;
    private DateTime dateNotification;
    private Bundle savedInstanceState;

    public static void show(FragmentActivity activity, Fragment fragment, Task task) {
        NotificationDialogFragment notificationDialogFragment = new NotificationDialogFragment();
        notificationDialogFragment.task = task;
        notificationDialogFragment.show(activity.getSupportFragmentManager(), TAG);
        notificationDialogFragment.setTargetFragment(fragment, Constants.REQUEST_CODE_SET_NOTIFICATION);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        this.savedInstanceState = savedInstanceState;
        if (this.savedInstanceState != null) {
            long id = savedInstanceState.getLong(TASK_ID);
            if (id != 0) {
                task = Task.getTasks().get(id);
            }
        }
        locale = getResources().getConfiguration().locale;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_task_notification, null);
        tvDateNotification = (TextView) viewGroup.findViewById(R.id.tv_date_start);
        tvTimeNotification = (TextView) viewGroup.findViewById(R.id.tv_time_notification);
        btnSetCancelDate = (Button) viewGroup.findViewById(R.id.btn_set_cancel_date);

        builder.setTitle(task.getName())
                .setPositiveButton(R.string.set, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());
                        ContentValues contentValues = new ContentValues();
                        DateTime dateTimeNotification;
                        if (dateNotificationSet) {
                            dateTimeNotification = DateTime.parse(tvDateNotification.getText().toString() + tvTimeNotification.getText().toString(),
                                    DateTimeFormat.forPattern(DD_MM_YYYY + HH_MM).withLocale(locale));
                        } else {
                            dateTimeNotification = new DateTime(0);
                        }
                        contentValues.put(Task.DATE_NOTIFICATION, dateTimeNotification.getMillis());
                        task.setDateNotification(dateTimeNotification);
                        databaseHelper.updateTask(task.getId(), contentValues);
                        databaseHelper.close();
                        checkNotification(dateTimeNotification.getMillis());
                        for (IPositiveButtonDialogListener listener : getPositiveButtonDialogListeners()) {
                            listener.onPositiveButtonClicked(mRequestCode);
                        }
                        dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        }).setView(viewGroup);
        initDateTimeNotification();
        initTvDateNotificationListener();
        initTvTimeNotificationListener();
        initBtnSetCancelListener();
        return builder;
    }

    private void checkNotification(long dateTime) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean(getString(R.string.notifications_on), true)) {
            if (dateTime != 0 && DateUtils.isToday(dateTime) &&
                    new DateTime(dateTime).isAfter(DateTime.now())) {
                MyService.scheduleNotificationCheck(getContext(), dateTime);
            }
        }
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
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(NotificationDialogFragment.this, dateNotification.getHourOfDay(), dateNotification.getMinuteOfHour(), true);
                timePickerDialog.show(getActivity().getFragmentManager(), "TimePickerDialog");
            }
        });
    }

    private void initTvDateNotificationListener() {
        tvDateNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Period period = task.getPeriod();
                final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(NotificationDialogFragment.this, dateNotification.getYear(), dateNotification.getMonthOfYear() - 1, dateNotification.getDayOfMonth());
                DateTime dateStart = period.getDateStart();
                Calendar startDate = dateStart.toCalendar(locale);
                Calendar endDate = dateStart.plusDays(period.getInterval() - 1).toCalendar(locale);
                datePickerDialog.setMinDate(startDate);
                datePickerDialog.setMaxDate(endDate);
                datePickerDialog.show(getActivity().getFragmentManager(), "DatePickerDialog");
            }
        });
    }

    private void initDateTimeNotification() {
        if (savedInstanceState == null) {
            if (task != null && task.getDateNotification().getMillis() != 0) {
                dateNotificationSet = true;
                dateNotification = task.getDateNotification();
            } else {
                dateNotification = DateTime.now();
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
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
    public void onSaveInstanceState(Bundle outState) {
        if (task != null) {
            outState.putLong(TASK_ID, task.getId());
        }
        outState.putSerializable(DATE_NOTIFICATION, dateNotification);
        outState.putBoolean(DATE_NOTIFICATION_SET, dateNotificationSet);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onDismiss(dialog);
    }
}
