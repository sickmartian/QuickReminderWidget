package com.sickmartian.quickreminderwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import org.joda.time.Duration;

import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.CUSTOM_TIME_1;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.CUSTOM_TIME_2;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.CUSTOM_TIME_3;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_1;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_2;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_3;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_POSSIBILITY_TO_ADD_NOTE;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.POSSIBILITY_TO_ADD_NOTE;

/**
 * Created by sickmartian on 8/9/16.
 */
public class CustomValuesWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_IDS_KEY ="WIDGET_IDS_KEY";
    public static final String SHOW_PLUS = "SHOW_PLUS";
    private static final boolean DEFAULT_SHOW_PLUS = true;
    private static final int NUMBER_OF_BUTTONS = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            //noinspection ConstantConditions
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            // Get a widget
            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.custom_values_widget_layout);
            appWidgetManager.updateAppWidget(appWidgetId, widget);

            SharedPreferences widgetPrefs = App.getWidgetSharedPref(appWidgetId);

            boolean showPlus = widgetPrefs.getBoolean(SHOW_PLUS, DEFAULT_SHOW_PLUS);
            if (!showPlus) {
                widget.setViewVisibility(R.id.add_custom_reminder, View.GONE);
            } else {
                widget.setOnClickPendingIntent(R.id.add_custom_reminder,
                        Utils.getPIInNewStack(ReminderEditionActivity.getIntentForCreation(), appWidgetId));
                widget.setViewVisibility(R.id.add_custom_reminder, View.VISIBLE);
            }

            boolean possibilityToAddNote = widgetPrefs.getBoolean(POSSIBILITY_TO_ADD_NOTE,
                    DEFAULT_POSSIBILITY_TO_ADD_NOTE);

            // Helper to calculate notification id
            int customValue1 = widgetPrefs.getInt(CUSTOM_TIME_1, DEFAULT_CUSTOM_TIME_1);
            setupButton(context, widget, possibilityToAddNote, appWidgetId,
                    1, R.id.custom_value_1_button, customValue1);

            int customValue2 = widgetPrefs.getInt(CUSTOM_TIME_2, DEFAULT_CUSTOM_TIME_2);
            setupButton(context, widget, possibilityToAddNote, appWidgetId,
                    2, R.id.custom_value_2_button, customValue2);

            int customValue3 = widgetPrefs.getInt(CUSTOM_TIME_3, DEFAULT_CUSTOM_TIME_3);
            setupButton(context, widget, possibilityToAddNote, appWidgetId,
                    3, R.id.custom_value_3_button, customValue3);

            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void setupButton(Context context, RemoteViews widget, boolean possibilityToAddNote, int appWidgetId,
                             int buttonCountInWidget, int buttonControlId, int customValueForTime) {
        if (customValueForTime != DISABLED_CUSTOM_TIME) {
            // Get parameter to schedule alarm
            ReminderIntentionData currentReminderIntentionData = new ReminderIntentionData(Duration.standardMinutes(customValueForTime), null);
            // Unique request code for this widget button
            int augAppWidgetId = appWidgetId + 1;
            int requestCode = ( ( ( NUMBER_OF_BUTTONS * ( augAppWidgetId - 1 ) ) + buttonCountInWidget ) * -1);
            // Setup pending intent for action
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    requestCode, ReminderIntentionReceiver.getIntentForIntention(context,
                            currentReminderIntentionData, possibilityToAddNote, appWidgetId, requestCode),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(buttonControlId, pendingIntent);

            // Layout
            widget.setTextViewText(buttonControlId,
                    CustomAlarmTimeValue.getCustomValueShortLabel((int) currentReminderIntentionData.getDuration().getStandardMinutes()));
            widget.setTextColor(buttonControlId, App.inactiveColor);
            widget.setViewVisibility(buttonControlId, View.VISIBLE);
        } else {
            widget.setViewVisibility(buttonControlId, View.GONE);
        }
    }

}
