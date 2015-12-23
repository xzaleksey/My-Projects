package com.valyakinaleksey.timerwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimerWidget extends AppWidgetProvider {
    public static final String LOG_TAG = "MyTag";
    public static final String ACTION_UPDATE = "com.valyakinaleksey.timer_widget.UPDATE";
    public static final String ACTION_RESTART = "com.valyakinaleksey.timer_widget.RESTART";
    public static final String APP_WIDGET_ID = "appWidgetId";
    public static final String FORMAT = "0:%02d";
    public static final int DEFAULT_TIME = 60;
    public static final String DEFAULT_TEXT = "1:00";
    private static SparseBooleanArray STARTED_WIDGETS = new SparseBooleanArray();
    private static SparseIntArray TIME_WIDGETS = new SparseIntArray();
    private static Map<Integer, Thread> threadMap = new HashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private MyHandler myHandler;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            Log.d(LOG_TAG, "onUpdate AppWidgetId " + appWidgetId);
            STARTED_WIDGETS.put(appWidgetId, false);
            TIME_WIDGETS.put(appWidgetId, DEFAULT_TIME);
            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.play_pause, getPendingIntent(context, appWidgetId, getIntent(context, appWidgetId, ACTION_UPDATE)));
            views.setOnClickPendingIntent(R.id.restart, getPendingIntent(context, Integer.MAX_VALUE - appWidgetId, getIntent(context, appWidgetId, ACTION_RESTART)));
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @NonNull
    private Intent getIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, TimerWidget.class);
        intent.setAction(action);
        intent.putExtra(APP_WIDGET_ID, appWidgetId);
        return intent;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        final int appWidgetId = intent.getIntExtra(APP_WIDGET_ID, 0);
        Log.d(LOG_TAG, "AppWidgetId " + appWidgetId);
        String action = intent.getAction();
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        if (action.equals(ACTION_UPDATE)) {
            final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (appWidgetId != 0) {

                if (STARTED_WIDGETS.get(appWidgetId)) {
                    threadMap.put(appWidgetId, null);
                    Log.d(LOG_TAG, "Stopped");
                    views.setInt(R.id.play_pause, "setImageResource", R.drawable.ic_play_circle_fill);
                    STARTED_WIDGETS.put(appWidgetId, false);
                } else {
                    Log.d(LOG_TAG, "Started");
                    if (myHandler == null) {
                        myHandler = new MyHandler(context);
                    }
                    STARTED_WIDGETS.put(appWidgetId, true);
                    views.setInt(R.id.play_pause, "setImageResource", R.drawable.ic_pause_circle_fill);
                    if (TIME_WIDGETS.get(appWidgetId) == 0) {
                        TIME_WIDGETS.put(appWidgetId, DEFAULT_TIME);
                    }
                    executorService.execute((new Runnable() {
                        @Override
                        public void run() {
                            Thread thisThread = Thread.currentThread();
                            threadMap.put(appWidgetId, thisThread);
                            int remainedTime = TIME_WIDGETS.get(appWidgetId);
                            Log.d(LOG_TAG, "remained time " + remainedTime);
                            for (int i = 1; i <= remainedTime; i++) {
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                    if (thisThread.equals(threadMap.get(appWidgetId))) {
                                        Log.d(LOG_TAG, "Entered");
                                        int newTime = remainedTime - i;
                                        Log.d(LOG_TAG, "newTime " + newTime);
                                        TIME_WIDGETS.put(appWidgetId, newTime);
                                        views.setTextViewText(R.id.tv_timer, String.format(FORMAT, newTime));
                                        updateWidget(context, appWidgetId, views);
                                    } else {
                                        return;
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            resetTimer(context, views, appWidgetId);
                            vibrator.vibrate(200);
                            myHandler.sendEmptyMessage(0);
                        }
                    }));
                }
                updateWidget(context, appWidgetId, views);
            }
            Log.d(LOG_TAG, ACTION_UPDATE);
        } else if (action.equals(ACTION_RESTART)) {
            restartTimer(context, appWidgetId, views);
        }
    }

    private void restartTimer(Context context, int appWidgetId, RemoteViews views) {
        if (appWidgetId != 0) {
            Log.d(LOG_TAG, ACTION_RESTART);
            threadMap.put(appWidgetId, null);
            resetTimer(context, views, appWidgetId);
        }
    }

    private void resetTimer(Context context, RemoteViews views, int appWidgetId) {
        Log.d(LOG_TAG, "Reset timer");
        Log.d(LOG_TAG, "" + TIME_WIDGETS.get(appWidgetId));
        views.setTextViewText(R.id.tv_timer, DEFAULT_TEXT);
        views.setInt(R.id.play_pause, "setImageResource", R.drawable.ic_play_circle_fill);
        TIME_WIDGETS.put(appWidgetId, DEFAULT_TIME);
        STARTED_WIDGETS.put(appWidgetId, false);
        updateWidget(context, appWidgetId, views);
    }

    private void updateWidget(Context context, int appWidgetId, RemoteViews views) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static PendingIntent getPendingIntent(Context context, int i, Intent intent) {
        return PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static class MyHandler extends Handler {
        private WeakReference<Context> contextWeakReference;

        public MyHandler(Context context) {
            this.contextWeakReference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(contextWeakReference.get(), "Time has come!", Toast.LENGTH_SHORT).show();
        }
    }
}
