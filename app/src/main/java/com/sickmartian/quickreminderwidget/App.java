package com.sickmartian.quickreminderwidget;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;

import io.fabric.sdk.android.Fabric;
import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/11/16.
 */
public class App extends Application {
    public static final String CUSTOM_NOTIFICATION = "CUSTOM_NOTIFICATION";
    public static final String CUSTOM_NOTIFICATION_VIBRATE = "CUSTOM_NOTIFICATION_VIBRATE";
    public static final String CUSTOM_NOTIFICATION_LIGHTS = "CUSTOM_NOTIFICATION_LIGHTS";
    public static final String CUSTOM_NOTIFICATION_SOUND = "CUSTOM_NOTIFICATION_SOUND";

    public static final boolean DEFAULT_CUSTOM_NOTIFICATION = false;
    public static final boolean DEFAULT_VIBRATE = true;
    public static final boolean DEFAULT_LIGHT = true;
    public static final String DEFAULT_MORNING_TIME = "DEFAULT_MORNING_TIME";
    public static final String DEFAULT_NOON_TIME = "DEFAULT_NOON_TIME";
    public static final String DEFAULT_EVENING_TIME = "DEFAULT_EVENING_TIME";
    public static final String DEFAULT_NIGHT_TIME = "DEFAULT_NIGHT_TIME";

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

        JodaTimeAndroid.init(App.getAppContext());
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(App.getAppContext(), new Crashlytics());
            Timber.plant(new CrashlyticsTree());
        }

        dateFormatter = DateTimeFormat.mediumDate();
        timeFormatter = DateTimeFormat.shortTime();
        dateTimeFormatter = DateTimeFormat.shortDateTime();

        activeColor = App.getAppContext().getResources().getColor(R.color.activeColor);
        inactiveColor = App.getAppContext().getResources().getColor(R.color.inactiveColor);
    }

    public class CrashlyticsTree extends Timber.Tree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
            // Only for DEV
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            // Only add it as information so we have more context in case we get an error later.
            if ((priority == Log.INFO) && message != null) {
                Crashlytics.log(message);
                return;
            }

            // Errors, warnings, etc... might get more context still in the future
            Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
            Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
            Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

            // Log crash as exception
            if (t == null) {
                Crashlytics.logException(new Exception(message));
            } else {
                Crashlytics.logException(t);
            }
        }
    }

    public static boolean areThereWidgets() {
        return getSharedPreferences().getBoolean(ARE_THERE_WIDGETS, false);
    }

    public static SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
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
        return App.context;
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

    public static void updateBucketWidget(int widgetId) {
        Context appContext = App.getAppContext();
        int[] ids = {widgetId};
        Intent updateIntent = new Intent();
        updateIntent.setAction(getAppContext().getString(R.string.custom_bucket_widget_update_action));
        updateIntent.putExtra(BucketWidgetProvider.WIDGET_IDS_KEY, ids);
        appContext.sendBroadcast(updateIntent);
    }

    public static void updateQuickReminderWidget(int widgetId) {
        Context appContext = App.getAppContext();
        int[] ids = {widgetId};
        Intent updateIntent = new Intent();
        updateIntent.setAction(getAppContext().getString(R.string.custom_widget_update_action));
        updateIntent.putExtra(QuickReminderWidgetProvider.WIDGET_IDS_KEY, ids);
        appContext.sendBroadcast(updateIntent);
    }

    public static void updateAllQuickReminderWidgets() {
        Context appContext = App.getAppContext();
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

    public static SharedPreferences getWidgetSharedPref(int appWidgetId) {
        return App.getAppContext().getSharedPreferences("WIDGET_" + appWidgetId,
                Context.MODE_APPEND);
    }
}
