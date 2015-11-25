package com.valyakinaleksey.followplan.followplan2.followplan;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Task;
import com.valyakinaleksey.followplan.followplan2.followplan.notifications.Notifications;
import com.valyakinaleksey.followplan.followplan2.followplan.update.BootReceiver;
import com.valyakinaleksey.followplan.followplan2.followplan.update.MainReceiver;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.*;

import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.LOG_TAG;
import static com.valyakinaleksey.followplan.followplan2.followplan.update.MainReceiver.*;

public class MyService extends Service {
    public static final String NOTIFICATION_TIME = "time";
    private NotificationManager nm;
    private AlarmManager alarmManager;

    @Override

    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(
                Context.ALARM_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int type = intent.getIntExtra(TYPE, ID_ACTION_UPDATE);
        Log.d(LOG_TAG, "MyService onStart");
        DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
        switch (type) {
            case ID_ACTION_UPDATE:
                Log.d(LOG_TAG, "action_update");
                databaseHelper.deleteAllNotifications();
                update(databaseHelper);
                break;
            case ID_ACTION_NOTIFICATION:
                Log.d(LOG_TAG, "action_notify");
                long notificationTime = intent.getLongExtra(NOTIFICATION_TIME, 0);
                startNotification(notificationTime);
                break;
            case BootReceiver.ID_ACTION_BOOT:
                Log.d(LOG_TAG, "action_boot");
                update(databaseHelper);
                reScheduleNotifications();
                break;
        }
        databaseHelper.close();
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private void reScheduleNotifications() {
        Set<DateTime> timesOfNotifications = new HashSet<>();
        for (Task task : Task.getTasks().values()) {
            final long millis = task.getDateNotification().getMillis();
            if (DateUtils.isToday(millis)) {
                timesOfNotifications.add(task.getDateNotification());
            }
        }
        scheduleTodayNotificationTimes(timesOfNotifications);
    }
//
//    private void resetTodayNotifications() {
//        Log.d(LOG_TAG, "resetTodayNotifications");
//        List<Long> times = Notifications.getDateNotifications(getBaseContext());
//        Set<DateTime> dateTimes = new HashSet<>();
//        for (Long time : times) {
//            dateTimes.add(new DateTime(time));
//        }
//        scheduleTodayNotificationTimes(dateTimes);
//    }

    private void update(DatabaseHelper databaseHelper) {
        scheduleUpdate();
        if (Plan.getPlans().size() == 0) {
            Log.d(LOG_TAG, "initFromDb");
            databaseHelper.initFromDb();
        }
        List<Period> periodsToUpdate = initPeriods();
        Log.d(LOG_TAG, "Количество периодов для обновления " + periodsToUpdate.size());
        Set<DateTime> timesOfNotifications = new HashSet<>();
        Set<Plan> plans = new HashSet<>();
        for (Period period : periodsToUpdate) {
            Plan plan = period.getPlan();
            plans.add(plan);
            final Collection<Task> tasks = period.getTasks().values();
//            int overDueTasks = tasks.size() - period.getTasksDoneCount(); //overdue count
            plan.setCurrentDoneTasksCount(plan.getCurrentDoneTasksCount() - period.getTasksDoneCount());
            plan.setCurrentTasksCount(plan.getCurrentTasksCount() - tasks.size());
            updateNotificationDates(databaseHelper, period, tasks);
            Log.d(LOG_TAG, "Check tasks for today");
            fillTimesOfNotifications(timesOfNotifications);
            final int tasksSize = period.getTasks().size();
            plan.setCurrentTasksCount(plan.getCurrentTasksCount() + tasksSize);
            plan.setTotalTasksCount(plan.getTotalTasksCount() + tasksSize);
            databaseHelper.setPeriodTasksDone(period.getId(), false);
        }
        for (Plan plan : plans) {
            plan.updatePlanTasksCount(databaseHelper);
        }
        scheduleTodayNotificationTimes(timesOfNotifications); //Запланировать на сегодня Notification Check
        updateApp();
    }

    private void updateNotificationDates(DatabaseHelper databaseHelper, Period period, Collection<Task> tasks) {
        DateTime start = period.getDateStart();
        DateTime end = DateTime.now();
        int diff = Days.daysBetween(start.toLocalDate(), end.toLocalDate()).getDays() / period.getInterval();
        period.updateDates(databaseHelper, diff);
        updateTasks(databaseHelper, tasks, diff);
    }

    private void updateApp() {
        Intent intent = new Intent(getBaseContext(), MainReceiver.class);
        intent.putExtra(MainReceiver.TYPE, MainReceiver.ID_ACTION_INIT_DB);
        sendBroadcast(intent);
    }

    private void updateTasks(DatabaseHelper databaseHelper, Collection<Task> tasks, int diff) {
        for (Task task : tasks) {
            if (task.isDisposable() && task.isDone()) {
                Task.deleteTask(databaseHelper, task);
                continue;
            }
            if (task.isDone()) {
                task.setDone(false);
            }
            if (task.getDateNotification().getMillis() != 0) {  // update task notification
                task.updateDateNotification(databaseHelper, diff);
                Log.d(LOG_TAG, "updated task Notification " + task.getName());
            }
        }
    }

    private void fillTimesOfNotifications(Set<DateTime> timesOfNotifications) {
        for (Task task : Task.getTasks().values()) {
            if (DateUtils.isToday(task.getDateNotification().getMillis())) {
                Log.d(LOG_TAG, "Notification today" + task.getName());
                Log.d(LOG_TAG, "" + DateUtils.isToday(task.getDateNotification().getMillis()));
                timesOfNotifications.add(task.getDateNotification());
            }
        }
    }

    private void scheduleTodayNotificationTimes(Set<DateTime> times) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
        List<Long> dateTimes = Notifications.getDateNotifications(getBaseContext());
        for (DateTime time : times) {
            final long millis = time.getMillis();
            if (!dateTimes.contains(millis)) {
                Log.d(LOG_TAG, new DateTime(time).toString());
                scheduleNotificationCheck(millis);
            }
        }
        databaseHelper.close();
    }

