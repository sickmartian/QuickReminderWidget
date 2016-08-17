package com.sickmartian.quickreminderwidget;

import android.app.Dialog;
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
import android.widget.EditText;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.parceler.Parcels;

/**
 * Created by ***REMOVED*** on 8/14/16.
 */
public class ReminderEditionActivity extends AppCompatActivity {

    private static final String ALARM = "ALARM";
    private boolean imDismissingIt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_edition_activity);

        final Alarm alarm = Parcels.unwrap(getIntent().getParcelableExtra(ALARM));
        findViewById(R.id.status_bar_space).setVisibility(
                getResources().getBoolean(getResources().getIdentifier("config_showNavigationBar", "bool", "android"))
                        ? View.VISIBLE :View.GONE);

        CoordinatorLayout rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
        assert rootLayout != null;
        final Snackbar snackbar = Snackbar.make(rootLayout,
                String.format(getString(R.string.alarm_created_for), alarm.getDateTime().toString(QRWApp.dateTimeFormatter)),
                Snackbar.LENGTH_SHORT);
        imDismissingIt = false;
        snackbar.setAction("Edit", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imDismissingIt = true;
                        snackbar.dismiss();

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

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public static Intent getIntentForEdition(Alarm alarm) {
        Intent intent = new Intent(QRWApp.getAppContext(), ReminderEditionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ReminderEditionActivity.ALARM, Parcels.wrap(alarm));
        return intent;
    }

    public static class AlarmEditionDialog extends Dialog {
        private Alarm alarm;
        private EditText alarmNote;
        private Button save;
        private Button cancel;

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
            save = (Button) findViewById(R.id.done_button);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alarm.setNote(alarmNote.getText().toString());
                    new SaveAndUpdateWidgetAsyncTask(alarm).execute();
                    dismiss();
                }
            });
            cancel = (Button) findViewById(R.id.cancel_button);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            alarmNote.setText(alarm.getNote());
        }

        public void updateAlarm() {
            this.alarm.setNote(alarmNote.toString().trim());
        }

    }

    private static class SaveAndUpdateWidgetAsyncTask extends AsyncTask<Void, Void, Void>{
        Alarm alarm;

        public SaveAndUpdateWidgetAsyncTask(@NonNull Alarm alarm) {
            this.alarm = alarm;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            alarm.modifySync();
            QRWApp.updateAllWidgets();
            return null;
        }
    }

}
