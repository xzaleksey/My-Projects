package com.valyakinaleksey.followplan.followplan2.followplan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Task;
import org.joda.time.DateTime;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {
    public static final String TABLE_PLANS = "plans";
    public static final String TABLE_PERIODS = "periods";
    public static final String TABLE_TASKS = "tasks";
    public static final String NAME = "name";
    public static final String SELECT_MAX = "SELECT MAX";
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;
    private static final String PLANS_CREATE_SCRIPT = "create table "
            + TABLE_PLANS + " (" + _ID
            + " integer primary key autoincrement, " + NAME
            + " text not null, " + Plan.DATE_CREATED + " integer, " + Plan.ORDER_NUM + " integer, " + Plan.COLOR
            + " integer, " + Plan.CURRENT_TASKS_COUNT + " integer DEFAULT 0, " + Plan.CURRENT_DONE_TASKS_COUNT + " integer DEFAULT 0, " + Plan.TOTAL_TASKS_COUNT + " integer DEFAULT 0, "
            + Plan.TOTAL_DONE_TASKS_COUNT + " integer DEFAULT 0);";
    private static final String PERIODS_CREATE_SCRIPT = "create table "
            + TABLE_PERIODS + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + NAME
            + " text not null, " + Period.DATE_START + " integer, " + Period.DATE_END + " integer, " + Period.DATE_NOTIFICATION + " integer, " + Period.INTERVAL
            + " integer, " + Period.PLAN + " integer DEFAULT 0);";
    private static final String TASKS_CREATE_SCRIPT = "create table "
            + TABLE_TASKS + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + NAME
            + " text not null, " + Task.DATE_CREATED + " integer, " + Task.DATE_NOTIFICATION + " integer, " + Task.ORDER_NUM
            + " integer, " + Task.PLAN + " integer, " + Task.PERIOD + " integer, " + Task.DONE + " integer DEFAULT 0, "
            + Task.DISPOSABLE + " integer DEFAULT 0);";
    private Context c;
    private SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        c = context;
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    public static long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
    }

    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PLANS_CREATE_SCRIPT);
        db.execSQL(PERIODS_CREATE_SCRIPT);
        db.execSQL(TASKS_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);
        db.execSQL("DROP TABLE IF IT EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF IT EXISTS " + TABLE_PERIODS);
        db.execSQL("DROP TABLE IF IT EXISTS " + TABLE_PLANS);
        onCreate(db);
    }

    public long createPlan(String name, int order, int color) {
        ContentValues initialValues = createPlanContentValues(name, order, color);
        return createRow(TABLE_PLANS, initialValues);
    }

    public long createPeriod(String name, int interval, long planId, DateTime dateStart, DateTime dateEnd, DateTime dateNotification) {
        ContentValues initialValues = createPeriodContentValues(name, interval, planId, dateStart, dateEnd, dateNotification);
        return createRow(TABLE_PERIODS, initialValues);
    }

    public long createTask(String name, DateTime dateCreated, DateTime dateNotification, int order, long planId, long periodId, int disposable) {
        ContentValues initialValues = createTaskContentValues(name, dateCreated, dateNotification, order, planId, periodId, disposable);
        return createRow(TABLE_TASKS, initialValues);
    }

    private ContentValues createTaskContentValues(String name, DateTime dateCreated, DateTime dateNotification, int order, long planId, long periodId, int disposable) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Task.NAME, name);
        contentValues.put(Task.DATE_CREATED, dateCreated.getMillis());
        if (dateNotification == null) {
            contentValues.put(Task.DATE_NOTIFICATION, 0);
        } else {
            contentValues.put(Task.DATE_NOTIFICATION, dateNotification.getMillis());
        }
        contentValues.put(Task.ORDER_NUM, order);
        contentValues.put(Task.PLAN, planId);
        contentValues.put(Task.PERIOD, periodId);
        contentValues.put(Task.DISPOSABLE, disposable);
        contentValues.put(Task.DONE, 0);
        return contentValues;
    }

    private long createRow(String tableName, ContentValues initialValues) {
        db = this.getWritableDatabase();
        long row = db.insert(tableName, null, initialValues);
        db.close();
        return row;
    }

    public Cursor getPlan(long rowId) {
        return getRow(TABLE_PLANS, rowId);
    }

    public Cursor getRow(String tableNmae, long rowId) {
        return db.query(true, tableNmae,
                null, _ID + "=" + rowId, null,
                null, null, null, null);
    }

    public Cursor getPeriodsByPlanId(long planId) {
        return db.query(true, TABLE_PERIODS,
                null, Period.PLAN + "=" + planId, null,
                null, null, null, null);
    }

    public int updatePlan(long rowId, ContentValues contentValues) {
        return updateRow(TABLE_PLANS, rowId, contentValues);
    }

    public int updateTask(long rowId, ContentValues contentValues) {
        return updateRow(TABLE_TASKS, rowId, contentValues);
    }

    private int updateRow(String tableName, long rowId, ContentValues contentValues) {
        db = this.getWritableDatabase();
        return db.update(tableName, contentValues, _ID + " = ?",
                new String[]{String.valueOf(rowId)});
    }

    public int getProjectsCount() {
        return getRowCount(TABLE_PLANS);
    }

    private int getRowCount(String tableName) {
        String countQuery = "SELECT  * FROM " + tableName;
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getTaskMaxOrder() {
        return getMaxOrder(TABLE_TASKS);
    }

    public int getPlanMaxOrder() {
        return getMaxOrder(TABLE_PLANS);
    }

    private int getMaxOrder(String tableName) {
        db = getReadableDatabase();
        Cursor cursor = db.query(tableName, new String[]{"MAX(" + Plan.ORDER_NUM + ")"},
                null, null, null, null, null);
        int order = 0;
        if (cursor.moveToFirst()) {
            order = cursor.getInt(0);
        }
        cursor.close();
        return order;
    }

    public void setTaskDone(long id, boolean done) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Task.DONE, done ? 1 : 0);
        updateRow(TABLE_TASKS, id, contentValues);
    }

    private int getMaxPlanId() {
        db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANS, null,
                _ID + "=(" + SELECT_MAX + "(" + _ID + "))", null, null, null, null);
        int maxId = -1;
        if (cursor.moveToFirst()) {
            maxId = cursor.getInt(0);
        }
        cursor.close();
        return maxId;
    }

    public void deletePlan(long rowId) {
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_PERIODS, Period.PLAN + "=" + rowId, null);
            db.delete(TABLE_TASKS, Task.PLAN + "=" + rowId, null);
            deleteRow(TABLE_PLANS, rowId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteTask(long rowId) {
        deleteRow(TABLE_TASKS, rowId);
    }

    public void deletePeriod(long rowId) {
        deleteRow(TABLE_PERIODS, rowId);
    }

    private void deleteRow(String table, long rowId) {
        db = this.getWritableDatabase();
        db.delete(table, _ID + "=" + rowId, null);
    }

    public Cursor getAllPlans() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PLANS, null, null,
                null, null, null, null);
    }

    public Cursor getAllPeriods() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PERIODS, null, null,
                null, null, null, null);
    }

    public Cursor getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TASKS, null, null,
                null, null, null, null);
    }


    private ContentValues createPlanContentValues(String name, int order, int color) {
        ContentValues values = new ContentValues();
        values.put(Plan.NAME, name);
        values.put(Plan.ORDER_NUM, order);
        values.put(Plan.COLOR, color);
        values.put(Plan.DATE_CREATED, System.currentTimeMillis());
        return values;
    }

    public ContentValues createPeriodContentValues(String name, int interval, long projectId, DateTime dateStart, DateTime dateEnd, DateTime dateNotification) {
        ContentValues values = new ContentValues();
        values.put(Period.NAME, name);
        values.put(Period.DATE_START, dateStart.getMillis());
        values.put(Period.INTERVAL, interval);
        values.put(Period.DATE_END, dateEnd.getMillis());
        values.put(Period.DATE_NOTIFICATION, dateNotification.getMillis());
        values.put(Period.PLAN, projectId);
        return values;
    }

}