package com.sickmartian.quickreminderwidget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import java.util.List;

import timber.log.Timber;

import static com.sickmartian.quickreminderwidget.App.REMINDER_NOTIFICATION_CHANNEL;
import static com.sickmartian.quickreminderwidget.App.SNOOZE_1;
import static com.sickmartian.quickreminderwidget.App.SNOOZE_2;
import static com.sickmartian.quickreminderwidget.App.SNOOZE_3;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_1;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_2;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_3;

/**
 * Created by sickmartian on 8/13/16.
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION = "ALARM_NOTIFICATION";
    static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    public static void setupCustomNotification(Context context,
                                               NotificationCompat.Builder notificationBuilder) {
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

    private void setupActions(Context context, Alarm alarm, int notificationId,
                              NotificationCompat.Builder notificationBuilder) {
        SharedPreferences sharedPref = App.getSharedPreferences();
        int snooze1 = sharedPref.getInt(SNOOZE_1, DEFAULT_CUSTOM_TIME_1);
        if (snooze1 != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            setupSnooze(context, alarm, notificationId, notificationBuilder, snooze1);
        }

        int snooze2 = sharedPref.getInt(SNOOZE_2, DEFAULT_CUSTOM_TIME_2);
        if (snooze2 != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            setupSnooze(context, alarm, notificationId, notificationBuilder, snooze2);
        }

        int snooze3 = sharedPref.getInt(SNOOZE_3, DEFAULT_CUSTOM_TIME_3);
        if (snooze3 != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            setupSnooze(context, alarm, notificationId, notificationBuilder, snooze3);
        }
    }

    private void setupSnooze(Context context, Alarm alarm, int notificationId,
                             NotificationCompat.Builder notificationBuilder, int snoozeMinutes) {
        Intent int1 = new Intent(context, SnoozeService.class);
        int1.putExtra(SnoozeService.ALARM_PARAMETER, Parcels.wrap(alarm));
        int1.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId);
        int1.putExtra(SnoozeService.REQUESTED_SNOOZE, snoozeMinutes);
        PendingIntent snooze1Intent = PendingIntent.getService(context,
                Utils.getSharedUniqueId(),
                int1,
                PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.addAction(R.drawable.ic_snooze_black_24dp,
                CustomAlarmTimeValue.getCustomValueShortLabel(snoozeMinutes),
                snooze1Intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("NotificationReceiver invoked");
        processAlarmsAndCreateNotifications(context);
    }

    private void processAlarmsAndCreateNotifications(Context context) {
        try {
            Timber.d("processAlarmsAndCreateNotifications - starting");
            LocalDateTime now = LocalDateTime.now();

            // Check that there is an alarm for the notification
            List<Alarm> currentAlarms = Alarm.getLastsSync(now);

            // Calculate next alarm after adjusting them
            CalculateAndScheduleNextAlarmReceiver.sendBroadcast();

            if (currentAlarms.size() > 0) {
                // Cleanup old alarms we don't care about anymore
                Alarm lastAlarm = currentAlarms.get(currentAlarms.size() - 1);
                Alarm.deleteUpTo(lastAlarm.getDateTime());

                // Update presentation if we just removed a custom alarm
                // because the TimeSync is not gonna run for us
                for (Alarm currentAlarm : currentAlarms) {
                    if (currentAlarm.isCustomDateTime(App.isThereOneEvery30())) {
                        App.updatePresentation();
                        break;
                    }
                }

                // Start with the notification, one for each alarm
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL);

                int smallIcon = Utils.getAppSmallIcon();
                NotificationReceiver.setupCustomNotification(context, notificationBuilder);

                notificationBuilder.setContentTitle(context.getString(R.string.app_name))
                        .setSmallIcon(smallIcon)
                        .setColor(context.getResources().getColor(R.color.colorPrimary))
                        .setAutoCancel(true)
                        .setGroup("QAWNotificationGroup");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notificationBuilder.setCategory(Notification.CATEGORY_REMINDER);
                }

                for (Alarm alarm : currentAlarms) {
                    int notificationId = Utils.getSharedUniqueId();
                    setupActions(context, alarm, notificationId, notificationBuilder);

                    // Customize with note or creation date for each
                    if (alarm.getNote() == null || alarm.getNote().isEmpty()) {
                        notificationBuilder.setContentText(String.format(
                                context.getString(R.string.alarm_content_creation_title),
                                alarm.getCreationDateTime().toString(App.dateTimeFormatter)));
                    } else {
                        String content = String.format(
                                context.getString(R.string.alarm_content_note_title),
                                alarm.getNote());
                        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                                .setContentText(content);
                    }

                    // Allow the user to re-create the alarm
                    notificationBuilder.setContentIntent(
                            Utils.getPIInNewStack(ReminderEditionActivity.getIntentForReCreation(alarm),
                                    notificationId * -1));

                    // Trigger notification
                    Notification notification = notificationBuilder.build();
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    assert notificationManager != null;
                    notificationManager.notify(NotificationReceiver.NOTIFICATION, notificationId, notification);
                }
            }
        } finally {
            Timber.d("processAlarmsAndCreateNotifications - ending");
        }
    }

    public static void sendBroadcast() {
        Intent intent = new Intent(App.getAppContext(), NotificationReceiver.class);
        App.getAppContext().sendBroadcast(intent);
    }
}
