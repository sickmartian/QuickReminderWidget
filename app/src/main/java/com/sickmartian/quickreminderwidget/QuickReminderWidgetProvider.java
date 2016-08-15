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
    public static final String CUSTOM_TIME_1 = "CUSTOM_TIME_1";
    public static final String CUSTOM_TIME_2 = "CUSTOM_TIME_2";
    public static final String CUSTOM_TIME_3 = "CUSTOM_TIME_3";
    public static final String EVERY_30 = "EVERY_30";
    public static final String HOURS = "HOURS";
    public static final String POSSIBILITY_TO_ADD_NOTE = "POSSIBILITY_TO_ADD_NOTE";

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
        QAWApp.setWidgetExist(true);
        TimeSyncReceiver.sendBroadcast(false, false);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Timber.d("TimeSync starting because of all widgets disabled");
        QAWApp.setWidgetExist(false);
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
            SharedPreferences sharedPreferences = getWidgetSharedPref(appWidgetId);
            int customTime = sharedPreferences.getInt(CUSTOM_TIME_1, 1);
            svcIntent.putExtra(CUSTOM_TIME_1, customTime);
            customTime = sharedPreferences.getInt(CUSTOM_TIME_2, 5);
            svcIntent.putExtra(CUSTOM_TIME_2, customTime);
            customTime = sharedPreferences.getInt(CUSTOM_TIME_3, 10);
            svcIntent.putExtra(CUSTOM_TIME_3, customTime);

            // Set if there is at least one widget set to every30
            // so we know what a normal or custom alarm is and when we have
            // to manually update widgets
            boolean every30 = sharedPreferences.getBoolean(EVERY_30, true);
            svcIntent.putExtra(EVERY_30, every30);
            if (every30) {
                oneEvery30 = true;
            }

            int hours = sharedPreferences.getInt(HOURS, 4);
            svcIntent.putExtra(HOURS, hours);
            boolean possibilityToAddNotes = sharedPreferences.getBoolean(POSSIBILITY_TO_ADD_NOTE, true);
            svcIntent.putExtra(POSSIBILITY_TO_ADD_NOTE, possibilityToAddNotes);

            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            widget.setRemoteAdapter(R.id.quick_widget_list, svcIntent);

            // Each row when pressed sends an alarm intention
            Intent clickIntent = new Intent(context, AlarmIntentionReceiver.class);
            PendingIntent clickPI = PendingIntent.getBroadcast(context, 0,
                    clickIntent, 0);
            widget.setPendingIntentTemplate(R.id.quick_widget_list, clickPI);

            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
        QAWApp.setOneEvery30(oneEvery30);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.quick_widget_list);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private static SharedPreferences getWidgetSharedPref(int appWidgetId) {
        return QAWApp.getAppContext().getSharedPreferences("WIDGET_" + appWidgetId,
                Context.MODE_APPEND);
    }

}
