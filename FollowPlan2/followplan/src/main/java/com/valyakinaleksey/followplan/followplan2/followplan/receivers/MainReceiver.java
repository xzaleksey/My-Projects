package com.valyakinaleksey.followplan.followplan2.followplan.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.MyService;

import static com.valyakinaleksey.followplan.followplan2.followplan.MyService.NOTIFICATION_TIME;
import static com.valyakinaleksey.followplan.followplan2.followplan.MyService.TYPE;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.LOG_TAG;

public class MainReceiver extends BroadcastReceiver {
    public static final int ACTION_UPDATE = 1;
    public static final int ACTION_NOTIFICATION = 2;
    public static final int ACTION_INIT_DB = 3;


    public final static String BROADCAST_ACTION = "com.valyakinaleksey.followplan.followplan2.followplan";

    public static void checkPendingIntentExist(Context context) {
        final Intent intent = new Intent(context, MainReceiver.class);
        intent.setAction(BROADCAST_ACTION);
        boolean alarmUp = (PendingIntent.getBroadcast(context, ACTION_UPDATE,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null);
        if (!alarmUp) {
            Log.d(LOG_TAG, "Update alarm not set");
            final Intent intent1 = new Intent(context, MyService.class);
            context.startService(intent1);
        } else {
            Log.d(LOG_TAG, "Update alarm is set");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "MainReceiver onReceive");
        int type = intent.getIntExtra(TYPE, ACTION_UPDATE);
        if (type == ACTION_INIT_DB) {
            Log.d(LOG_TAG, "MainReceiver Init from DB");
            new DatabaseHelper(context).initFromDb();
        } else {
            final Intent serviceIntent = new Intent(context, MyService.class);
            serviceIntent.putExtra(TYPE, type);
            if (type == ACTION_NOTIFICATION) {
                final long longExtra = intent.getLongExtra(NOTIFICATION_TIME, 0);
                Log.d(LOG_TAG, "notification time " + longExtra);
                serviceIntent.putExtra(NOTIFICATION_TIME, longExtra);
            }
            context.startService(serviceIntent);
        }
    }
}
