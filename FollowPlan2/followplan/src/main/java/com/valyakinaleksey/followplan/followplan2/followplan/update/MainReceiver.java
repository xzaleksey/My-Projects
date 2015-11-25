package com.valyakinaleksey.followplan.followplan2.followplan.update;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.MyService;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;

import static com.valyakinaleksey.followplan.followplan2.followplan.MyService.NOTIFICATION_TIME;
import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.LOG_TAG;

public class MainReceiver extends BroadcastReceiver {
    public static final int ID_ACTION_UPDATE = 1;
    public static final int ID_ACTION_NOTIFICATION = 2;
    public static final int ID_ACTION_INIT_DB = 3;

    public static final String TYPE = "type";
    public final static String BROADCAST_ACTION = "com.valyakinaleksey.followplan.followplan2.followplan";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "MainReceiver onReceive");
        int type = intent.getIntExtra(TYPE, ID_ACTION_UPDATE);
        if (type == ID_ACTION_INIT_DB) {
            Log.d(LOG_TAG, "MainReceiver Init from DB");
            if (Plan.getPlans().size() > 0) {
                new DatabaseHelper(context).initFromDb();
            }
        } else {
            final Intent intent1 = new Intent(context, MyService.class);
            intent1.putExtra(TYPE, type);
            if (type == ID_ACTION_NOTIFICATION) {
                final long longExtra = intent.getLongExtra(NOTIFICATION_TIME, 0);
                Log.d(LOG_TAG, "notification time " + longExtra);
                intent1.putExtra(NOTIFICATION_TIME, longExtra);
            }
            context.startService(intent1);
        }
    }

    public static void checkPendingIntentExist(Context context) {
        final Intent intent = new Intent(context, MainReceiver.class);
        intent.setAction(BROADCAST_ACTION);
        boolean alarmUp = (PendingIntent.getBroadcast(context, ID_ACTION_UPDATE,
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


//    Intent createIntent(Context context) {
//        Intent intent = new Intent(context, BootReceiver.class);
//        intent.setAction(action);
//        intent.putExtra("extra", extra);
//        return intent;
//    }
}
