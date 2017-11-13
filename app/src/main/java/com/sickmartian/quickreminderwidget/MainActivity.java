package com.sickmartian.quickreminderwidget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_1;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_2;
import static com.sickmartian.quickreminderwidget.QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_3;

/**
 * Created by sickmartian on 8/15/16.
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

    protected CustomAlarmTimeFragment customAlarmTimeFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (savedInstanceState == null) {
            SharedPreferences sharedPref = App.getSharedPreferences();
            customAlarmTimeFragment = CustomAlarmTimeFragment.getInstance(
                    sharedPref.getInt(App.SNOOZE_1, DEFAULT_CUSTOM_TIME_1),
                    sharedPref.getInt(App.SNOOZE_2, DEFAULT_CUSTOM_TIME_2),
                    sharedPref.getInt(App.SNOOZE_3, DEFAULT_CUSTOM_TIME_3)
            );
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
            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View view) {
                CustomAlarmTimeFragment.Times times = customAlarmTimeFragment.getTimes();

                App.getSharedPreferences()
                        .edit()
                        .putBoolean(App.CUSTOM_NOTIFICATION, customNotification.isChecked())
                        .putBoolean(App.CUSTOM_NOTIFICATION_VIBRATE, vibrate.isChecked())
                        .putBoolean(App.CUSTOM_NOTIFICATION_LIGHTS, light.isChecked())
                        .putInt(App.SNOOZE_1, times.getV1())
                        .putInt(App.SNOOZE_2, times.getV2())
                        .putInt(App.SNOOZE_3, times.getV3())
                        .putString(App.CUSTOM_NOTIFICATION_SOUND, ringtoneSelectedValue)
                        .commit();

                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        try {
            MenuItem menuItem;
            int i = 0;
            while (i < 100) {
                menuItem = menu.getItem(i);
                setMenuIconColor(this, menuItem, R.color.inactiveColor);
                i++;
            }
        } catch (IndexOutOfBoundsException ignored) {

        }

        return true;
    }

    public static void setMenuIconColor(Activity activity, MenuItem menuItem, int color) {
        if (activity != null) {
            Drawable newIcon = menuItem.getIcon();
            newIcon.mutate().setColorFilter(activity.getResources().getColor(color),
                    PorterDuff.Mode.SRC_IN);
            menuItem.setIcon(newIcon);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info_menu_item: {
                startActivity(InfoActivity.getIntentForShow());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
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

}
