package com.sickmartian.quickreminderwidget;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.stetho.Stetho;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/11/16.
 */
public class QAWApp extends Application {

    private static final String ARE_THERE_WIDGETS = "ARE_THERE_WIDGETS";
    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final int NOTIFICATION_ID_INITIAL_VALUE = -1;
    private static final String ARE_THERE_30M_WIDGETS = "ARE_THERE_30M_WIDGETS";
    private static Context context;
    public static DateTimeFormatter timeFormatter;
    public static DateTimeFormatter dateFormatter;
    public static DateTimeFormatter dateTimeFormatter;
    public static int activeColor = -1;
    public static int inactiveColor = -1;
    public static SharedPreferences sharedPreferences = null;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        JodaTimeAndroid.init(QAWApp.getAppContext());
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
            Timber.plant(new Timber.DebugTree());
        }

        dateFormatter = DateTimeFormat.mediumDate();
        timeFormatter = DateTimeFormat.shortTime();
        dateTimeFormatter = DateTimeFormat.shortDateTime();

        activeColor = QAWApp.getAppContext().getResources().getColor(R.color.activeColor);
        inactiveColor = QAWApp.getAppContext().getResources().getColor(R.color.inactiveColor);
    }

    public static boolean areThereWidgets() {
        return getSharedPreferences().getBoolean(ARE_THERE_WIDGETS, false);
    }

    private static SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(QAWApp.getAppContext());
        }
        return sharedPreferences;
    }

    public static void setWidgetExist(boolean atLeastOneWidgetExists) {
        Timber.d("setWidgetExist:" + Boolean.toString(atLeastOneWidgetExists));
        getSharedPreferences().edit().putBoolean(ARE_THERE_WIDGETS, atLeastOneWidgetExists).commit();
    }

    public static boolean isThereOneEvery30() {
        return getSharedPreferences().getBoolean(ARE_THERE_30M_WIDGETS, false);
    }

    public static void setOneEvery30(boolean oneEvery30) {
        Timber.d("setOneEvery30:" + Boolean.toString(oneEvery30));
        getSharedPreferences().edit().putBoolean(ARE_THERE_30M_WIDGETS, oneEvery30).commit();
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

    public static void updateAllWidgets() {
        Context appContext = QAWApp.getAppContext();
        AppWidgetManager man = AppWidgetManager.getInstance(appContext);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(appContext, QuickReminderWidgetProvider.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(getAppContext().getString(R.string.custom_widget_update_action));
        updateIntent.putExtra(QuickReminderWidgetProvider.WIDGET_IDS_KEY, ids);
        appContext.sendBroadcast(updateIntent);
    }

    public static int getNewNotificationId() {
        int notificationId = getSharedPreferences().getInt(NOTIFICATION_ID, NOTIFICATION_ID_INITIAL_VALUE);
        if (notificationId == Integer.MAX_VALUE) {
            notificationId = NOTIFICATION_ID_INITIAL_VALUE;
        }
        getSharedPreferences().edit().putInt(NOTIFICATION_ID, ++notificationId).commit();
        return notificationId;
    }
}
