package com.valyakinaleksey.followplan.followplan2.followplan.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.valyakinaleksey.followplan.followplan2.followplan.MyService;

import static com.valyakinaleksey.followplan.followplan2.followplan.help_classes.Constants.LOG_TAG;

public class BootReceiver extends BroadcastReceiver {
    public static final int ID_ACTION_BOOT = 4;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "BootReceiver onReceive");
        final Intent intent1 = new Intent(context, MyService.class);
        intent1.putExtra(MainReceiver.TYPE, ID_ACTION_BOOT);
        context.startService(intent1);
    }

//    Intent createIntent(Context context) {
//        Intent intent = new Intent(context, BootReceiver.class);
//        intent.setAction(action);
//        intent.putExtra("extra", extra);
//        return intent;
//    }
}
