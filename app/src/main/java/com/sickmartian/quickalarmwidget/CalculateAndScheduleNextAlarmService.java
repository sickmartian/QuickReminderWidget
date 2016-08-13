package com.sickmartian.quickalarmwidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/13/16.
 */
public class CalculateAndScheduleNextAlarmService extends IntentService {

    private static final int REQUEST_CODE = 9898;

    public CalculateAndScheduleNextAlarmService() {
        super(CalculateAndScheduleNextAlarmService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Timber.i("CalculateAndScheduleNextAlarmService starting");

            // Calculate next alarm
            Alarm nextAlarm = Alarm.getNextSync(Utils.getNow());

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent notificationIntent = new Intent(this, NotificationReceiver.class);

            if (nextAlarm != null) {
                Timber.d("Next Alarm: " + nextAlarm.getAlarmTime().toDateTime().toString());
                PendingIntent notificationPendingIntent =
                        PendingIntent.getBroadcast(this,
                                CalculateAndScheduleNextAlarmService.REQUEST_CODE,
                                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            nextAlarm.getAlarmTime().toDateTime().getMillis(),
                            notificationPendingIntent);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                                nextAlarm.getAlarmTime().toDateTime().getMillis(),
                                notificationPendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                nextAlarm.getAlarmTime().toDateTime().getMillis(),
                                notificationPendingIntent);
                    }
                }
            } else {
                Timber.d("Next Alarm set to none");
                PendingIntent notificationPendingIntent =
                        PendingIntent.getBroadcast(this,
                                CalculateAndScheduleNextAlarmService.REQUEST_CODE,
                                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                alarmManager.cancel(notificationPendingIntent);
            }
        } finally {
            Timber.i("CalculateAndScheduleNextAlarmService ending");
            if (intent != null) {
                CalculateAndScheduleNextAlarmReceiver.completeWakefulIntent(intent);
            }
        }

    }
}

