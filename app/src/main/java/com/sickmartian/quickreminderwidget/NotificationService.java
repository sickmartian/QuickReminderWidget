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
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import java.util.List;

import timber.log.Timber;

import static com.sickmartian.quickreminderwidget.App.SNOOZE_1;
import static com.sickmartian.quickreminderwidget.App.SNOOZE_2;
import static com.sickmartian.quickreminderwidget.App.SNOOZE_3;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_1;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_2;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_3;

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
                assert notificationManager != null;

                int smallIcon = Utils.getAppSmallIcon();
                setupCustomNotification(this, notificationManager, notificationBuilder);

                notificationBuilder.setContentTitle(getString(R.string.app_name))
                        .setSmallIcon(smallIcon)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setAutoCancel(true)
                        .setGroup("QAWNotificationGroup");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notificationBuilder.setCategory(Notification.CATEGORY_REMINDER);
                }

                for (Alarm alarm : currentAlarms) {
                    int notificationId = getUniqueId();
                    setupActions(this, alarm, notificationId, notificationBuilder);

                    // Customize with note or creation date for each
                    if (alarm.getNote() != null) {
                        String content = String.format(getString(R.string.alarm_content_note_title), alarm.getNote());
                        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                                .setContentText(content);
                    } else {
                        notificationBuilder.setContentText(String.format(getString(R.string.alarm_content_creation_title),
                                alarm.getCreationDateTime().toString(App.dateTimeFormatter)));
                    }

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
        int1.putExtra(NOTIFICATION_ID, notificationId);
        int1.putExtra(SnoozeService.REQUESTED_SNOOZE, snoozeMinutes);
        PendingIntent snooze1Intent = PendingIntent.getService(context,
                getUniqueId(),
                int1,
                PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.addAction(R.drawable.ic_alarm_on_black_24dp,
                CustomAlarmTimeValue.getCustomValueShortLabel(snoozeMinutes),
                snooze1Intent);
    }

    public static void setupCustomNotification(Context context, NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder) {
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

    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final int NOTIFICATION_ID_INITIAL_VALUE = -1;
    private static int getUniqueId() {
        int notificationId = App.getSharedPreferences().getInt(NOTIFICATION_ID, NOTIFICATION_ID_INITIAL_VALUE);
        if (notificationId == Integer.MAX_VALUE) {
            notificationId = NOTIFICATION_ID_INITIAL_VALUE;
        }
        App.getSharedPreferences().edit().putInt(NOTIFICATION_ID, ++notificationId).commit();
        return notificationId;
    }
}