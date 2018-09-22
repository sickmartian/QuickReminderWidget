package com.sickmartian.quickreminderwidget;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

/**
 * Created by Leo on 11/12/17.
 */

public class SnoozeService extends IntentService {

    public static final String ALARM_PARAMETER = "ALARM";
    public static final String REQUESTED_SNOOZE = "REQUESTED_SNOOZE";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    public SnoozeService() {
        super(SnoozeService.class.toString());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        // Dismiss notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(NotificationReceiver.NOTIFICATION, intent.getIntExtra(NOTIFICATION_ID, -1));

        // Set alarm
        int snoozedTime = intent.getIntExtra(REQUESTED_SNOOZE, -1);
        Alarm alarm = Parcels.unwrap(intent.getParcelableExtra(ALARM_PARAMETER));
        LocalDateTime oldAlarmDT = alarm.getDateTime();
        alarm.setDateTime(Utils.saneNowPlusDurationForAlarms(Duration.standardMinutes(snoozedTime)));
        new ReminderEditionActivity.SaveAndUpdateWidgetAsyncTask(alarm,
                false,
                oldAlarmDT)
                .execute();
    }
}
