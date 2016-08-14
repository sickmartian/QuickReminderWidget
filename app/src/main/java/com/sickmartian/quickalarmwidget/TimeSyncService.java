package com.sickmartian.quickalarmwidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/11/16.
 */
public class TimeSyncService extends IntentService {
    private static final int REQUEST_CODE = 5656;
    public static final String AND_UPDATE_WIDGETS = "AND_UPDATE_WIDGETS";
    public static final String AND_DISABLE = "AND_DISABLE";

    public TimeSyncService() {
        super(TimeSyncService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Timber.d("TimeSyncService starting");
            LocalDateTime nextTime = QAWApp.getInitialTime(QAWApp.isThereOneEvery30);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent timeSyncIntent = new Intent(this, TimeSyncReceiver.class);
            timeSyncIntent.putExtra(TimeSyncService.AND_UPDATE_WIDGETS, true);
            PendingIntent timeSyncIntentPI = PendingIntent.getBroadcast(this,
                    TimeSyncService.REQUEST_CODE, timeSyncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // If we are not disabling, we get next one
            if (!intent.getBooleanExtra(AND_DISABLE, false)) {
                Timber.i("Placing TimeSync alarm for: " + nextTime.toString());
                alarmManager.set(AlarmManager.RTC, nextTime.toDateTime().getMillis(),
                        timeSyncIntentPI);
            } else {
                Timber.i("Disabling TimeSync");
                alarmManager.cancel(timeSyncIntentPI);
            }

            if (intent.getBooleanExtra(AND_UPDATE_WIDGETS, false)) {
                Timber.i("Updating Widgets");
                QAWApp.updateAllWidgets();
            }
        } finally {
            Timber.d("TimeSyncService ending");
            TimeSyncReceiver.completeWakefulIntent(intent);
        }
    }
}
