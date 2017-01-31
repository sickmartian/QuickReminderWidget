package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.parceler.Parcels;

/**
 * Created by sickmartian on 8/12/16.
 */
public class ReminderIntentionReceiver extends WakefulBroadcastReceiver {
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
        Intent serviceIntent = new Intent(context, ReminderIntentionService.class);
        serviceIntent.putExtras(intent);
        startWakefulService(context, serviceIntent);
    }
}
