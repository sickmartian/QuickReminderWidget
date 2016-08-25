package com.sickmartian.quickreminderwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class QuickReminderWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_IDS_KEY ="WIDGET_IDS_KEY";
    // Settings
    public static final String CUSTOM_TIME_1 = "CUSTOM_TIME_1";
    public static final String CUSTOM_TIME_2 = "CUSTOM_TIME_2";
    public static final String CUSTOM_TIME_3 = "CUSTOM_TIME_3";
    public static final String EVERY_30 = "EVERY_30";
    public static final String HOURS = "HOURS";
    public static final String POSSIBILITY_TO_ADD_NOTE = "POSSIBILITY_TO_ADD_NOTE";
    // Custom notification settings
    public static final String CUSTOM_NOTIFICATION = "CUSTOM_NOTIFICATION";
    public static final String CUSTOM_NOTIFICATION_VIBRATE = "CUSTOM_NOTIFICATION_VIBRATE";
    public static final String CUSTOM_NOTIFICATION_LIGHTS = "CUSTOM_NOTIFICATION_LIGHTS";
    public static final String CUSTOM_NOTIFICATION_SOUND = "CUSTOM_NOTIFICATION_SOUND";
    // Default values
    public static final boolean DEFAULT_EVERY_30 = true;
    public static final int DISABLED_CUSTOM_TIME = -1;
    public static final int DEFAULT_CUSTOM_TIME_1 = 5;
    public static final int DEFAULT_CUSTOM_TIME_2 = 15;
    public static final int DEFAULT_CUSTOM_TIME_3 = DISABLED_CUSTOM_TIME;
    public static final boolean DEFAULT_POSSIBILITY_TO_ADD_NOTE = true;
    public static final int DEFAULT_HOURS = 24;
    public static final boolean DEFAULT_CUSTOM_NOTIFICATION = false;
    public static final boolean DEFAULT_VIBRATE = true;
    public static final boolean DEFAULT_LIGHT = true;

    public static final String EDITION_MODE = "EDITION_MODE";

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
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Timber.d("TimeSync starting because of first widget enabled");
        App.setWidgetExist(true);
        TimeSyncReceiver.sendBroadcast(false, false);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Timber.d("TimeSync starting because of all widgets disabled");
        App.setWidgetExist(false);
        TimeSyncReceiver.sendBroadcast(false, true);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        boolean oneEvery30 = false;
        for (int appWidgetId : appWidgetIds) {
            // Get a widget
            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.quick_widget_layout);

            // Intent for the service that updates the list
            Intent svcIntent = new Intent(context, QuickReminderWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            // Get preferences for the widget
            SharedPreferences sharedPreferences = App.getWidgetSharedPref(appWidgetId);
            int customTime = sharedPreferences.getInt(CUSTOM_TIME_1, DEFAULT_CUSTOM_TIME_1);
            svcIntent.putExtra(CUSTOM_TIME_1, customTime);
            customTime = sharedPreferences.getInt(CUSTOM_TIME_2, DEFAULT_CUSTOM_TIME_2);
            svcIntent.putExtra(CUSTOM_TIME_2, customTime);
            customTime = sharedPreferences.getInt(CUSTOM_TIME_3, DEFAULT_CUSTOM_TIME_3);
            svcIntent.putExtra(CUSTOM_TIME_3, customTime);

            // Set if there is at least one widget set to every30
            // so we know what a normal or custom alarm is and when we have
            // to manually update widgets
            boolean every30 = sharedPreferences.getBoolean(EVERY_30, DEFAULT_EVERY_30);
            svcIntent.putExtra(EVERY_30, every30);
            if (every30) {
                oneEvery30 = true;
            }

            int hours = sharedPreferences.getInt(HOURS, DEFAULT_HOURS);
            svcIntent.putExtra(HOURS, hours);
            boolean possibilityToAddNotes = sharedPreferences.getBoolean(POSSIBILITY_TO_ADD_NOTE, DEFAULT_POSSIBILITY_TO_ADD_NOTE);
            svcIntent.putExtra(POSSIBILITY_TO_ADD_NOTE, possibilityToAddNotes);

            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            widget.setRemoteAdapter(R.id.quick_widget_list, svcIntent);

            // Edition mode
            boolean editionMode = sharedPreferences.getBoolean(EDITION_MODE, false);
            if (!editionMode) {
                // Each row when pressed sends an alarm intention
                Intent clickIntent = new Intent(context, ReminderIntentionReceiver.class);
                PendingIntent clickPI = PendingIntent.getBroadcast(context, 6666,
                        clickIntent, 0);
                widget.setPendingIntentTemplate(R.id.quick_widget_list, clickPI);

                widget.setInt(R.id.toggle_edit_mode, "setColorFilter", App.inactiveColor);
            } else {
                // Each row triggers edition
                PendingIntent clickPI = PendingIntent.getActivity(context, 7777,
                        ReminderEditionActivity.getIntentForEditionPart1(context), 0);
                widget.setPendingIntentTemplate(R.id.quick_widget_list, clickPI);

                widget.setInt(R.id.toggle_edit_mode, "setColorFilter", App.activeColor);
            }

            widget.setOnClickPendingIntent(R.id.add_custom_reminder, PendingIntent.getActivity(context,
                    appWidgetId, ReminderEditionActivity.getIntentForCreation(), PendingIntent.FLAG_CANCEL_CURRENT));
            widget.setOnClickPendingIntent(R.id.toggle_edit_mode, PendingIntent.getBroadcast(context,
                    appWidgetId, EditionModeToggleReceiver.getIntent(appWidgetId, editionMode), PendingIntent.FLAG_CANCEL_CURRENT));

            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
        App.setOneEvery30(oneEvery30);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.quick_widget_list);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}
