package com.sickmartian.quickalarmwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class AlarmIntentionReceiver extends BroadcastReceiver {
    public static final String ALARM_TIME = "ALARM_TIME";
    public static final String ALARM = "EXISTING_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        final LocalDateTime time = (LocalDateTime) intent.getSerializableExtra(ALARM_TIME);
        Alarm alarm = Parcels.unwrap(intent.getParcelableExtra(ALARM));

        Timber.d("Alarm intention for: " + time.toString());
        Timber.d("Alarm: " + (alarm == null ? "none" : alarm.toString()));

        if (alarm == null) {
            Alarm.fromTime(time).saveSync();
            Timber.d("Alarm created");
        } else {
            alarm.deleteSync();
            Timber.d("Alarm deleted");
        }

        QAWApp.updateAllWidgets();
        CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
    }
}
