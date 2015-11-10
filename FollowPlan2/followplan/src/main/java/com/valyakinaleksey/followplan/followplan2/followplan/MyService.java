package com.valyakinaleksey.followplan.followplan2.followplan;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.valyakinaleksey.followplan.followplan2.followplan.activities.MainActivity;
import org.joda.time.DateTime;

public class MyService extends Service {
    private static final int NOTIFY_ID = 1;
    NotificationManager nm;

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    void sendNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_notification)
                .setTicker("Временная строка!")
                .setWhen(DateTime.now().getMillis())
                .setAutoCancel(true)
                .setContentTitle("Заголовок")
                .setContentText("Тело")
                .setContentIntent(contentIntent);
        Notification notification = builder.build();
        nm.notify(NOTIFY_ID, notification);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}
