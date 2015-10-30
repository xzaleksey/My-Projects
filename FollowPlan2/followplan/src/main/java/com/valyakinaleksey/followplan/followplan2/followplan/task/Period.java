package com.valyakinaleksey.followplan.followplan2.followplan.task;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.Map;

public class Period {
    public static final String NAME = "name";
    public static final String DATE_START = "date_start";
    public static final String DATE_END = "date_end";
    public static final String DATE_NOTIFICATION = "date_notification";
    public static final String PLAN = "plan";
    public static final String INTERVAL = "interval";
    private static Map<Long, Period> periods = new LinkedHashMap<>();
    private final long id;
    private String name;
    private DateTime dateStart;
    private DateTime dateEnd;
    private int interval;
    private DateTime dateNotification;
    private Plan plan;
    private Map<Long, Task> tasks = new LinkedHashMap<>();

    public Period(long id, String name, Plan plan, int interval) {
        this.id = id;
        this.name = name;
        this.plan = plan;
        this.interval = interval;
    }

    public Period(long id, String name, Plan plan, int interval, DateTime dateStart) {
        this.id = id;
        this.name = name;
        this.plan = plan;
        this.interval = interval;
        this.dateStart = dateStart;
    }

    public Period(long id, String name, Plan plan, int interval, DateTime dateStart, DateTime dateEnd, DateTime dateNotification) {
        this.id = id;
        this.name = name;
        this.plan = plan;
        this.interval = interval;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.dateNotification = dateNotification;
    }

    public static void addPeriod(Period period) {
        periods.put(period.getId(), period);
    }

    public static Map<Long, Period> getPeriods() {
        return periods;
    }

    public static void initPeriods(DatabaseHelper databaseHelper) {
        Cursor cursor = databaseHelper.getAllPeriods();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            periods.put(id, new Period(id, cursor.getString(cursor.getColumnIndex(Period.NAME)),
                    Plan.getPlans().get(cursor.getLong(cursor.getColumnIndex(PLAN))),
                    cursor.getInt(cursor.getColumnIndex(INTERVAL)),
                    new DateTime(cursor.getLong(cursor.getColumnIndex(DATE_START))),
                    new DateTime(cursor.getLong(cursor.getColumnIndex(DATE_END))),
                    new DateTime(cursor.getLong(cursor.getColumnIndex(DATE_NOTIFICATION)))
            ));
        }
        cursor.close();
        databaseHelper.close();
    }

    public static void createBasePeriods(Context context, Plan plan) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        long planId = plan.getId();
        int[] intervals = context.getResources().getIntArray(R.array.periods_base_int);
        String[] periodNames = context.getResources().getStringArray(R.array.periods_base);
        DateTime dateStart = new DateTime();
        long periodId;
        for (int i = 0; i < intervals.length; i++) {
            int interval = intervals[i];
            DateTime dateEnd = dateStart.withMinuteOfHour(0).withHourOfDay(0).plusDays(interval);
            DateTime dateNotification = dateEnd.minusHours(2);
            periodId = createBasePeriod(databaseHelper, planId, periodNames[i], interval, dateStart, dateEnd, dateNotification);
            Period period = new Period(periodId, periodNames[i], plan, intervals[i], dateStart, dateEnd, dateNotification);
            Period.addPeriod(period);
            plan.getPlanPeriods().put(periodId, period);
        }
        databaseHelper.close();
    }

    private static long createBasePeriod(DatabaseHelper databaseHelper, long planId, String periodName, int interval, DateTime dateStart, DateTime dateEnd, DateTime dateNotification) {
        return databaseHelper.createPeriod(periodName, interval, planId, dateStart, dateEnd, dateNotification);
    }

    public static void fillTasks() {
        for (Map.Entry<Long, Task> taskEntry : Task.getTasks().entrySet()) {
            Task task = taskEntry.getValue();
            periods.get(task.getPeriod().getId()).tasks.put(taskEntry.getKey(), task);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(DateTime dateStart) {
        this.dateStart = dateStart;
    }

    public DateTime getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(DateTime dateEnd) {
        this.dateEnd = dateEnd;
    }

    public DateTime getDateNotification() {
        return dateNotification;
    }

    public void setDateNotification(DateTime dateNotification) {
        this.dateNotification = dateNotification;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public long getId() {
        return id;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Map<Long, Task> getTasks() {
        return tasks;
    }
}
