package com.sickmartian.quickreminderwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

/**
 * Created by sickmartian on 8/11/16.
 */
public class TimeSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Timber.d("TimeSync called via: " + intent.getAction());
            Timber.d("areThereWidgets:" + Boolean.toString(App.areThereWidgets()));
        }
        Intent service = new Intent(context, TimeSyncService.class);
        if (intent != null) {
            service.putExtra(TimeSyncService.AND_UPDATE_PRESENTATION,
                    intent.getBooleanExtra(TimeSyncService.AND_UPDATE_PRESENTATION, true));
            service.putExtra(TimeSyncService.AND_DISABLE,
                    intent.getBooleanExtra(TimeSyncService.AND_DISABLE, !App.areThereWidgets()));
        } else {
            service.putExtra(TimeSyncService.AND_UPDATE_PRESENTATION, true);
            service.putExtra(TimeSyncService.AND_DISABLE, !App.areThereWidgets());
        }

        context.startService(service);
    }

    public static void sendBroadcast(boolean andUpdateWidgets, boolean andDisable) {
        Intent intent = new Intent(App.getAppContext(), TimeSyncReceiver.class);
        intent.putExtra(TimeSyncService.AND_UPDATE_PRESENTATION, andUpdateWidgets);
        intent.putExtra(TimeSyncService.AND_DISABLE, andDisable);
        App.getAppContext().sendBroadcast(intent);
    }

}
