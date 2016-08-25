package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.joda.time.LocalTime;

/**
 * Created by ***REMOVED*** on 8/15/16.
 */
public class BucketWidgetConfigurationActivity extends AppCompatActivity {
    private int appWidgetId;

    FloatingActionButton saveFAB;

    Switch morningSwitch;
    View morningValueContainer;
    TextView morningValueEditor;
    Switch noonSwitch;
    View noonValueContainer;
    TextView noonValueEditor;
    Switch eveningSwitch;
    View eveningValueContainer;
    TextView eveningValueEditor;
    Switch nightSwitch;
    View nightValueContainer;
    TextView nightValueEditor;
    private TimeFieldHandler morningValueHandler;
    private TimeFieldHandler noonValueHandler;
    private TimeFieldHandler eveningValueHandler;
    private TimeFieldHandler nightValueHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bucket_widget_configuration_activity);

        // Set toolbar and navigation
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        morningSwitch = (Switch) findViewById(R.id.bucket_widget_morning_switch);
        morningValueContainer = findViewById(R.id.morning_value_container);
        morningValueEditor = (TextView) findViewById(R.id.morning_value_editor);
        morningSwitch.setOnCheckedChangeListener(new SwitchListener(morningValueContainer));
        morningValueHandler = new TimeFieldHandler(this, morningValueEditor,
                LocalTime.parse(sharedPreferences.getString(App.DEFAULT_MORNING_TIME, "08:00")));

        noonSwitch = (Switch) findViewById(R.id.bucket_widget_noon_switch);
        noonValueContainer = findViewById(R.id.noon_value_container);
        noonValueEditor = (TextView) findViewById(R.id.noon_value_editor);
        noonSwitch.setOnCheckedChangeListener(new SwitchListener(noonValueContainer));
        noonValueHandler = new TimeFieldHandler(this, noonValueEditor,
                LocalTime.parse(sharedPreferences.getString(App.DEFAULT_NOON_TIME, "12:00")));

        eveningSwitch = (Switch) findViewById(R.id.bucket_widget_evening_switch);
        eveningValueContainer = findViewById(R.id.evening_value_container);
        eveningValueEditor = (TextView) findViewById(R.id.evening_value_editor);
        eveningSwitch.setOnCheckedChangeListener(new SwitchListener(eveningValueContainer));
        eveningValueHandler = new TimeFieldHandler(this, eveningValueEditor,
                LocalTime.parse(sharedPreferences.getString(App.DEFAULT_EVENING_TIME, "18:00")));

        nightSwitch = (Switch) findViewById(R.id.bucket_widget_night_switch);
        nightValueContainer = findViewById(R.id.night_value_container);
        nightValueEditor = (TextView) findViewById(R.id.night_value_editor);
        nightSwitch.setOnCheckedChangeListener(new SwitchListener(nightValueContainer));
        nightValueHandler = new TimeFieldHandler(this, nightValueEditor,
                LocalTime.parse(sharedPreferences.getString(App.DEFAULT_NIGHT_TIME, "22:00")));

        saveFAB = (FloatingActionButton) findViewById(R.id.save_and_close);
        saveFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor widgetPrefEditor = App.getWidgetSharedPref(appWidgetId).edit();
                if (morningSwitch.isChecked()) {
                    widgetPrefEditor.putString(BucketWidgetProvider.MORNING_TIME, morningValueHandler.getCurrentValue().toString());
                } else {
                    widgetPrefEditor.remove(BucketWidgetProvider.MORNING_TIME);
                }
                if (eveningSwitch.isChecked()) {
                    widgetPrefEditor.putString(BucketWidgetProvider.EVENING_TIME, eveningValueHandler.getCurrentValue().toString());
                } else {
                    widgetPrefEditor.remove(BucketWidgetProvider.EVENING_TIME);
                }
                if (noonSwitch.isChecked()) {
                    widgetPrefEditor.putString(BucketWidgetProvider.NOON_TIME, noonValueHandler.getCurrentValue().toString());
                } else {
                    widgetPrefEditor.remove(BucketWidgetProvider.NOON_TIME);
                }
                if (nightSwitch.isChecked()) {
                    widgetPrefEditor.putString(BucketWidgetProvider.NIGHT_TIME, nightValueHandler.getCurrentValue().toString());
                } else {
                    widgetPrefEditor.remove(BucketWidgetProvider.NIGHT_TIME);
                }
                widgetPrefEditor.commit();

                // Update widget
                App.updateBucketWidget(appWidgetId);

                // Say the widget was created
                Intent resultValue = new Intent(App.getAppContext(), BucketWidgetProvider.class);
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

    private class SwitchListener implements CompoundButton.OnCheckedChangeListener {
        View containerView;
        public SwitchListener(View containerView) {
            this.containerView = containerView;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            containerView.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }
}
