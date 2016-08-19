package com.sickmartian.quickreminderwidget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.parceler.Parcels;

/**
 * Created by ***REMOVED*** on 8/14/16.
 */
public class ReminderEditionActivity extends AppCompatActivity {

    private static final String ALARM = "ALARM";
    private static final String JUST_CREATED = "JUST_CREATED";
    private boolean imDismissingIt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_edition_activity);

        final Alarm alarm = Parcels.unwrap(getIntent().getParcelableExtra(ALARM));

        CoordinatorLayout rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
        assert rootLayout != null;
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        boolean justCreated;
        if (savedInstanceState == null) {
            justCreated = getIntent().getBooleanExtra(JUST_CREATED, true);
        } else {
            justCreated = savedInstanceState.getBoolean(JUST_CREATED, false);
        }

        // If we are showing to the user that the alarm was created, prepare the snackbar
        if (justCreated) {
            findViewById(R.id.status_bar_space).setVisibility(
                    getResources().getBoolean(getResources().getIdentifier("config_showNavigationBar", "bool", "android"))
                            ? View.VISIBLE :View.GONE);

            final Snackbar snackbar = Snackbar.make(rootLayout,
                    String.format(getString(R.string.alarm_created_for), alarm.getDateTime().toString(QRWApp.dateTimeFormatter)),
                    Snackbar.LENGTH_SHORT);
            imDismissingIt = false;
            snackbar.setAction("Edit", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imDismissingIt = true;
                    snackbar.dismiss();

                    triggerEditionDialog(alarm);
                }
            });
            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    if (!imDismissingIt) {
                        finish();
                    }
                }
            });
            snackbar.show();
        } else {
            // If we are creating a new alarm or editing one that wasn't just created
            // we can go directly to the edition dialog
            triggerEditionDialog(alarm);
        }

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.base_layout);
        baseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void triggerEditionDialog(Alarm alarm) {
        ContextThemeWrapper wrappedContext = new ContextThemeWrapper(ReminderEditionActivity.this,
                R.style.AppTheme);
        final AlarmEditionDialog editionDialog = new AlarmEditionDialog(wrappedContext, alarm);
        editionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        editionDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public static Intent getIntentForEditionOfJustCreatedAlarm(Alarm alarm) {
        Intent intent = new Intent(QRWApp.getAppContext(), ReminderEditionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ReminderEditionActivity.ALARM, Parcels.wrap(alarm));
        intent.putExtra(JUST_CREATED, true);
        return intent;
    }

    public static Intent getIntentForCreation() {
        Intent intent = new Intent(QRWApp.getAppContext(), ReminderEditionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(JUST_CREATED, false);
        return intent;
    }

    public static Intent getIntentForEditionPart1(Context context) {
        Intent intent = new Intent(context, ReminderEditionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getIntentForEditionPart2(Alarm alarm) {
        Intent intent = new Intent();
        intent.putExtra(ReminderEditionActivity.ALARM, Parcels.wrap(alarm));
        intent.putExtra(JUST_CREATED, false);
        return intent;
    }

    public static class AlarmEditionDialog extends Dialog {
        private Alarm alarm;
        private EditText alarmNote;
        private DateFieldHandler alarmDateVH;
        private TimeFieldHandler alarmTimeVH;

        protected AlarmEditionDialog(@NonNull Context context, @NonNull Alarm alarm) {
            super(context);
            this.alarm = alarm;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.reminder_edition_dialog);
            setTitle(R.string.alarm_edition_title);

            alarmNote = (EditText) findViewById(R.id.alarm_note);
            Button save = (Button) findViewById(R.id.done_button);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isNew;
                    LocalDateTime oldAlarmDT = null;

                    // Check that the new alarm is not in the past
                    LocalDateTime newDateTime = alarmDateVH.getCurrentValue().toLocalDateTime(alarmTimeVH.getCurrentValue());
                    LocalDateTime now = Utils.getNow();
                    if (newDateTime.isBefore(now) ||
                            newDateTime.isEqual(now)) {
                        Toast.makeText(getContext(),
                                R.string.reminder_in_the_past,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // See if we have to create it
                    if (alarm == null) {
                        isNew = true;
                        alarm = Alarm.fromDateTime(newDateTime);
                    } else {
                        isNew = false;
                        // Or move it
                        if (!alarm.getDateTime().isEqual(newDateTime)) {
                            oldAlarmDT = alarm.getDateTime();
                            alarm.setDateTime(newDateTime);
                        }
                    }
                    // Set data
                    String newNote = alarmNote.getText().toString();
                    alarm.setNote(newNote.isEmpty() ? null : newNote);
                    // And save
                    new SaveAndUpdateWidgetAsyncTask(alarm, isNew, oldAlarmDT).execute();
                    dismiss();
                }
            });
            Button cancel = (Button) findViewById(R.id.cancel_button);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            Button delete = (Button) findViewById(R.id.delete_button);
            if (alarm != null) {
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DeleteAndUpdateWidgetAsyncTask(alarm).execute();
                        dismiss();
                    }
                });
            } else {
                delete.setVisibility(View.GONE);
            }

            TextView date = (TextView) findViewById(R.id.alarm_date);
            TextView time = (TextView) findViewById(R.id.alarm_time);

            LocalDateTime initialDateTime;
            if (alarm != null) {
                alarmNote.setText(alarm.getNote());
                initialDateTime = alarm.getDateTime();
            } else {
                initialDateTime = Utils.getNow().withSecondOfMinute(0).withMillisOfSecond(0);
            }

            alarmDateVH = new DateFieldHandler(getContext(), date, initialDateTime.toLocalDate());
            alarmTimeVH = new TimeFieldHandler(getContext(), time, initialDateTime.toLocalTime());
        }

    }

    private static class SaveAndUpdateWidgetAsyncTask extends AsyncTask<Void, Void, Void>{
        Alarm alarm;
        boolean isNew;
        LocalDateTime oldAlarmDT;

        public SaveAndUpdateWidgetAsyncTask(@NonNull Alarm alarm, boolean isNew, LocalDateTime oldAlarmDT) {
            this.alarm = alarm;
            this.isNew = isNew;
            this.oldAlarmDT = oldAlarmDT;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (isNew) {
                alarm.createSync();
                CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
            } else {
                if (oldAlarmDT == null) {
                    alarm.modifySync();
                } else {
                    alarm.deleteSync(oldAlarmDT);
                    alarm.createSync();
                    CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
                }
            }
            QRWApp.updateAllWidgets();
            return null;
        }
    }

    private static class DeleteAndUpdateWidgetAsyncTask extends AsyncTask<Void, Void, Void>{
        Alarm alarm;

        public DeleteAndUpdateWidgetAsyncTask(@NonNull Alarm alarm) {
            this.alarm = alarm;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            alarm.deleteSync();
            CalculateAndScheduleNextAlarmReceiver.sendBroadcast();
            QRWApp.updateAllWidgets();
            return null;
        }
    }

    private static class DateFieldHandler implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
        private LocalDate mCurrentValue;
        private final TextView mField;
        private final Context mContext;
        private boolean mIsDirty;
        private ChangeListener mChangeListener;

        public interface ChangeListener {
            void dateHasNewValue(DateFieldHandler dateFieldHandler, LocalDate newValue);
        }

        public DateFieldHandler(Context context, TextView field, LocalDate initialDate) {
            mContext = context;
            mField = field;
            setAndShowNewDate(mField, initialDate);
            mField.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            LocalDate localDate = getCurrentValue();
            DatePickerDialog dialog = Utils.getDatePickerDialog(mContext,
                    this,
                    localDate.getYear(), localDate.getMonthOfYear() - 1, localDate.getDayOfMonth());
            dialog.show();
        }

        public void setAndShowNewDate(TextView customStartDate, LocalDate localDate) {
            mCurrentValue = localDate;
            if (mChangeListener != null) {
                mChangeListener.dateHasNewValue(this, mCurrentValue);
            }
            customStartDate.setText(localDate.toString(DateTimeFormat.mediumDate()));
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            setAndShowNewDate(mField, new LocalDate(year, monthOfYear + 1, dayOfMonth));
            mIsDirty = true;
        }

        public boolean isDirty() {
            return mIsDirty;
        }

        public LocalDate getCurrentValue() {
            return mCurrentValue;
        }

        public void setChangeListener(ChangeListener mChangeListener) {
            this.mChangeListener = mChangeListener;
        }

    }

    private static class TimeFieldHandler implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {
        private LocalTime mCurrentValue;
        private final TextView mField;
        private final Context mContext;
        private boolean mIsDirty;

        public TimeFieldHandler(Context context, TextView field, LocalTime localTime) {
            mContext = context;
            mField = field;
            setAndShowNewDate(mField, localTime);
            mField.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            LocalTime localTime = getCurrentValue();
            Utils.getTimePickerDialog(mContext, this,
                    localTime.getHourOfDay(), localTime.getMinuteOfHour(),
                    android.text.format.DateFormat.is24HourFormat(mContext)).show();
        }

        public void setAndShowNewDate(TextView customStartDate, LocalTime localTime) {
            mCurrentValue = localTime;
            customStartDate.setText(localTime.toString(DateTimeFormat.shortTime()));
        }

        public boolean isDirty() {
            return mIsDirty;
        }

        public LocalTime getCurrentValue() {
            return mCurrentValue;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            setAndShowNewDate(mField, new LocalTime(hourOfDay, minute));
            mIsDirty = true;
        }
    }

}
