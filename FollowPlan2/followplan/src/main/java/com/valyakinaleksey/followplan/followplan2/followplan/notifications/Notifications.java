package com.valyakinaleksey.followplan.followplan2.followplan.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class Notifications {
    public static final String DATETIME = "timeNotification";
//    public static void scheduleNotification(Activity activity, Notification notification, long delay) {
//        Intent notificationIntent = new Intent(activity, NotificationPublisher.class);
//        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
//        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        long futureInMillis = SystemClock.elapsedRealtime() + delay;
//        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
//    }

    public static Notification getNotification(Context context, int id, String title, String content) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                id, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .setPriority(Notification.PRIORITY_MAX)
//                .setLights(0xff00ff00, 300, 100)
                .setContentIntent(contentIntent);
        return builder.build();
    }

    public static List<Long> getDateNotifications(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        List<Long> times = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllNotifications();
        while (cursor.moveToNext()) {
            times.add(cursor.getLong(cursor.getColumnIndex(DATETIME)));
        }
        cursor.close();
        databaseHelper.close();
        return times;
    }
}
