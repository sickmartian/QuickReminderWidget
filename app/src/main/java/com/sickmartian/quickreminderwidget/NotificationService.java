package com.sickmartian.quickreminderwidget;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
                Alarm lastAlarm = currentAlarms.get(currentAlarms.size() - 1);
                Alarm.deleteUpTo(lastAlarm.getDateTime());

                // Update widget if we just removed a custom alarm
                // because the TimeSync is not gonna run for us
                for (Alarm currentAlarm : currentAlarms) {
                    if (currentAlarm.isCustomDateTime(App.isThereOneEvery30())) {
                        App.updateAllWidgets();
                        break;
                    }
                }

                // Start with the notification, one for each alarm
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                int smallIcon = Utils.getAppSmallIcon();

                Alarm firstAlarm = currentAlarms.get(0);
                setupCustomNotification(this, firstAlarm.getSourceWidgetId(), notificationBuilder);

                notificationBuilder.setContentTitle(getString(R.string.app_name))
                        .setSmallIcon(smallIcon)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setAutoCancel(true)
                        .setGroup("QAWNotificationGroup")
                        .setCategory(Notification.CATEGORY_REMINDER);

                // Customize with note or creation date for each
                for (Alarm alarm : currentAlarms) {
                    if (alarm.getNote() != null) {
                        notificationBuilder.setContentText(
                                String.format(getString(R.string.alarm_content_note_title),
                                        alarm.getNote())
                        );
                    } else {
                        notificationBuilder.setContentText(
                                String.format(getString(R.string.alarm_content_creation_title),
                                        alarm.getCreationDateTime().toString(App.dateTimeFormatter))
                        );
                    }

                    // Trigger notification
                    Notification notification = notificationBuilder.build();
                    notificationManager.notify(NOTIFICATION, App.getNewNotificationId(), notification);
                }
            }
        } finally {
            Timber.d("NotificationService ending");
            NotificationReceiver.completeWakefulIntent(intent);
        }
    }

    public static void setupCustomNotification(Context context, int widgetId, NotificationCompat.Builder notificationBuilder) {
        SharedPreferences widgetPreferences = QuickReminderWidgetProvider.getWidgetSharedPref(widgetId);
        if (widgetPreferences.getBoolean(QuickReminderWidgetProvider.CUSTOM_NOTIFICATION, false)) {
            int defaults = 0;
            if (widgetPreferences.getBoolean(QuickReminderWidgetProvider.CUSTOM_NOTIFICATION_VIBRATE, true)) {
                defaults |= Notification.DEFAULT_VIBRATE;
            }
            if (widgetPreferences.getBoolean(QuickReminderWidgetProvider.CUSTOM_NOTIFICATION_LIGHTS, true)) {
                defaults |= Notification.DEFAULT_LIGHTS;
            }
            String notificationUriString = widgetPreferences.getString(QuickReminderWidgetProvider.CUSTOM_NOTIFICATION_SOUND, null);
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