package com.sickmartian.quickreminderwidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
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
                List<Alarm> nextAlarms = Alarm.getNextsSync(now);

                // Calculate next alarm now
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                assert alarmManager != null;

                Intent notificationIntent = new Intent(this, NotificationReceiver.class);
                if (!nextAlarms.isEmpty()) {
                    DateTime nextAlarmTime = nextAlarms.get(0).getDateTime().toDateTime();
                    Timber.i("Next Alarm: " + nextAlarmTime.toString());
                    PendingIntent notificationPendingIntent =
                            PendingIntent.getBroadcast(this,
                                    CalculateAndScheduleNextAlarmService.REQUEST_CODE,
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
                            PendingIntent.getBroadcast(this,
                                    CalculateAndScheduleNextAlarmService.REQUEST_CODE,
                                    notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    alarmManager.cancel(notificationPendingIntent);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    handleShortcuts(this, nextAlarms);
                }
            }
        } finally {
            Timber.d("CalculateAndScheduleNextAlarmService ending");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void handleShortcuts(Context context, List<Alarm> nextAlarms) {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        assert shortcutManager != null;

        if (nextAlarms.isEmpty()) {
            shortcutManager.removeAllDynamicShortcuts();
            return;
        }

        List<ShortcutInfo> shortcuts = new ArrayList<>();
        for (Alarm nextAlarm : nextAlarms) {
            shortcuts.add(new ShortcutInfo.Builder(this, nextAlarm.getDateTime().toString())
                    .setShortLabel(Utils.getFormattedMessageForDate(nextAlarm.getDateTime(),
                            R.string.edit_shortcut_action_short))
                    .setLongLabel(Utils.getFormattedMessageForDate(nextAlarm.getDateTime(),
                            R.string.edit_shortcut_action_long))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_create_black_24dp))
                    .setIntent(ReminderEditionActivity.getIntentForEditionInShortcut(nextAlarm))
                    .build());
            if (shortcuts.size() == 3) {
                break;
            }
        }

        shortcutManager.setDynamicShortcuts(shortcuts);
    }
}

