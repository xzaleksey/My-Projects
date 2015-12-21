package com.valyakinaleksey.timerwidgetupdated;

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
    public static final String ACTION_RESET = "com.valyakinaleksey.timer_widget.RESET";
    public static final String APP_WIDGET_ID = "appWidgetId";
    public static final String FORMAT = "0:%02d";
    public static final long DEFAULT_TIME = 60000;
    public static final String DEFAULT_TEXT = "1:00";
    private static Map<Integer, Timer> timerMap = new HashMap<>();
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
            timerMap.put(appWidgetId, new Timer(appWidgetId, DEFAULT_TIME));
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.play_pause, getPendingIntent(context, appWidgetId, getIntent(context, appWidgetId, ACTION_UPDATE)));
            views.setOnClickPendingIntent(R.id.restart, getPendingIntent(context, Integer.MAX_VALUE - appWidgetId, getIntent(context, appWidgetId, ACTION_RESET)));
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
        if (appWidgetId != 0) {
            if (!timerMap.containsKey(appWidgetId)) {
                timerMap.put(appWidgetId, new Timer(appWidgetId, DEFAULT_TIME));
            }
            final Timer timer = timerMap.get(appWidgetId);
            if (action.equals(ACTION_UPDATE)) {
                actionUpdate(context, appWidgetId, views, timer);
            } else if (action.equals(ACTION_RESET)) {
                resetTimer(context, views, appWidgetId);
            }
        }
    }

    private void actionUpdate(final Context context, final int appWidgetId, final RemoteViews views, final Timer timer) {
        if (timer.isStarted()) {
            timer.setElapsedTime(timer.getElapsedTime() + System.currentTimeMillis() - timer.getStartTime());
            Log.d(LOG_TAG, "Stopped");
            views.setInt(R.id.play_pause, "setImageResource", R.drawable.ic_play_circle_fill);
            timer.setThread(null);
            timer.setStarted(false);
        } else {
            final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            Log.d(LOG_TAG, "Started");
            if (myHandler == null) {
                myHandler = new MyHandler(context);
            }
            views.setInt(R.id.play_pause, "setImageResource", R.drawable.ic_pause_circle_fill);
            timer.setStarted(true);
            if (timer.getElapsedTime() >= timer.getEndTime()) {
                timer.setElapsedTime(0);
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Thread thread = Thread.currentThread();
                    timer.setThread(thread);
                    timer.setStartTime(System.currentTimeMillis());
                    long endTime = timer.getEndTime();
                    long elapsedTime = timer.getElapsedTime();
                    while (elapsedTime <= endTime) {
                        long waitTime = 1000 - elapsedTime % 1000;
                        try {
                            TimeUnit.MILLISECONDS.sleep(waitTime);
                            if (thread.equals(timer.getThread())) {
                                elapsedTime += waitTime;
                                views.setTextViewText(R.id.tv_timer, String.format(FORMAT, (endTime - elapsedTime) / 1000));
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
            });
        }
        updateWidget(context, appWidgetId, views);
    }


    private void resetTimer(Context context, RemoteViews views, int appWidgetId) {
        Log.d(LOG_TAG, "Reset timer");
        views.setTextViewText(R.id.tv_timer, DEFAULT_TEXT);
        views.setInt(R.id.play_pause, "setImageResource", R.drawable.ic_play_circle_fill);
        Timer timer = timerMap.get(appWidgetId);
        if (timer != null) {
            timer.setElapsedTime(0);
            timer.setStarted(false);
            timer.setThread(null);
        }
        updateWidget(context, appWidgetId, views);
    }

    private void updateWidget(Context context, int appWidgetId, RemoteViews views) {
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
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
            Context context = contextWeakReference.get();
            if (context != null) {
                Toast.makeText(context, "Time has come!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
