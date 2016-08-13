package com.sickmartian.quickalarmwidget;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/13/16.
 */
public class NotificationService extends IntentService {
    public static final String NOTIFICATION = "ALARM_NOTIFICATION";
    private static final int REMINDER_NOTIFICATION_ID = 7777;

    public NotificationService() {
        super(NotificationService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Timber.i("NotificationService starting");
            LocalDateTime now = LocalDateTime.now();

            // Check that there is an alarm for the notification
            Alarm lastAlarm = Alarm.getLastSync(now);

            // Calculate next alarm after adjusting them
            CalculateAndScheduleNextAlarmReceiver.sendBroadcast();


            if (lastAlarm != null) {
                // Cleanup old alarms we don't care about anymore
                Alarm.deleteUpTo(lastAlarm.getAlarmTime());

                // Start with the notification:
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                int smallIcon = Utils.getAppSmallIcon();

                setupCustomNotification(this, notificationBuilder);

                notificationBuilder.setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.app_name))
                        .setSmallIcon(smallIcon)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setAutoCancel(true)
                        .setCategory(Notification.CATEGORY_REMINDER);
                Notification notification = notificationBuilder.build();
                notificationManager.notify(NOTIFICATION, REMINDER_NOTIFICATION_ID, notification);
            }
        } finally {
            Timber.i("NotificationService ending");
            NotificationReceiver.completeWakefulIntent(intent);
        }
    }

    public static void setupCustomNotification(Context context, NotificationCompat.Builder notificationBuilder) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        if (sharedPreferences.getBoolean(SettingsActivity.CUSTOM_NOTIFICATION, false)) {
//            int defaults = 0;
//            if (sharedPreferences.getBoolean(SettingsActivity.CUSTOM_NOTIFICATION_VIBRATE, true)) {
//                defaults |= Notification.DEFAULT_VIBRATE;
//            }
//            if (sharedPreferences.getBoolean(SettingsActivity.CUSTOM_NOTIFICATION_LIGHTS, true)) {
//                defaults |= Notification.DEFAULT_LIGHTS;
//            }
//            String notificationUriString = sharedPreferences.getString(SettingsActivity.CUSTOM_NOTIFICATION_SOUND, null);
//            if (notificationUriString != null) {
//                // Check custom ringtone:
//                try {
//                    RingtoneManager rm = new RingtoneManager(context);
//                    rm.setType(RingtoneManager.TYPE_NOTIFICATION);
//                    Uri loadedRingtoneUri = Uri.parse(notificationUriString);
//                    Ringtone tone = RingtoneManager.getRingtone(context, loadedRingtoneUri);
//                    if (tone != null) {
//                        notificationBuilder.setSound(loadedRingtoneUri);
//                    } else {
//                        defaults |= Notification.DEFAULT_SOUND;
//                    }
//                } catch (Exception e) {
//                    defaults |= Notification.DEFAULT_SOUND;
//                }
//            } else {
//                defaults |= Notification.DEFAULT_SOUND;
//            }
//            notificationBuilder.setDefaults(defaults);
//        } else {
            notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
//        }
    }
}