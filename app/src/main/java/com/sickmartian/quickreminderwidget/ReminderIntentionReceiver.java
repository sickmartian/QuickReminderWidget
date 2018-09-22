package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import timber.log.Timber;

/**
 * Created by sickmartian on 8/12/16.
 */
public class ReminderIntentionReceiver extends BroadcastReceiver {
    public static final String ALARM_INTENTION_DATA = "ALARM_INTENTION_DATA";
    public static final String AND_OFFER_EDITION = "AND_OFFER_EDITION";

    public static Intent getIntentForIntention(Context context,
                                               ReminderIntentionData currentReminderIntentionData,
                                               boolean possibilityToAddNote,
                                               int appWidgetId, int requestCode) {
        // Fill each alarm's intention to the intents
        Intent intent = new Intent(context, ReminderIntentionReceiver.class);
        Bundle extras = new Bundle();
        extras.putParcelable(ReminderIntentionReceiver.ALARM_INTENTION_DATA,
                Parcels.wrap(currentReminderIntentionData));
        extras.putBoolean(ReminderIntentionReceiver.AND_OFFER_EDITION, possibilityToAddNote);
        extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        extras.putInt("RequestCode", requestCode);
        intent.putExtras(extras);
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final ReminderIntentionData intentionData =
                Parcels.unwrap(intent.getParcelableExtra(ReminderIntentionReceiver.ALARM_INTENTION_DATA));

        Timber.d("Received alarm intention: " + intentionData.toString());

        if (intentionData.getAlarm() == null) {
            LocalDateTime alarmTime = null;
            if (intentionData.getTime() != null) {
                alarmTime = intentionData.getTime();
            } else if (intentionData.getDuration() != null) {
                alarmTime = Utils.saneNowPlusDurationForAlarms(intentionData.getDuration());
            }
            assert alarmTime != null; // Either a duration or a time, if none something is really fishy

            if (!DateTimeZone.getDefault().isLocalDateTimeGap(alarmTime)) {
                Alarm newAlarm = Alarm.fromDateTime(alarmTime);
                boolean created = newAlarm.createSync();

                if (created) {
                    if (intent.getBooleanExtra(ReminderIntentionReceiver.AND_OFFER_EDITION, false)) {
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getAppContext());
                        stackBuilder.addParentStack(ReminderEditionActivity.class);
                        stackBuilder.addNextIntent(ReminderEditionActivity.getIntentForEditionOfJustCreatedAlarm(newAlarm));
                        Intent newActivityIntent = stackBuilder.getIntents()[0];
                        context.startActivity(newActivityIntent);
                    } else {
                        Utils.toastTo(Utils.getFormattedMessageForDate(alarmTime, R.string.reminder_created_for));
                    }
                } else {
                    Utils.toastTo(App.getAppContext().getString(R.string.reminder_not_created_exists));
                }
            } else {
                Utils.toastTo(App.getAppContext().getString(R.string.alarm_falls_into_dst));
            }
        } else {
            intentionData.getAlarm().deleteSync();
            Utils.toastTo(Utils.getFormattedMessageForDate(intentionData.getTime(), R.string.reminder_deleted_for));
        }

        App.updatePresentation();
        CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
    }
}
