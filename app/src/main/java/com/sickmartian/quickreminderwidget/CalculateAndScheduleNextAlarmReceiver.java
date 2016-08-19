package com.sickmartian.quickreminderwidget;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.joda.time.DateTimeZone;

import java.util.TimeZone;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/13/16.
 */
public class CalculateAndScheduleNextAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, CalculateAndScheduleNextAlarmService.class);

        // If we got updated or the device was restarted, we might have lost some notifications
        // in the meantime, so we have to trigger the notifications again just in case
        if ( intent != null && intent.getAction() != null &&
                (   intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED") ||
                    intent.getAction().equalsIgnoreCase("android.intent.action.MY_PACKAGE_REPLACED") ) ) {
            service.putExtra(CalculateAndScheduleNextAlarmService.TRIGGER_OLD_NOTIFICATION, true);
        }

        if (intent == null) {
            Timber.d("CalculateAndScheduleNextAlarmReceiver - Intent is null");
        } else if (intent.getAction() == null) {
            Timber.d("CalculateAndScheduleNextAlarmReceiver - Action is null");
        } else {
            Timber.d("CalculateAndScheduleNextAlarmReceiver - Action is " + intent.getAction());
        }

        try {
            // Update joda time with system timezone... o.O
            DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getDefault()));
        } catch (IllegalArgumentException e) {
            Timber.e(e, "Problem updating joda time with:" + TimeZone.getDefault().getDisplayName());
        }
        startWakefulService(context, service);
    }

    public static void sendBroadcast() {
        Intent intent = new Intent(App.getAppContext(), CalculateAndScheduleNextAlarmReceiver.class);
        App.getAppContext().sendBroadcast(intent);
    }
}