    private void startNotification(long notificationTime) {
        Log.d(LOG_TAG, "startNotification");
        DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
        if (databaseHelper.checkExistingNotifications(notificationTime)) {
            return;
        }
        Cursor cursor = databaseHelper.getAllTasksByDateNotification(notificationTime);
        List<TaskShort> tasks = new ArrayList<>();
        while (cursor.moveToNext()) {
            tasks.add(new TaskShort(cursor.getString(cursor.getColumnIndex("tName")),
                    cursor.getString(cursor.getColumnIndex("planName")),
                    cursor.getString(cursor.getColumnIndex("periodName"))));
        }
        cursor.close();
        Log.d(LOG_TAG, "Tasks size" + tasks.size());
        if (!tasks.isEmpty()) {
            Log.d(LOG_TAG, "Notifying");
            int id = (int) System.currentTimeMillis();
            Notification notification = Notifications.getNotification(getBaseContext(), id, TaskShort.getTitle(tasks, getBaseContext()), TaskShort.getContent(tasks));
            nm.notify(id, notification);
        }
        databaseHelper.createNotification(notificationTime);
        databaseHelper.close();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "destroy service");
        super.onDestroy();
    }

    private List<Period> initPeriods() {
        List<Period> periods = new ArrayList<>();
        DateTime currentDate = DateTime.now();
        for (Period period : Period.getPeriods().values()) {
            if (currentDate.isAfter(period.getDateEnd())) {
                periods.add(period);
            }
        }
        return periods;
    }

    private void scheduleNotificationCheck(long millis) {
        Intent intent = new Intent(getBaseContext(), MainReceiver.class);
        intent.putExtra(TYPE, ID_ACTION_NOTIFICATION);
        intent.putExtra(NOTIFICATION_TIME, millis);
        setAlarm(millis, intent, (int) System.currentTimeMillis());
        Log.d(LOG_TAG, "Notification scheduled" + new DateTime(millis));
    }

    public static void scheduleNotificationCheck(Context context, long millis) {
        Intent intent = new Intent(context, MainReceiver.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);
        intent.putExtra(TYPE, ID_ACTION_NOTIFICATION);
//        new DateTime(millis).minusHours(1).getMillis()
        intent.putExtra(NOTIFICATION_TIME, millis);
        intent.setAction(MainReceiver.BROADCAST_ACTION);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pIntent);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pIntent);
//        Log.d(LOG_TAG, "Notification scheduled" + new DateTime(millis).minusHours(1));
        Log.d(LOG_TAG, "" + new DateTime(System.currentTimeMillis()));

    }

    private void scheduleUpdate() {
        final long millis = DateTime.now().plusDays(1).withHourOfDay(0).withMinuteOfHour(1).getMillis();
        Log.d(LOG_TAG, new DateTime(millis).toString());
        Intent intent = new Intent(getBaseContext(), MainReceiver.class);
        setAlarm(millis, intent, ID_ACTION_UPDATE);
        Log.d(LOG_TAG, "MainReceiver scheduled");
    }

    private void setAlarm(long millis, Intent intent, int requestCode) {
        intent.setAction(MainReceiver.BROADCAST_ACTION);
        PendingIntent pIntent = PendingIntent.getBroadcast(getBaseContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pIntent);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    private static class TaskShort {
        String taskName;
        String planName;
        String periodName;

        public TaskShort(String taskName, String planName, String periodName) {
            this.taskName = taskName;
            this.planName = planName;
            this.periodName = periodName;
        }

        static String getTitle(List<TaskShort> tasks, Context context) {
            return context.getString(R.string.notification_title_part_1) + " " + tasks.size() + " " + context.getString(tasks.size() == 1 ? R.string.notification_title_part_2_single :
                    R.string.notification_title_part_2_plural);
        }

        static String getContent(List<TaskShort> tasks) {
            StringBuilder stringBuilder = new StringBuilder();
            for (TaskShort task : tasks) {
                Log.d(LOG_TAG, "" + task);
                stringBuilder.append(task.planName)
                        .append(" ")
                        .append(task.periodName)
                        .append(" ")
                        .append(task.taskName)
                        .append("\n");
            }
            return stringBuilder.toString();
        }
    }
}
