package com.valyakinaleksey.followplan.followplan2.followplan.task;

import android.database.Cursor;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Plan {
    public static final String NAME = "name";
    public static final String DATE_CREATED = "date_created";
    public static final String ORDER_NUM = "order_num";
    public static final String COLOR = "color";
    public static final String CURRENT_TASKS_COUNT = "current_tasks_count";
    public static final String CURRENT_DONE_TASKS_COUNT = "current_done_tasks_count";
    public static final String TOTAL_TASKS_COUNT = "total_tasks_count";
    public static final String TOTAL_DONE_TASKS_COUNT = "total_done_tasks_count";

    private static Map<Long, Plan> plans = new LinkedHashMap<>();
    private static Plan lastPlan;

    private final long id;
    private String name;
    private Date dateCreated;
    private int orderNum;
    private int color;
    private int currentTasksCount;
    private int currentDoneTasksCount;
    private int totalTasksCount;
    private int totalDoneTasksCount;

    private Map<Long, Period> planPeriods = new LinkedHashMap<>();
    private Map<Long, Task> tasks = new LinkedHashMap<>();

    public Plan(long id, String name) {
        this.id = id;
        this.name = name;
    }


    public Plan(long id, String name, int orderNum, int color) {
        this(id, name);
        this.orderNum = orderNum;
        this.color = color;
    }

    public static Map<Long, Plan> getPlans() {
        return plans;
    }

    public static void initPlans(DatabaseHelper databaseHelper) {
        Cursor cursor = databaseHelper.getAllPlans();
        databaseHelper.setDb(databaseHelper.getReadableDatabase());
        while (cursor.moveToNext()) {
            long id = DatabaseHelper.getId(cursor);
            Plan plan = new Plan(id, cursor.getString(cursor.getColumnIndex(Plan.NAME)), cursor.getInt(cursor.getColumnIndex(Plan.ORDER_NUM)), cursor.getInt(cursor.getColumnIndex(Plan.COLOR)));
            Plan.addPlan(plan);
//            Cursor periodsCursor = databaseHelper.getPeriodsByPlanId(id);
//            while (periodsCursor.moveToNext()) {
//                long periodId = DatabaseHelper.getId(periodsCursor);
//                plan.planPeriods.put(periodId, Period.getPeriods().get(periodId));
//            }
        }
        cursor.close();
        databaseHelper.close();
    }


    public static void addPlan(Plan plan) {
        plans.put(plan.getId(), plan);
        lastPlan = plan;
    }

    public static Plan getLastPlan() {
        return lastPlan;
    }

    public static void deletePlan(long planId, DatabaseHelper databaseHelper) {
        databaseHelper.deletePlan(planId);
        databaseHelper.close();
        lastPlan = plans.remove(planId);
    }

    public static void fillPeriods() {
        for (Map.Entry<Long, Period> taskEntry : Period.getPeriods().entrySet()) {
            Period period = taskEntry.getValue();
            plans.get(period.getPlan().getId()).planPeriods.put(taskEntry.getKey(), period);
        }
    }

    public static void fillTasks() {
        for (Map.Entry<Long, Task> taskEntry : Task.getTasks().entrySet()) {
            Task task = taskEntry.getValue();
            plans.get(task.getPlan().getId()).tasks.put(taskEntry.getKey(), task);
        }
    }

    public static int getPlanCount() {
        return plans.size();
    }

    public static Plan getPlanByIndex(int index) {
        return (Plan) plans.values().toArray()[index];
    }

    public static int getIndexByPlan(Plan plan) {
        return new ArrayList<>(plans.values()).indexOf(plan);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getCurrentTasksCount() {
        return currentTasksCount;
    }

    public void setCurrentTasksCount(int currentTasksCount) {
        this.currentTasksCount = currentTasksCount;
    }

    public int getCurrentDoneTasksCount() {
        return currentDoneTasksCount;
    }

    public void setCurrentDoneTasksCount(int currentDoneTasksCount) {
        this.currentDoneTasksCount = currentDoneTasksCount;
    }

    public int getTotalTasksCount() {
        return totalTasksCount;
    }

    public void setTotalTasksCount(int totalTasksCount) {
        this.totalTasksCount = totalTasksCount;
    }

    public int getTotalDoneTasksCount() {
        return totalDoneTasksCount;
    }

    public void setTotalDoneTasksCount(int totalDoneTasksCount) {
        this.totalDoneTasksCount = totalDoneTasksCount;
    }

    public long getId() {
        return id;
    }

    public Map<Long, Period> getPlanPeriods() {
        return planPeriods;
    }

    public Map<Long, Task> getTasks() {
        return tasks;
    }

    public Period getPeriodByIndex(int index) {
        return (Period) planPeriods.values().toArray()[index];
    }

    @Override
    public String toString() {
        return getName();
    }

    public int getIndexByPeriod(Period period) {
        return new ArrayList<>(planPeriods.values()).indexOf(period);
    }
}
