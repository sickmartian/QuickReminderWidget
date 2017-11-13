package com.sickmartian.quickreminderwidget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;

/**
 * Created by sickmartian on 8/15/16.
 */
public class QuickReminderWidgetConfigurationActivity extends AppCompatActivity {
    int appWidgetId;

    Toolbar toolbar;
    EditText hours;
    Switch every30;
    Switch notes;
    FloatingActionButton saveFAB;
    protected CustomAlarmTimeFragment customAlarmTimeFragment;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_widget_configuration_activity);

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

        hours = (EditText) findViewById(R.id.hours);
        every30 = (Switch) findViewById(R.id.every_30);
        notes = (Switch) findViewById(R.id.notes);
        saveFAB = (FloatingActionButton) findViewById(R.id.save_and_close);

        hours.setText(Integer.toString(QuickReminderWidgetProvider.DEFAULT_HOURS));
        every30.setChecked(QuickReminderWidgetProvider.DEFAULT_EVERY_30);
        notes.setChecked(QuickReminderWidgetProvider.DEFAULT_POSSIBILITY_TO_ADD_NOTE);

        saveFAB.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View view) {
                int hoursInt;
                if (hours.length() == 0) {
                    showErrorText(getString(R.string.widget_configuration_hour_missing));
                    return;
                } else {
                    try {
                        hoursInt = Integer.valueOf(hours.getText().toString());
                    } catch (Exception e) {
                        showErrorText(getString(R.string.widget_configuration_hour_invalid));
                        return;
                    }
                    if (hoursInt > 168) {
                        showErrorText(getString(R.string.widget_configuration_hour_above_max));
                        return;
                    }
                }

                CustomAlarmTimeFragment.Times times = customAlarmTimeFragment.getTimes();

                App.getWidgetSharedPref(appWidgetId)
                        .edit()
                        .putInt(QuickReminderWidgetProvider.HOURS, hoursInt)
                        .putBoolean(QuickReminderWidgetProvider.EVERY_30, every30.isChecked())
                        .putBoolean(QuickReminderWidgetProvider.POSSIBILITY_TO_ADD_NOTE, notes.isChecked())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_1, times.getV1())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_2, times.getV2())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_3, times.getV3())
                        .commit();

                // Update widget
                App.updateAllQuickReminderWidgets();

                // Say the widget was created
                Intent resultValue = new Intent(App.getAppContext(), QuickReminderWidgetProvider.class);
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

    protected void showErrorText(String errorText) {
        SpannableStringBuilder snackbarText = new SpannableStringBuilder();
        snackbarText.append(errorText).setSpan(new ForegroundColorSpan(
                        ActivityCompat.getColor(this, R.color.colorAccent)),
                0, errorText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSnackbar(snackbarText, Snackbar.LENGTH_LONG).show();
    }

    public Snackbar getSnackbar(CharSequence text, int duration) {
        CoordinatorLayout rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
        assert rootLayout != null;
        return Snackbar.make(rootLayout, text, duration);
    }

}
