package com.sickmartian.quickreminderwidget;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class AlarmIntentionReceiver extends WakefulBroadcastReceiver {
    public static final String ALARM_INTENTION_DATA = "ALARM_INTENTION_DATA";
    public static final String AND_OFFER_EDITION = "AND_OFFER_EDITION";

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.getExtras();
        Intent serviceIntent = new Intent(context, AlarmIntentionService.class);
        serviceIntent.putExtras(intent);
        startWakefulService(context, serviceIntent);
    }
}
