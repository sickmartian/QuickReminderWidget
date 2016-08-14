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
                Alarm newAlarm = Alarm.fromDateTime(alarmTime);
                boolean created = newAlarm.createSync();

                if (created) {
                    if (intent.getBooleanExtra(AlarmIntentionReceiver.AND_OFFER_EDITION, false)) {
                        startActivity(AlarmEditionActivity.getIntentForEdition(newAlarm));
                    } else {
                        final LocalDateTime finalAlarmTime = alarmTime;
                        toastTo(String.format(QAWApp.getAppContext().getString(R.string.alarm_created_for),
                                finalAlarmTime.toString(QAWApp.dateTimeFormatter)));
                    }
                } else {
                    toastTo(QAWApp.getAppContext().getString(R.string.alarm_couldnt_be_created));
                }
            } else {
                intentionData.getAlarm().deleteSync();
                toastTo(String.format(QAWApp.getAppContext().getString(R.string.alarm_deleted_for),
                        intentionData.getTime().toString(QAWApp.dateTimeFormatter)));
            }

            QAWApp.updateAllWidgets();
            CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
        } finally {
            AlarmIntentionReceiver.completeWakefulIntent(intent);
        }

    }

    public void toastTo(final String toastMessage) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(QAWApp.getAppContext(),
                        toastMessage,
                        Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

}
