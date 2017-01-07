package com.sickmartian.quickreminderwidget;

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
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class CustomValuesWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_IDS_KEY ="WIDGET_IDS_KEY";
    public static final String SHOW_PLUS = "SHOW_PLUS";
    private static final boolean DEFAULT_SHOW_PLUS = true;
    private static final int NUMBER_OF_BUTTONS = 3;

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

            // Get a widget
            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.custom_values_widget_layout);
            appWidgetManager.updateAppWidget(appWidgetId, widget);

            SharedPreferences widgetPrefs = App.getWidgetSharedPref(appWidgetId);

            // Set if there is at least one widget set to every30
            // so we know what a normal or custom alarm is and when we have
            // to manually update widgets
            boolean showPlus = widgetPrefs.getBoolean(SHOW_PLUS, DEFAULT_SHOW_PLUS);

            int augAppWidgetId = appWidgetId + 1;

            int customValue1 = widgetPrefs.getInt(CUSTOM_TIME_1, DEFAULT_CUSTOM_TIME_1);
            if (customValue1 != DISABLED_CUSTOM_TIME) {
                ReminderIntentionData currentReminderIntentionData = new ReminderIntentionData(Duration.standardMinutes(customValue1), null);
                widget.setOnClickPendingIntent(R.id.custom_value_1_button,
                        Utils.getPIInNewStack(ReminderEditionActivity.getIntentForEditionWithIntention(context,
                                currentReminderIntentionData, appWidgetId),
                                ( ( NUMBER_OF_BUTTONS * ( augAppWidgetId - 1 ) ) + 1 ) * -1));
                widget.setTextViewText(R.id.custom_value_1_button,
                        CustomAlarmTimeValue.getCustomValueShortLabel((int) currentReminderIntentionData.getDuration().getStandardMinutes()));
                widget.setTextColor(R.id.custom_value_1_button, App.inactiveColor);
                widget.setViewVisibility(R.id.custom_value_1_button, View.VISIBLE);
            } else {
                widget.setViewVisibility(R.id.custom_value_1_button, View.GONE);
            }

            int customValue2 = widgetPrefs.getInt(CUSTOM_TIME_2, DEFAULT_CUSTOM_TIME_2);
            if (customValue2 != DISABLED_CUSTOM_TIME) {
                ReminderIntentionData currentReminderIntentionData = new ReminderIntentionData(Duration.standardMinutes(customValue2), null);
                widget.setOnClickPendingIntent(R.id.custom_value_2_button,
                        Utils.getPIInNewStack(ReminderEditionActivity.getIntentForEditionWithIntention(context,
                                currentReminderIntentionData, appWidgetId),
                                ( ( NUMBER_OF_BUTTONS * ( augAppWidgetId - 1 ) ) + 2 ) * -1));
                widget.setTextViewText(R.id.custom_value_2_button,
                        CustomAlarmTimeValue.getCustomValueShortLabel((int) currentReminderIntentionData.getDuration().getStandardMinutes()));
                widget.setTextColor(R.id.custom_value_2_button, App.inactiveColor);
                widget.setViewVisibility(R.id.custom_value_2_button, View.VISIBLE);
            } else {
                widget.setViewVisibility(R.id.custom_value_2_button, View.GONE);
            }

            int customValue3 = widgetPrefs.getInt(CUSTOM_TIME_3, DEFAULT_CUSTOM_TIME_3);
            if (customValue3 != DISABLED_CUSTOM_TIME) {
                ReminderIntentionData currentReminderIntentionData = new ReminderIntentionData(Duration.standardMinutes(customValue3), null);
                widget.setOnClickPendingIntent(R.id.custom_value_3_button,
                        Utils.getPIInNewStack(ReminderEditionActivity.getIntentForEditionWithIntention(context,
                                currentReminderIntentionData, appWidgetId),
                                ( ( NUMBER_OF_BUTTONS * ( augAppWidgetId - 1 ) ) + 3 ) * -1));
                widget.setTextViewText(R.id.custom_value_3_button,
                        CustomAlarmTimeValue.getCustomValueShortLabel((int) currentReminderIntentionData.getDuration().getStandardMinutes()));
                widget.setTextColor(R.id.custom_value_3_button, App.inactiveColor);
                widget.setViewVisibility(R.id.custom_value_3_button, View.VISIBLE);
            } else {
                widget.setViewVisibility(R.id.custom_value_3_button, View.GONE);
            }

            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}
