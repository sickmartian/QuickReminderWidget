package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Switch;

import static android.view.View.GONE;

/**
 * Created by ***REMOVED*** on 1/7/17.
 */

public class CustomValuesWidgetConfigurationActivity extends QuickReminderWidgetConfigurationActivity {

    private static final boolean DEFAULT_SHOW_PLUS = true;
    private Switch showPlus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.hour_container).setVisibility(GONE);
        findViewById(R.id.every_30_container).setVisibility(GONE);

        findViewById(R.id.show_plus_container).setVisibility(View.VISIBLE);
        showPlus = (Switch) findViewById(R.id.show_plus);
        showPlus.setChecked(DEFAULT_SHOW_PLUS);
    }

    protected View.OnClickListener getFABSaveListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getWidgetSharedPref(appWidgetId)
                        .edit()
                        .putBoolean(CustomValuesWidgetProvider.SHOW_PLUS, showPlus.isChecked())
                        .putBoolean(QuickReminderWidgetProvider.POSSIBILITY_TO_ADD_NOTE, notes.isChecked())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_1, customValue1.getValue())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_2, customValue2.getValue())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_3, customValue3.getValue())
                        .commit();

                // Update widget
                App.updateCustomValuesWidget(appWidgetId);

                // Say the widget was created
                Intent resultValue = new Intent(App.getAppContext(), CustomValuesWidgetProvider.class);
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        };
    }
}
