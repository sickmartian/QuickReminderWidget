package com.sickmartian.quickreminderwidget;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/11/16.
 */
public class TimeSyncReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Timber.d("TimeSync called via: " + intent.getAction());
            Timber.d("areThereWidgets:" + Boolean.toString(App.areThereWidgets()));
        }
        Intent service = new Intent(context, TimeSyncService.class);
        if (intent != null) {
            service.putExtra(TimeSyncService.AND_UPDATE_WIDGETS,
                    intent.getBooleanExtra(TimeSyncService.AND_UPDATE_WIDGETS, true));
            service.putExtra(TimeSyncService.AND_DISABLE,
                    intent.getBooleanExtra(TimeSyncService.AND_DISABLE, !App.areThereWidgets()));
        } else {
            service.putExtra(TimeSyncService.AND_UPDATE_WIDGETS, true);
            service.putExtra(TimeSyncService.AND_DISABLE, !App.areThereWidgets());
        }
        startWakefulService(context, service);
    }

    public static void sendBroadcast(boolean andUpdateWidgets, boolean andDisable) {
        Intent intent = new Intent(App.getAppContext(), TimeSyncReceiver.class);
        intent.putExtra(TimeSyncService.AND_UPDATE_WIDGETS, andUpdateWidgets);
        intent.putExtra(TimeSyncService.AND_DISABLE, andDisable);
        App.getAppContext().sendBroadcast(intent);
    }

}
