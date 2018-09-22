package com.sickmartian.quickreminderwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.joda.time.LocalDateTime;

import timber.log.Timber;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by sickmartian on 8/11/16.
 */
public class TimeSyncReceiver extends BroadcastReceiver {
    public static final String AND_UPDATE_PRESENTATION = "AND_UPDATE_PRESENTATION";
    public static final String AND_DISABLE = "AND_DISABLE";
    static final int REQUEST_CODE = 5656;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Timber.d("TimeSync called via: " + intent.getAction());
            Timber.d("areThereWidgets:" + Boolean.toString(App.areThereWidgets()));
        }

        processTimeSync(context, processIntentWithSaneDefaults(context, intent));
    }

    private Intent processIntentWithSaneDefaults(Context context, Intent inputIntent) {
        // Intent might either come from system or from broadcast, we create a new one
        // if we don't get any, so we an use the defaults
        Intent intent = inputIntent == null ? new Intent() : inputIntent;
        Intent outputIntent = new Intent(context, TimeSyncReceiver.class);
        outputIntent.putExtra(AND_UPDATE_PRESENTATION,
                intent.getBooleanExtra(AND_UPDATE_PRESENTATION, true));
        outputIntent.putExtra(AND_DISABLE,
                intent.getBooleanExtra(AND_DISABLE, !App.areThereWidgets()));
        return outputIntent;
    }

    private void processTimeSync(Context context, Intent intent) {
        try {
            Timber.d("processTimeSync starting");
            LocalDateTime nextTime = App.getInitialTime(App.isThereOneEvery30());

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            assert alarmManager != null;

            Intent timeSyncIntent = new Intent(context, TimeSyncReceiver.class);
            timeSyncIntent.putExtra(TimeSyncReceiver.AND_UPDATE_PRESENTATION, true);
            PendingIntent timeSyncIntentPI = PendingIntent.getBroadcast(context,
                    TimeSyncReceiver.REQUEST_CODE, timeSyncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // If we are disabling, disable
            if (intent.getBooleanExtra(TimeSyncReceiver.AND_DISABLE, !App.areThereWidgets())) {
                Timber.i("Disabling TimeSync");
                alarmManager.cancel(timeSyncIntentPI);
            } else {
                // If not, we schedule the next call to this service
                Timber.i("Placing TimeSync alarm for: " + nextTime.toString());
                long nextMillis = Utils.convertLocalToDTSafely(nextTime).getMillis();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC, nextMillis, timeSyncIntentPI);
                } else {
                    alarmManager.set(AlarmManager.RTC, nextMillis, timeSyncIntentPI);
                }
            }

            if (intent.getBooleanExtra(TimeSyncReceiver.AND_UPDATE_PRESENTATION, false)) {
                Timber.i("Updating Widgets");
                App.updatePresentation();
            }
        } finally {
            Timber.d("processTimeSync ending");
        }
    }

    public static void sendBroadcast(boolean andUpdateWidgets, boolean andDisable) {
        Intent intent = new Intent(App.getAppContext(), TimeSyncReceiver.class);
        intent.putExtra(AND_UPDATE_PRESENTATION, andUpdateWidgets);
        intent.putExtra(AND_DISABLE, andDisable);
        App.getAppContext().sendBroadcast(intent);
    }
}
