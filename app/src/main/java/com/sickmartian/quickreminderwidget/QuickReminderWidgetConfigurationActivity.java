package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by ***REMOVED*** on 8/15/16.
 */
public class QuickReminderWidgetConfigurationActivity extends AppCompatActivity {
    private static final int NONE_INDEX = 0;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.quick_widget_configuration_activity);

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

        hours.setText(Integer.toString(QuickReminderWidgetProvider.DEFAULT_HOURS));
        every30.setChecked(QuickReminderWidgetProvider.DEFAULT_EVERY_30);
        notes.setChecked(QuickReminderWidgetProvider.DEFAULT_POSSIBILITY_TO_ADD_NOTE);

        changeText(customValue1Button, R.string.custom_values_5_label);
        customValue1.setValue(QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_1);
        changeText(customValue2Button, R.string.custom_values_15_label);
        customValue2.setValue(QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_2);
        changeText(customValue3Button, R.string.custom_values_disabled_label);
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
                        .commit();

                // Update widget
                QAWApp.updateAllWidgets();

                // Say the widget was created
                Intent resultValue = new Intent(QAWApp.getAppContext(), QuickReminderWidgetProvider.class);
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

    public void changeText(TextView textView, int textId) {
        if (textId == R.string.alarm_notification_note_label) {
            textView.setText(R.string.alarm_notification_note_label);
        } else {
            textView.setText(textId);
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
            changeText(textToChange, customValueCorrespondingNames[which]);
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
