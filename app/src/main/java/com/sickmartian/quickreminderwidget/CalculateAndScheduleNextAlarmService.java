package com.sickmartian.quickreminderwidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.List;

import timber.log.Timber;

/**
 * Created by sickmartian on 8/13/16.
 */
public class CalculateAndScheduleNextAlarmService extends IntentService {

    private static final int REQUEST_CODE = 9898;
    public static final String TRIGGER_OLD_NOTIFICATION = "TRIGGER_OLD_NOTIFICATION";

    public CalculateAndScheduleNextAlarmService() {
        super(CalculateAndScheduleNextAlarmService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Timber.d("CalculateAndScheduleNextAlarmService starting");
            LocalDateTime now = Utils.getNow();

            // For when we restart or the app gets updated and there are pending notifications
            // visible for the user
            if (intent.getBooleanExtra(TRIGGER_OLD_NOTIFICATION, false) && Alarm.getLastsSync(now).size() > 0) {
                Timber.d("Trying to trigger pending notifications");
                NotificationReceiver.sendBroadcast(); // Notification will call this again after it's done
                // and we can calculate the next alarm then
            } else {
                // Calculate next alarm now
                Alarm nextAlarm = Alarm.getNextSync(now);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent notificationIntent = new Intent(this, NotificationReceiver.class);
                if (nextAlarm != null) {
                    DateTime alarmTime = nextAlarm.getDateTime().toDateTime();
                    Timber.i("Next Alarm: " + alarmTime.toString());
                    PendingIntent notificationPendingIntent =
                            PendingIntent.getBroadcast(this,
                                    CalculateAndScheduleNextAlarmService.REQUEST_CODE,
                                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                alarmTime.getMillis(),
                                notificationPendingIntent);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                                    alarmTime.getMillis(),
                                    notificationPendingIntent);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP,
                                    alarmTime.getMillis(),
                                    notificationPendingIntent);
                        }
                    }
                } else {
                    Timber.i("Next Alarm set to none");
                    PendingIntent notificationPendingIntent =
                            PendingIntent.getBroadcast(this,
                                    CalculateAndScheduleNextAlarmService.REQUEST_CODE,
                                    notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    alarmManager.cancel(notificationPendingIntent);
                }
            }
        } finally {
            Timber.d("CalculateAndScheduleNextAlarmService ending");
            if (intent != null) {
                CalculateAndScheduleNextAlarmReceiver.completeWakefulIntent(intent);
            }
        }

    }
}

