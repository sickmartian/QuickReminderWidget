package com.sickmartian.quickreminderwidget;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;

import java.util.List;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/13/16.
 */
public class NotificationService extends IntentService {
    public static final String NOTIFICATION = "ALARM_NOTIFICATION";

    public NotificationService() {
        super(NotificationService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Timber.d("NotificationService starting");
            LocalDateTime now = LocalDateTime.now();

            // Check that there is an alarm for the notification
            List<Alarm> currentAlarms = Alarm.getLastsSync(now);

            // Calculate next alarm after adjusting them
            CalculateAndScheduleNextAlarmReceiver.sendBroadcast();

            if (currentAlarms.size() > 0) {
                // Cleanup old alarms we don't care about anymore
                Alarm.deleteUpTo(currentAlarms.get(currentAlarms.size() - 1).getDateTime());

                // Update widget if we just removed a custom alarm
                // because the TimeSync is not gonna run for us
                for (Alarm currentAlarm : currentAlarms) {
                    if (currentAlarm.isCustomDateTime(QAWApp.isThereOneEvery30())) {
                        QAWApp.updateAllWidgets();
                        break;
                    }
                }

                // Start with the notification:
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                int smallIcon = Utils.getAppSmallIcon();

                setupCustomNotification(this, notificationBuilder);

                notificationBuilder.setContentTitle(getString(R.string.app_name))
                        .setSmallIcon(smallIcon)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setAutoCancel(true)
                        .setGroup("QAWNotificationGroup")
                        .setCategory(Notification.CATEGORY_REMINDER);

                // Set note(s)
                StringBuilder sb = new StringBuilder();
                if (currentAlarms.size() > 1) {
                    sb.append(getString(R.string.alarm_notification_notes_label));
                } else {
                    sb.append(getString(R.string.alarm_notification_note_label));
                }
                boolean foundOne = false;
                for (Alarm alarm : currentAlarms) {
                    if (alarm.getNote() != null) {
                        sb.append(alarm.getNote());
                        foundOne = true;
                    }
                }
                if (foundOne) {
                    notificationBuilder.setContentText(sb.toString());
                }

                Notification notification = notificationBuilder.build();
                notificationManager.notify(NOTIFICATION, QAWApp.getNewNotificationId(), notification);
            }
        } finally {
            Timber.d("NotificationService ending");
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