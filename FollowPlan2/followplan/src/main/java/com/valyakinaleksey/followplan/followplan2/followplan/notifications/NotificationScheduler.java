package com.valyakinaleksey.followplan.followplan2.followplan.notifications;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import com.valyakinaleksey.followplan.followplan2.followplan.activities.MainActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.R;

public class NotificationScheduler {
    public static void scheduleNotification(Activity activity, Notification notification, long delay) {
        Intent notificationIntent = new Intent(activity, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    public static Notification getNotification(Context context, String content) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Follow Plan")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent);
        return builder.build();
    }
}
