package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by sickmartian on 8/12/16.
 */
public class EditionModeToggleReceiver extends BroadcastReceiver {
    private static final String CURRENT_EDITION_MODE = "CURRENT_EDITION_MODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        boolean currentEditionMode = intent.getBooleanExtra(CURRENT_EDITION_MODE, false);
        SharedPreferences widgetPrefs = App.getWidgetSharedPref(widgetId);
        widgetPrefs
                .edit()
                .putBoolean(QuickReminderWidgetProvider.EDITION_MODE, !currentEditionMode)
                .commit();
        App.updateQuickReminderWidget(widgetId);
    }

    public static Intent getIntent(int widgetId, boolean currentEditionMode) {
        Intent intent = new Intent(App.getAppContext(), EditionModeToggleReceiver.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(CURRENT_EDITION_MODE, currentEditionMode);
        return intent;
    }
}
