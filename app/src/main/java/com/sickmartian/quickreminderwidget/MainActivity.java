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
public class MainActivity extends AppCompatActivity {
    private static final int SOUND_REQUEST_CODE = 3443;

    Toolbar toolbar;
    FloatingActionButton saveFAB;
    Switch customNotification;
    View vibrateContainer;
    Switch vibrate;
    View lightContainer;
    Switch light;
    Button sound;

    String ringtoneSelectedValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
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

        saveFAB = (FloatingActionButton) findViewById(R.id.save_and_close);
        customNotification = (Switch) findViewById(R.id.custom);
        vibrateContainer = findViewById(R.id.notification_vibrate);
        vibrate = (Switch) findViewById(R.id.vibrate);
        lightContainer = findViewById(R.id.notification_light);
        light = (Switch) findViewById(R.id.light);
        sound = (Button) findViewById(R.id.sound);

        customNotification.setChecked(App.getSharedPreferences().getBoolean(App.CUSTOM_NOTIFICATION,
                App.DEFAULT_CUSTOM_NOTIFICATION));
        vibrate.setChecked(App.getSharedPreferences().getBoolean(App.CUSTOM_NOTIFICATION_VIBRATE,
                App.DEFAULT_VIBRATE));
        light.setChecked(App.getSharedPreferences().getBoolean(App.CUSTOM_NOTIFICATION_LIGHTS,
                App.DEFAULT_LIGHT));
        customNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean newStatus) {
                calculateCustomButtons(newStatus);
            }
        });
        calculateCustomButtons(customNotification.isChecked());
        ringtoneSelectedValue = App.getSharedPreferences().getString(App.CUSTOM_NOTIFICATION_SOUND, null);
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triggerRingtoneSelection();
            }
        });

        saveFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getSharedPreferences()
                        .edit()
                        .putBoolean(App.CUSTOM_NOTIFICATION, customNotification.isChecked())
                        .putBoolean(App.CUSTOM_NOTIFICATION_VIBRATE, vibrate.isChecked())
                        .putBoolean(App.CUSTOM_NOTIFICATION_LIGHTS, light.isChecked())
                        .putString(App.CUSTOM_NOTIFICATION_SOUND, ringtoneSelectedValue)
                        .commit();

                finish();
            }
        });
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
