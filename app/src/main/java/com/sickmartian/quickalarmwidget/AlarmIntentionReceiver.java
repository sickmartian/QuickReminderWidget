package com.sickmartian.quickalarmwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class AlarmIntentionReceiver extends BroadcastReceiver {
    public static final String ALARM_INTENTION_DATA = "ALARM_INTENTION_DATA";

    @Override
    public void onReceive(Context context, Intent intent) {
        final AlarmIntentionData intentionData =
                Parcels.unwrap(intent.getParcelableExtra(ALARM_INTENTION_DATA));

        Timber.d("Received alarm intention: " + intentionData.toString());

        if (intentionData.getAlarm() == null) {
            LocalDateTime alarmTime = null;
            if (intentionData.getTime() != null) {
                alarmTime = intentionData.getTime();
            } else if (intentionData.getDuration() != null) {
                alarmTime = Utils.getNow().plus(intentionData.getDuration())
                        .withSecondOfMinute(0)
                        .withMillisOfSecond(0);
            }
            assert alarmTime != null; // Either a duration or a time, if none something is really fishy
            Alarm.fromTime(alarmTime).saveSync();
            Toast.makeText(QAWApp.getAppContext(),
                    String.format(QAWApp.getAppContext().getString(R.string.alarm_created_for),
                            alarmTime.toString(QAWApp.dateTimeFormatter)),
                    Toast.LENGTH_LONG)
                    .show();
        } else {
            intentionData.getAlarm().deleteSync();
            Toast.makeText(QAWApp.getAppContext(),
                    String.format(QAWApp.getAppContext().getString(R.string.alarm_deleted_for),
                            intentionData.getTime().toString(QAWApp.dateTimeFormatter)),
                    Toast.LENGTH_LONG)
                    .show();
        }

        QAWApp.updateAllWidgets();
        CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
    }
}
