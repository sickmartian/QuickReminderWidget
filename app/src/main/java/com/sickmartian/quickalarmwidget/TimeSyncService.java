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

    public TimeSyncService() {
        super(TimeSyncService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Timber.d("TimeSyncService starting");

            LocalDateTime nextTime = QAWApp.getInitialTime(true);
            Timber.d("Placing TimeSync alarm for: " + nextTime.toString());

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent timeSyncIntent = new Intent(this, TimeSyncReceiver.class);
            timeSyncIntent.putExtra(TimeSyncService.AND_UPDATE_WIDGETS, true);
            PendingIntent timeSyncIntentPI = PendingIntent.getBroadcast(this,
                    TimeSyncService.REQUEST_CODE, timeSyncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC, nextTime.toDateTime().getMillis(),
                    timeSyncIntentPI);

            if (intent.getBooleanExtra(AND_UPDATE_WIDGETS, false)) {
                QAWApp.updateAllWidgets();
            }
        } finally {
            Timber.d("TimeSyncService ending");
            TimeSyncReceiver.completeWakefulIntent(intent);
        }
    }
}
