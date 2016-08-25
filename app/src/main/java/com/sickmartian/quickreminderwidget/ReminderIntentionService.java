package com.sickmartian.quickreminderwidget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/14/16.
 */
public class ReminderIntentionService extends IntentService {
    public ReminderIntentionService() {
        super(ReminderIntentionService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final ReminderIntentionData intentionData =
                    Parcels.unwrap(intent.getParcelableExtra(ReminderIntentionReceiver.ALARM_INTENTION_DATA));

            Timber.d("Received alarm intention: " + intentionData.toString());

            if (intentionData.getAlarm() == null) {
                LocalDateTime alarmTime = null;
                if (intentionData.getTime() != null) {
                    alarmTime = intentionData.getTime();
                } else if (intentionData.getDuration() != null) {
                    alarmTime = Utils.getNow().plus(intentionData.getDuration())
                            .withSecondOfMinute(0)
                            .withMillisOfSecond(0);
                }
                assert alarmTime != null; // Either a duration or a time, if none something is really fishy
                Alarm newAlarm = Alarm.fromDateTime(alarmTime);
                int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                newAlarm.setSourceWidgetId(widgetId);
                boolean created = newAlarm.createSync();

                if (created) {
                    if (intent.getBooleanExtra(ReminderIntentionReceiver.AND_OFFER_EDITION, false)) {
                        startActivity(ReminderEditionActivity.getIntentForEditionOfJustCreatedAlarm(newAlarm));
                    } else {
                        final LocalDateTime finalAlarmTime = alarmTime;
                        Utils.toastTo(String.format(App.getAppContext().getString(R.string.reminder_created_for),
                                finalAlarmTime.toString(App.dateTimeFormatter)));
                    }
                } else {
                    Utils.toastTo(App.getAppContext().getString(R.string.reminder_not_created_exists));
                }
            } else {
                intentionData.getAlarm().deleteSync();
                Utils.toastTo(String.format(App.getAppContext().getString(R.string.reminder_deleted_for),
                        intentionData.getTime().toString(App.dateTimeFormatter)));
            }

            App.updateAllQuickReminderWidgets();
            CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
        } finally {
            ReminderIntentionReceiver.completeWakefulIntent(intent);
        }

    }

}
