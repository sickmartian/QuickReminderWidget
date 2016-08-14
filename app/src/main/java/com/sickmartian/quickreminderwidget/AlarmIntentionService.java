package com.sickmartian.quickreminderwidget;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/14/16.
 */
public class AlarmIntentionService extends IntentService {
    public AlarmIntentionService() {
        super(AlarmIntentionService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final AlarmIntentionData intentionData =
                    Parcels.unwrap(intent.getParcelableExtra(AlarmIntentionReceiver.ALARM_INTENTION_DATA));

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

                // Toast needs to be run on the main thread
                Handler handler = new Handler(Looper.getMainLooper());
                final LocalDateTime finalAlarmTime = alarmTime;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QAWApp.getAppContext(),
                                String.format(QAWApp.getAppContext().getString(R.string.alarm_created_for),
                                        finalAlarmTime.toString(QAWApp.dateTimeFormatter)),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            } else {
                intentionData.getAlarm().deleteSync();

                // Toast needs to be run on the main thread
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                     @Override
                     public void run() {
                         Toast.makeText(QAWApp.getAppContext(),
                                 String.format(QAWApp.getAppContext().getString(R.string.alarm_deleted_for),
                                         intentionData.getTime().toString(QAWApp.dateTimeFormatter)),
                                 Toast.LENGTH_LONG)
                                 .show();
                     }
                 });
            }

            QAWApp.updateAllWidgets();
            CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
        } finally {
            AlarmIntentionReceiver.completeWakefulIntent(intent);
        }
    }
}
