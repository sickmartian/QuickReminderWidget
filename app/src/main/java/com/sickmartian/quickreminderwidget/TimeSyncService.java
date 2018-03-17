package com.sickmartian.quickreminderwidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import org.joda.time.LocalDateTime;

import timber.log.Timber;

public class TimeSyncService extends IntentService {
    private static final int REQUEST_CODE = 5656;
    public static final String AND_UPDATE_PRESENTATION = "AND_UPDATE_PRESENTATION";
    public static final String AND_DISABLE = "AND_DISABLE";

    public TimeSyncService() {
        super(TimeSyncService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Timber.d("TimeSyncService starting");
            LocalDateTime nextTime = App.getInitialTime(App.isThereOneEvery30());

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            assert alarmManager != null;

            Intent timeSyncIntent = new Intent(this, TimeSyncReceiver.class);
            timeSyncIntent.putExtra(TimeSyncService.AND_UPDATE_PRESENTATION, true);
            PendingIntent timeSyncIntentPI = PendingIntent.getBroadcast(this,
                    TimeSyncService.REQUEST_CODE, timeSyncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // If we are disabling, disable
            if (intent.getBooleanExtra(AND_DISABLE, !App.areThereWidgets())) {
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

            if (intent.getBooleanExtra(AND_UPDATE_PRESENTATION, false)) {
                Timber.i("Updating Widgets");
                App.updatePresentation();
            }
        } finally {
            Timber.d("TimeSyncService ending");
        }
    }
}
