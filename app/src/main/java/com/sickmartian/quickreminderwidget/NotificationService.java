package com.sickmartian.quickreminderwidget;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;

import java.util.List;

import timber.log.Timber;

/**
 * Created by sickmartian on 8/13/16.
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
                Alarm lastAlarm = currentAlarms.get(currentAlarms.size() - 1);
                Alarm.deleteUpTo(lastAlarm.getDateTime());

                // Update widget if we just removed a custom alarm
                // because the TimeSync is not gonna run for us
                for (Alarm currentAlarm : currentAlarms) {
                    if (currentAlarm.isCustomDateTime(App.isThereOneEvery30())) {
                        App.updateAllQuickReminderWidgets();
                        break;
                    }
                }

                // Start with the notification, one for each alarm
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

                for (Alarm alarm : currentAlarms) {
                    // Customize with note or creation date for each
                    if (alarm.getNote() != null) {
                        String content = String.format(getString(R.string.alarm_content_note_title), alarm.getNote());
                        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setContentText(content);
                    } else {
                        notificationBuilder.setContentText(String.format(getString(R.string.alarm_content_creation_title),
                                alarm.getCreationDateTime().toString(App.dateTimeFormatter)));
                    }

                    int notificationId = App.getNewNotificationId();

                    // Allow the user to re-create the alarm
                    notificationBuilder.setContentIntent(
                            Utils.getPIInNewStack(ReminderEditionActivity.getIntentForReCreation(alarm),
                                    notificationId * -1));

                    // Trigger notification
                    Notification notification = notificationBuilder.build();
                    notificationManager.notify(NOTIFICATION, notificationId, notification);
                }
            }
        } finally {
            Timber.d("NotificationService ending");
            NotificationReceiver.completeWakefulIntent(intent);
        }
    }

    public static void setupCustomNotification(Context context, NotificationCompat.Builder notificationBuilder) {
        SharedPreferences widgetPreferences = App.getSharedPreferences();
        if (widgetPreferences.getBoolean(App.CUSTOM_NOTIFICATION, false)) {
            int defaults = 0;
            if (widgetPreferences.getBoolean(App.CUSTOM_NOTIFICATION_VIBRATE, true)) {
                defaults |= Notification.DEFAULT_VIBRATE;
            }
            if (widgetPreferences.getBoolean(App.CUSTOM_NOTIFICATION_LIGHTS, true)) {
                defaults |= Notification.DEFAULT_LIGHTS;
            }
            String notificationUriString = widgetPreferences.getString(App.CUSTOM_NOTIFICATION_SOUND, null);
            if (notificationUriString != null) {
                // Check custom ringtone:
                try {
                    RingtoneManager rm = new RingtoneManager(context);
                    rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                    Uri loadedRingtoneUri = Uri.parse(notificationUriString);
                    Ringtone tone = RingtoneManager.getRingtone(context, loadedRingtoneUri);
                    if (tone != null) {
                        notificationBuilder.setSound(loadedRingtoneUri);
                    } else {
                        defaults |= Notification.DEFAULT_SOUND;
                    }
                } catch (Exception e) {
                    defaults |= Notification.DEFAULT_SOUND;
                }
            } else {
                defaults |= Notification.DEFAULT_SOUND;
            }
            notificationBuilder.setDefaults(defaults);
        } else {
            notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        }
    }
}