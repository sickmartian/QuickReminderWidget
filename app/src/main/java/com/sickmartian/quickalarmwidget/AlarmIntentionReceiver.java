package com.sickmartian.quickalarmwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableDuration;
import org.parceler.Parcels;

import java.io.Serializable;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class AlarmIntentionReceiver extends BroadcastReceiver {
    public static final String ALARM_TIME_OBJECT = "ALARM_TIME_OBJECT";
    public static final String ALARM = "EXISTING_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        final Serializable timeObject = intent.getSerializableExtra(ALARM_TIME_OBJECT);
        Alarm alarm = Parcels.unwrap(intent.getParcelableExtra(ALARM));

        Timber.d("Alarm intention for: " + timeObject.toString());
        Timber.d("Alarm: " + (alarm == null ? "none" : alarm.toString()));

        if (alarm == null) {
            if (timeObject instanceof LocalDateTime) {
                Alarm.fromTime((LocalDateTime) timeObject).saveSync();
            } else if (timeObject instanceof Duration) {
                Alarm.fromTime(Utils.getNow().plus((ReadableDuration) timeObject)).saveSync();
            }
            Timber.d("Alarm created");
        } else {
            alarm.deleteSync();
            Timber.d("Alarm deleted");
        }

        QAWApp.updateAllWidgets();
        CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
    }
}
