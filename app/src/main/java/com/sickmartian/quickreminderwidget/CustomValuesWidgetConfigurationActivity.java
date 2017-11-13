package com.sickmartian.quickreminderwidget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;

/**
 * Created by sickmartian on 1/7/17.
 */

public class CustomValuesWidgetConfigurationActivity extends AppCompatActivity {

    int appWidgetId;

    Toolbar toolbar;
    Switch notes;
    FloatingActionButton saveFAB;
    protected CustomAlarmTimeFragment customAlarmTimeFragment;

    private static final boolean DEFAULT_SHOW_PLUS = true;
    private Switch showPlus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_values_widget_configuration_activity);

        if (savedInstanceState == null) {
            customAlarmTimeFragment = CustomAlarmTimeFragment.getInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.custom_alarm_fragment_container,
                    customAlarmTimeFragment,
                    CustomAlarmTimeFragment.class.toString()
            ).commit();
        } else {
            customAlarmTimeFragment = (CustomAlarmTimeFragment) getSupportFragmentManager()
                    .findFragmentByTag(CustomAlarmTimeFragment.class.toString());
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set toolbar and navigation
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.widget_configuration_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.show_plus_container).setVisibility(View.VISIBLE);
        showPlus = (Switch) findViewById(R.id.show_plus);
        showPlus.setChecked(DEFAULT_SHOW_PLUS);

        notes = (Switch) findViewById(R.id.notes);
        notes.setChecked(QuickReminderWidgetProvider.DEFAULT_POSSIBILITY_TO_ADD_NOTE);

        saveFAB = (FloatingActionButton) findViewById(R.id.save_and_close);
        saveFAB.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View view) {
                CustomAlarmTimeFragment.Times times = customAlarmTimeFragment.getTimes();

                App.getWidgetSharedPref(appWidgetId)
                        .edit()
                        .putBoolean(CustomValuesWidgetProvider.SHOW_PLUS, showPlus.isChecked())
                        .putBoolean(QuickReminderWidgetProvider.POSSIBILITY_TO_ADD_NOTE, notes.isChecked())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_1, times.getV1())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_2, times.getV2())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_3, times.getV3())
                        .commit();

                // Update widget
                App.updateCustomValuesWidget(appWidgetId);

                // Say the widget was created
                Intent resultValue = new Intent(App.getAppContext(), CustomValuesWidgetProvider.class);
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Say the widget wasn't created by default
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);
    }

    public Snackbar getSnackbar(CharSequence text, int duration) {
        CoordinatorLayout rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
        assert rootLayout != null;
        return Snackbar.make(rootLayout, text, duration);
    }
}
