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
        Intent intent = new Intent(QAWApp.getAppContext(), CalculateAndScheduleNextAlarmReceiver.class);
        QAWApp.getAppContext().sendBroadcast(intent);
    }
}
