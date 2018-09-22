package com.sickmartian.quickreminderwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.util.TimeZone;

import timber.log.Timber;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_MY_PACKAGE_REPLACED;

/**
 * Created by sickmartian on 8/13/16.
 */
public class CalculateAndScheduleNextAlarmReceiver extends BroadcastReceiver {
    static final int REQUEST_CODE = 9898;

    @Override
    public void onReceive(Context context, Intent intent) {
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

        // If we got updated or the device was restarted, we might have lost some notifications
        // in the meantime, so we have to trigger the notifications again just in case
        boolean triggerOld = false;
        if (intent != null && intent.getAction() != null &&
                (ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                        ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()))) {
            triggerOld = true;
        }

        calculateAndScheduleNextAlarm(context, triggerOld);
    }

    protected void calculateAndScheduleNextAlarm(Context context, boolean triggerOld) {
        try {
            Timber.d("calculateAndScheduleNextAlarm starting");
            LocalDateTime now = Utils.getNow();

            // For when we restart or the app gets updated and there are pending notifications
            // visible for the user
            if (triggerOld && Alarm.getLastsSync(now).size() > 0) {
                Timber.d("Trying to trigger pending notifications");
                // Notification will call this again after it's done and we can calculate the next alarm then
                NotificationReceiver.sendBroadcast();
            } else {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                assert alarmManager != null;

                // Calculate next alarm now
                Alarm nextAlarm = Alarm.getNextSync(now);

                Intent notificationIntent = new Intent(context, NotificationReceiver.class);
                if (nextAlarm != null) {
                    DateTime nextAlarmTime = Utils.convertLocalToDTSafely(nextAlarm.getDateTime());
                    Timber.i("Next Alarm: " + nextAlarmTime.toString());
                    PendingIntent notificationPendingIntent =
                            PendingIntent.getBroadcast(context,
                                    CalculateAndScheduleNextAlarmReceiver.REQUEST_CODE,
                                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                nextAlarmTime.getMillis(),
                                notificationPendingIntent);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                                    nextAlarmTime.getMillis(),
                                    notificationPendingIntent);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP,
                                    nextAlarmTime.getMillis(),
                                    notificationPendingIntent);
                        }
                    }
                } else {
                    Timber.i("Next Alarm set to none");
                    PendingIntent notificationPendingIntent =
                            PendingIntent.getBroadcast(context,
                                    CalculateAndScheduleNextAlarmReceiver.REQUEST_CODE,
                                    notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    alarmManager.cancel(notificationPendingIntent);
                }
            }
        } finally {
            Timber.d("calculateAndScheduleNextAlarm ending");
        }
    }

    public static void sendBroadcast() {
        Intent intent = new Intent(App.getAppContext(), CalculateAndScheduleNextAlarmReceiver.class);
        App.getAppContext().sendBroadcast(intent);
    }
}
