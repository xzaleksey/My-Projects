package com.valyakinaleksey.followplan.followplan2.followplan.task;

import android.database.Cursor;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.Map;

public class Task {
    public static final String NAME = "name";
    public static final String DATE_CREATED = "date_created";
    public static final String DATE_NOTIFICATION = "date_notification";
    public static final String ORDER_NUM = "order_num";
    public static final String PLAN = "plan";
    public static final String PERIOD = "period";
    public static final String DONE = "done";
    public static final String DISPOSABLE = "disposable";
    private static Task lastTask;
    private static Map<Long, Task> tasks = new LinkedHashMap<>();
    private final long id;
    private String name;
    private DateTime dateCreated;
    private DateTime dateNotification;
    private int order;
    private Plan plan;
    private Period period;
    private boolean done;
    private boolean disposable;

    public Task(long id, String name, DateTime dateCreated, int order, Plan plan, Period period) {
        this(id, name, dateCreated, null, order, plan, period, false, false);
    }

    public Task(long id, String name, DateTime dateCreated, DateTime dateNotification, int order, Plan plan, Period period, boolean done, boolean disposable) {
        this.name = name;
        this.id = id;
        this.dateCreated = dateCreated;
        this.dateNotification = dateNotification;
        this.order = order;
        this.plan = plan;
        this.period = period;
        this.done = done;
        this.disposable = disposable;
    }

    public static void addTask(Task task) {
        tasks.put(task.getId(), task);
        lastTask = task;
    }

    public static Map<Long, Task> getTasks() {
        return tasks;
    }

    public static void initTasks(DatabaseHelper databaseHelper) {
        Cursor cursor = databaseHelper.getAllTasks();
        while (cursor.moveToNext()) {
            long id = DatabaseHelper.getId(cursor);
            tasks.put(id, new Task(id, cursor.getString(cursor.getColumnIndex(Task.NAME)),
                            new DateTime(cursor.getLong(cursor.getColumnIndex(DATE_CREATED))),
                            new DateTime(cursor.getLong(cursor.getColumnIndex(DATE_NOTIFICATION))),
                            cursor.getInt(cursor.getColumnIndex(ORDER_NUM)),
                            Plan.getPlans().get(cursor.getLong(cursor.getColumnIndex(PLAN))),
                            Period.getPeriods().get(cursor.getLong(cursor.getColumnIndex(PERIOD))),
                            cursor.getInt(cursor.getColumnIndex(DONE)) != 0,
                            cursor.getInt(cursor.getColumnIndex(DISPOSABLE)) != 0)
            );
        }
        cursor.close();
        databaseHelper.close();
    }

    public static Task getLastTask() {
        return lastTask;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(DateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getId() {
        return id;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public boolean isDisposable() {
        return disposable;
    }

    public void setDisposable(boolean disposable) {
        this.disposable = disposable;
    }

    public DateTime getDateNotification() {
        return dateNotification;
    }

    public void setDateNotification(DateTime dateNotification) {
        this.dateNotification = dateNotification;
    }
}
