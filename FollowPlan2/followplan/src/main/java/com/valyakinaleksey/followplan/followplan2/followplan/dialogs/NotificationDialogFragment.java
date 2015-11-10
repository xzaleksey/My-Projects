package com.valyakinaleksey.followplan.followplan2.followplan.dialogs;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.IPositiveButtonDialogListener;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Task;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Locale;

import static com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference.*;

public class NotificationDialogFragment extends SimpleDialogFragment implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private static final String TASK_ID = "taskId";
    public static String TAG = "NotificationDialogFragment";
    private Task task;
    private boolean dateNotificationSet = false;
    private String stringDateNotification;
    private TextView tvDateNotification;
    private TextView tvTimeNotification;
    private Locale locale;
    private Button btnSetCancelDate;

    public static void show(FragmentActivity activity, Fragment fragment, Task task) {
        NotificationDialogFragment notificationDialogFragment = new NotificationDialogFragment();
        notificationDialogFragment.task = task;
        notificationDialogFragment.show(activity.getSupportFragmentManager(), TAG);
        notificationDialogFragment.setTargetFragment(fragment, Constants.REQUEST_CODE_SET_NOTIFICATION);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            long id = savedInstanceState.getLong(TASK_ID);
            if (id != 0) {
                task = Task.getTasks().get(id);
            }
        }
        locale = getResources().getConfiguration().locale;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (task != null) {
            outState.putLong(TASK_ID, task.getId());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_task_notification, null);
        DateTime dateNotification = task.getDateNotification();
        tvDateNotification = (TextView) viewGroup.findViewById(R.id.tv_date_notification);
        tvTimeNotification = (TextView) viewGroup.findViewById(R.id.tv_time_notification);
        btnSetCancelDate = (Button) viewGroup.findViewById(R.id.btn_set_cancel_date);


        if (dateNotification.getMillis() != 0) {
            dateNotificationSet = true;
            btnSetCancelDate.setText(R.string.faw_close);
            tvTimeNotification.setEnabled(true);
        } else {
            dateNotification = DateTime.now();
        }
        initNotificationValues(dateNotification);
        builder.setTitle(task.getName())
                .setPositiveButton(R.string.set, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
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

        return builder;
    }

    private void initNotificationValues(DateTime dateNotification) {
        stringDateNotification = dateNotification.toString(DD_MM_YYYY);
        if (dateNotificationSet) {
            tvDateNotification.setText(stringDateNotification);
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern(HH_MM)
                .withLocale(locale);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        DateTime timeNotification = formatter.parseDateTime(sp.getString(getString(R.string.time_notification), DEFAULT_NOTIFICATION_VALUE));
        tvTimeNotification.setText(timeNotification.toString(formatter));
        initTvDateNotificationListener(dateNotification);
        initTvTimeNotificationListener(timeNotification);
        initBtnSetCancelListener();
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
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(NotificationDialogFragment.this, time.getHourOfDay(), time.getMinuteOfHour(), true);
                timePickerDialog.show(getActivity().getFragmentManager(), "TimePickerDialog");
            }
        });
    }

    private void initTvDateNotificationListener(final DateTime dateTime) {
        tvDateNotification.setOnClickListener(new View.OnClickListener() {
            private Period period;

            @Override
            public void onClick(View view) {
                period = task.getPeriod();
                final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(NotificationDialogFragment.this, dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());
                Calendar startDate = Calendar.getInstance();
                Calendar endDate = dateTime.plusDays(period.getInterval() - 1).toCalendar(locale);
                datePickerDialog.setMinDate(startDate);
                datePickerDialog.setMaxDate(endDate);
                datePickerDialog.show(getActivity().getFragmentManager(), "DatePickerDialog");
            }
        });
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i1) {
        tvTimeNotification.setText(String.format(locale, "%d:%02d", i, i1));

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
}
