package com.sickmartian.quickreminderwidget;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_1;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_2;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_3;

/**
 * Created by Leo on 11/12/17.
 */

public class SnoozeService extends IntentService {

    public static final String ALARM_PARAMETER = "ALARM";
    public static final String REQUESTED_SNOOZE = "REQUESTED_SNOOZE";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final int SNOOZE_1_REQ_CODE = 1;
    public static final int SNOOZE_2_REQ_CODE = 2;
    public static final int SNOOZE_3_REQ_CODE = 3;

    public SnoozeService() {
        super(SnoozeService.class.toString());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        SharedPreferences sharedPrefs = App.getSharedPreferences();

        Alarm alarm = Parcels.unwrap(intent.getParcelableExtra(ALARM_PARAMETER));
        int snoozedTime;
        switch (intent.getIntExtra(REQUESTED_SNOOZE, -1)) {
            case SNOOZE_1_REQ_CODE: {
                snoozedTime = sharedPrefs.getInt(App.SNOOZE_1, DEFAULT_CUSTOM_TIME_1);
                break;
            }
            case SNOOZE_2_REQ_CODE: {
                snoozedTime = sharedPrefs.getInt(App.SNOOZE_2, DEFAULT_CUSTOM_TIME_2);
                break;
            }
            case SNOOZE_3_REQ_CODE: {
                snoozedTime = sharedPrefs.getInt(App.SNOOZE_3, DEFAULT_CUSTOM_TIME_3);
                break;
            }
            default:
                return;
        }

        LocalDateTime oldAlarmDT = alarm.getDateTime();
        alarm.setDateTime(oldAlarmDT.plusMinutes(snoozedTime));
        new ReminderEditionActivity.SaveAndUpdateWidgetAsyncTask(alarm, false, oldAlarmDT)
                .execute();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(NotificationService.NOTIFICATION, intent.getIntExtra(NOTIFICATION_ID, -1));
    }
}
