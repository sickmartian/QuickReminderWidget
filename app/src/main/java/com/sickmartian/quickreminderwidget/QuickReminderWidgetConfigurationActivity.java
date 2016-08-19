package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by ***REMOVED*** on 8/15/16.
 */
public class QuickReminderWidgetConfigurationActivity extends AppCompatActivity {
    private static final int NONE_INDEX = 0;
    private static final int SOUND_REQUEST_CODE = 3443;
    private int appWidgetId;

    int[] customValueCorrespondingValues = new int[]{
            QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME, 1, 5, 15, 30, 45, 60, 90
    };
    int[] customValueCorrespondingNames = new int[]{
            R.string.custom_values_disabled_label,
            R.string.custom_values_1_label,
            R.string.custom_values_5_label,
            R.string.custom_values_15_label,
            R.string.custom_values_30_label,
            R.string.custom_values_45_label,
            R.string.custom_values_60_label,
            R.string.custom_values_90_label,
    };

    Toolbar toolbar;
    EditText hours;
    Switch every30;
    Switch notes;
    Button customValue1Button;
    Button customValue2Button;
    Button customValue3Button;
    FloatingActionButton saveFAB;
    Switch customNotification;
    View vibrateContainer;
    Switch vibrate;
    View lightContainer;
    Switch light;
    Button sound;

    class ValueHolder {
        int value = QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME;
        ValueHolder(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
    }

