package com.sickmartian.quickreminderwidget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import timber.log.Timber;

/**
 * Created by sickmartian on 8/14/16.
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
                boolean created = newAlarm.createSync();

                if (created) {
                    if (intent.getBooleanExtra(ReminderIntentionReceiver.AND_OFFER_EDITION, false)) {
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getAppContext());
                        stackBuilder.addParentStack(ReminderEditionActivity.class);
                        stackBuilder.addNextIntent(ReminderEditionActivity.getIntentForEditionOfJustCreatedAlarm(newAlarm));
                        Intent newActivityIntent = stackBuilder.getIntents()[0];
                        startActivity(newActivityIntent);
                    } else {
                        Utils.toastTo(Utils.getRemindedCreatedForMessage(alarmTime));
                    }
                } else {
                    Utils.toastTo(App.getAppContext().getString(R.string.reminder_not_created_exists));
                }
            } else {
                intentionData.getAlarm().deleteSync();
                Utils.toastTo(Utils.getRemindedDeletedForMessage(intentionData.getTime()));
            }

            App.updateAllQuickReminderWidgets();
            CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
        } finally {
            ReminderIntentionReceiver.completeWakefulIntent(intent);
        }

    }

}
