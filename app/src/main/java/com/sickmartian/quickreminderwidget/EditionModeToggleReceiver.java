package com.sickmartian.quickreminderwidget;

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class EditionModeToggleReceiver extends BroadcastReceiver {
    private static final String CURRENT_EDITION_MODE = "CURRENT_EDITION_MODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        boolean currentEditionMode = intent.getBooleanExtra(CURRENT_EDITION_MODE, false);
        SharedPreferences widgetPrefs = QuickReminderWidgetProvider.getWidgetSharedPref(widgetId);
        widgetPrefs
                .edit()
                .putBoolean(QuickReminderWidgetProvider.EDITION_MODE, !currentEditionMode)
                .commit();
        QRWApp.updateWidget(widgetId);
    }

    public static Intent getIntent(int widgetId, boolean currentEditionMode) {
        Intent intent = new Intent(QRWApp.getAppContext(), EditionModeToggleReceiver.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(CURRENT_EDITION_MODE, currentEditionMode);
        return intent;
    }
}
