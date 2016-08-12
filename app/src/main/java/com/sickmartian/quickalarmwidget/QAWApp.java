package com.sickmartian.quickalarmwidget;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/11/16.
 */
public class QAWApp extends Application {

    private static Context context;
    public static final String TIME_CHANGED_ALARM_SET = "TIME_CHANGED_ALARM_SET";
    public static DateTimeFormatter timeFormatter;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        JodaTimeAndroid.init(QAWApp.getAppContext());
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        timeFormatter = DateTimeFormat.shortTime();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!(sharedPreferences.getBoolean(TIME_CHANGED_ALARM_SET, false))) {
            Timber.d("Trying to set time sync alarm");
            TimeSyncReceiver.sendBroadcast(false);
        }
    }

    public static Context getAppContext() {
        return QAWApp.context;
    }

    static final int HALF_HOUR_MINUTES = 30;
    static final int HOUR_MINUTES = 60;

    public static LocalDateTime getInitialTime(boolean every30) {
        LocalDateTime dateTime = Utils.getNow();

        LocalDateTime initialTime;
        if (every30) {
            int minutes = dateTime.getMinuteOfHour();
            if (minutes >= HALF_HOUR_MINUTES) {
                initialTime = dateTime.plusHours(1)
                        .withMinuteOfHour(0)
                        .withSecondOfMinute(0)
                        .withMillisOfSecond(0);
            } else {
                initialTime = dateTime
                        .withMinuteOfHour(HALF_HOUR_MINUTES)
                        .withSecondOfMinute(0)
                        .withMillisOfSecond(0);
            }
        } else {
            initialTime = dateTime.plusHours(1)
                    .withMinuteOfHour(0)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0);
        }

        return initialTime;
    }
}