    ValueHolder customValue1 = new ValueHolder(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
    ValueHolder customValue2 = new ValueHolder(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
    ValueHolder customValue3 = new ValueHolder(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
    String ringtoneSelectedValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_widget_configuration_activity);
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
        customValue1Button = (Button) findViewById(R.id.custom_value_1_button);
        customValue2Button = (Button) findViewById(R.id.custom_value_2_button);
        customValue3Button = (Button) findViewById(R.id.custom_value_3_button);
        saveFAB = (FloatingActionButton) findViewById(R.id.save_and_close);
        customNotification = (Switch) findViewById(R.id.custom);
        vibrateContainer = findViewById(R.id.notification_vibrate);
        vibrate = (Switch) findViewById(R.id.vibrate);
        lightContainer = findViewById(R.id.notification_light);
        light = (Switch) findViewById(R.id.light);
        sound = (Button) findViewById(R.id.sound);

        hours.setText(Integer.toString(QuickReminderWidgetProvider.DEFAULT_HOURS));
        every30.setChecked(QuickReminderWidgetProvider.DEFAULT_EVERY_30);
        notes.setChecked(QuickReminderWidgetProvider.DEFAULT_POSSIBILITY_TO_ADD_NOTE);

        customNotification.setChecked(QuickReminderWidgetProvider.DEFAULT_CUSTOM_NOTIFICATION);
        vibrate.setChecked(QuickReminderWidgetProvider.DEFAULT_VIBRATE);
        light.setChecked(QuickReminderWidgetProvider.DEFAULT_LIGHT);
        customNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean newStatus) {
                calculateCustomButtons(newStatus);
            }
        });
        calculateCustomButtons(customNotification.isChecked());
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triggerRingtoneSelection();
            }
        });

        customValue1Button.setText(R.string.custom_values_5_label);
        customValue1.setValue(QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_1);
        customValue2Button.setText(R.string.custom_values_15_label);
        customValue2.setValue(QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_2);
        customValue3Button.setText(R.string.custom_values_disabled_label);
        customValue3.setValue(QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_3);
        showOrHideButtons();
        customValue1Button.setOnClickListener(new ValueButtonClickHandler(this, customValue1Button, customValue1));
        customValue2Button.setOnClickListener(new ValueButtonClickHandler(this, customValue2Button, customValue2));
        customValue3Button.setOnClickListener(new ValueButtonClickHandler(this, customValue3Button, customValue3));

        saveFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hoursInt = -1;
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

                QuickReminderWidgetProvider.getWidgetSharedPref(appWidgetId)
                        .edit()
                        .putInt(QuickReminderWidgetProvider.HOURS, hoursInt)
                        .putBoolean(QuickReminderWidgetProvider.EVERY_30, every30.isChecked())
                        .putBoolean(QuickReminderWidgetProvider.POSSIBILITY_TO_ADD_NOTE, notes.isChecked())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_1, customValue1.getValue())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_2, customValue2.getValue())
                        .putInt(QuickReminderWidgetProvider.CUSTOM_TIME_3, customValue3.getValue())
                        .putBoolean(QuickReminderWidgetProvider.CUSTOM_NOTIFICATION, customNotification.isChecked())
                        .putBoolean(QuickReminderWidgetProvider.CUSTOM_NOTIFICATION_VIBRATE, vibrate.isChecked())
                        .putBoolean(QuickReminderWidgetProvider.CUSTOM_NOTIFICATION_LIGHTS, light.isChecked())
                        .putString(QuickReminderWidgetProvider.CUSTOM_NOTIFICATION_SOUND, ringtoneSelectedValue)
                        .commit();

                // Update widget
                App.updateAllWidgets();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SOUND_REQUEST_CODE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone != null) {
                ringtoneSelectedValue = ringtone.toString();
            } else {
                // "Silent" was selected
                ringtoneSelectedValue = "";
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void calculateCustomButtons(boolean checked) {
        if (!checked) {
            vibrateContainer.setVisibility(View.GONE);
            lightContainer.setVisibility(View.GONE);
            sound.setVisibility(View.GONE);
        } else {
            vibrateContainer.setVisibility(View.VISIBLE);
            lightContainer.setVisibility(View.VISIBLE);
            sound.setVisibility(View.VISIBLE);
        }
    }

    private void triggerRingtoneSelection() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

        String existingValue = ringtoneSelectedValue;
        if (existingValue != null) {
            if (existingValue.length() == 0) {
                // Select "Silent"
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            } else {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
            }
        } else {
            // No ringtone has been selected, set to the default
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
        }

        startActivityForResult(intent, SOUND_REQUEST_CODE);
    }

    private void showOrHideButtons() {
        if (customValue1.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            if (customValue2.getValue() != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
                customValue1.setValue(customValue2.getValue());
                customValue1Button.setText(customValue2Button.getText());

                customValue2.setValue(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
                customValue2Button.setText(customValueCorrespondingNames[NONE_INDEX]);
            } else if (customValue3.getValue() != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
                customValue1.setValue(customValue3.getValue());
                customValue1Button.setText(customValue3Button.getText());

                customValue3.setValue(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
                customValue3Button.setText(customValueCorrespondingNames[NONE_INDEX]);
            }
        }

        if (customValue2.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            if (customValue3.getValue() != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
                customValue2.setValue(customValue3.getValue());
                customValue2Button.setText(customValue3Button.getText());

                customValue3.setValue(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
                customValue3Button.setText(customValueCorrespondingNames[NONE_INDEX]);
            }
        }

        if (customValue2.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME &&
                customValue3.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            customValue3Button.setVisibility(View.GONE);
        } else {
            customValue3Button.setVisibility(View.VISIBLE);
        }

        if (customValue1.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME &&
                customValue2.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            customValue2Button.setVisibility(View.GONE);
        } else {
            customValue2Button.setVisibility(View.VISIBLE);
        }
    }

    private class ValueChooserDialogHandler implements DialogInterface.OnClickListener {
        private final ValueHolder valueToChange;
        private final TextView textToChange;

        public ValueChooserDialogHandler(TextView textToChange, ValueHolder valueToChange) {
            this.valueToChange = valueToChange;
            this.textToChange = textToChange;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            textToChange.setText(customValueCorrespondingNames[which]);
            valueToChange.setValue(customValueCorrespondingValues[which]);

            showOrHideButtons();
        }
    }

    private class ValueButtonClickHandler implements View.OnClickListener {
        private final ValueHolder mValueToChange;
        private final TextView mTextToChange;
        private final Context mContext;

        public ValueButtonClickHandler(Context context, TextView textToChange, ValueHolder valueToChange) {
            mValueToChange = valueToChange;
            mTextToChange = textToChange;
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String[] names = new String[customValueCorrespondingNames.length];
            for (int i = 0; i < customValueCorrespondingNames.length; i++) {
                names[i] = getString(customValueCorrespondingNames[i]);
            }
            builder.setItems(names, new ValueChooserDialogHandler(mTextToChange, mValueToChange));
            builder.create().show();
        }
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
