package com.sickmartian.quickalarmwidget;

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
            Timber.d("Called via: " + intent.getAction());
        }
        Intent service = new Intent(context, TimeSyncService.class);
        if (intent != null) {
            service.putExtra(TimeSyncService.AND_UPDATE_WIDGETS,
                    intent.getBooleanExtra(TimeSyncService.AND_UPDATE_WIDGETS, true));
        } else {
            service.putExtra(TimeSyncService.AND_UPDATE_WIDGETS, true);
        }
        startWakefulService(context, service);
    }

    public static void sendBroadcast(boolean andUpdateWidgets) {
        Intent intent = new Intent(QAWApp.getAppContext(), TimeSyncReceiver.class);
        intent.putExtra(TimeSyncService.AND_UPDATE_WIDGETS, andUpdateWidgets);
        QAWApp.getAppContext().sendBroadcast(intent);
    }

}
