package com.sickmartian.quickreminderwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import org.joda.time.LocalTime;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class BucketWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_IDS_KEY ="WIDGET_IDS_KEY";
    int BASE_ADD = 1000000000;

    public static final String MORNING_TIME = "MORNING_TIME";
    public static final String NOON_TIME = "NOON_TIME";
    public static final String EVENING_TIME = "EVENING_TIME";
    public static final String NIGHT_TIME = "NIGHT_TIME";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            int baseAddId = BASE_ADD + appWidgetId * 1000;

            // Get a widget
            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.bucket_widget_layout);
            appWidgetManager.updateAppWidget(appWidgetId, widget);

            SharedPreferences widgetPrefs = App.getWidgetSharedPref(appWidgetId);
            if (widgetPrefs.contains(MORNING_TIME)) {
                LocalTime morningTime = LocalTime.parse(widgetPrefs.getString(MORNING_TIME, "OMG_Crash"));
                widget.setOnClickPendingIntent(R.id.bucket_morning,
                        PendingIntent.getActivity(context,
                            baseAddId + morningTime.getHourOfDay() * 60 + morningTime.getMinuteOfHour(),
                            ReminderEditionActivity.getIntentForCreationWithTime(morningTime),
                            PendingIntent.FLAG_CANCEL_CURRENT));
                widget.setViewVisibility(R.id.bucket_morning, View.VISIBLE);
            } else {
                widget.setViewVisibility(R.id.bucket_morning, View.GONE);
            }

            if (widgetPrefs.contains(EVENING_TIME)) {
                LocalTime eveningTime = LocalTime.parse(widgetPrefs.getString(EVENING_TIME, "OMG_Crash"));
                widget.setOnClickPendingIntent(R.id.bucket_evening,
                        PendingIntent.getActivity(context,
                                baseAddId + eveningTime.getHourOfDay() * 60 + eveningTime.getMinuteOfHour(),
                                ReminderEditionActivity.getIntentForCreationWithTime(eveningTime),
                                PendingIntent.FLAG_CANCEL_CURRENT));
                widget.setViewVisibility(R.id.bucket_evening, View.VISIBLE);
            } else {
                widget.setViewVisibility(R.id.bucket_evening, View.GONE);
            }

            if (widgetPrefs.contains(NOON_TIME)) {
                LocalTime noonTime = LocalTime.parse(widgetPrefs.getString(NOON_TIME, "OMG_Crash"));
                widget.setOnClickPendingIntent(R.id.bucket_noon,
                        PendingIntent.getActivity(context,
                                baseAddId + noonTime.getHourOfDay() * 60 + noonTime.getMinuteOfHour(),
                                ReminderEditionActivity.getIntentForCreationWithTime(noonTime),
                                PendingIntent.FLAG_CANCEL_CURRENT));
                widget.setViewVisibility(R.id.bucket_noon, View.VISIBLE);
            } else {
                widget.setViewVisibility(R.id.bucket_noon, View.GONE);
            }

            if (widgetPrefs.contains(NIGHT_TIME)) {
                LocalTime nightTime = LocalTime.parse(widgetPrefs.getString(NIGHT_TIME, "OMG_Crash"));
                widget.setOnClickPendingIntent(R.id.bucket_night,
                        PendingIntent.getActivity(context,
                                baseAddId + nightTime.getHourOfDay() * 60 + nightTime.getMinuteOfHour(),
                                ReminderEditionActivity.getIntentForCreationWithTime(nightTime),
                                PendingIntent.FLAG_CANCEL_CURRENT));
                widget.setViewVisibility(R.id.bucket_night, View.VISIBLE);
            } else {
                widget.setViewVisibility(R.id.bucket_night, View.GONE);
            }

            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}
