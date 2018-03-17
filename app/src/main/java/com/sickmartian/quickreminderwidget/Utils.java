package com.sickmartian.quickreminderwidget;

import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.view.ContextThemeWrapper;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.IllegalInstantException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.text.MessageFormat;

import timber.log.Timber;

/**
 * Created by sickmartian on 8/12/16.
 */
public class Utils {
    public static LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public static int getAppSmallIcon() {
        int smallIcon = R.drawable.ic_launcher;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            smallIcon = R.drawable.icon_outline;
        }
        return smallIcon;
    }

    private static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }

    private static boolean isBrokenSamsungDevice() {
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && isBetweenAndroidVersions(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1));
    }

    public static DatePickerDialog getDatePickerDialog(Context context, DatePickerDialog.OnDateSetListener onDateSetListener, int year, int month, int day) {
        if (isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog);
            return new DatePickerDialog(context,
                    onDateSetListener,
                    year, month, day);
        } else {
            return new DatePickerDialog(context,
                    onDateSetListener,
                    year, month, day);
        }
    }

    public static TimePickerDialog getTimePickerDialog(Context context,
                                                       TimePickerDialog.OnTimeSetListener onTimeSetListener,
                                                       int hour, int minute, boolean is24HourFormat) {
        return new TimePickerDialog(context,
                onTimeSetListener,
                hour, minute,
                is24HourFormat);
    }

    public static void toastTo(final String toastMessage) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getAppContext(),
                        toastMessage,
                        Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    public static PendingIntent getPIInNewStack(Intent intent, int requestCode) {
        return getPIInNewStack(intent, requestCode, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static PendingIntent getPIInNewStack(Intent intent, int requestCode, int flags) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getAppContext());
        stackBuilder.addParentStack(ReminderEditionActivity.class);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(requestCode, flags);
    }

    public static int calculateDaysUntil(LocalDateTime until) {
        return new Duration(
                LocalDate.now().toDateTime(LocalTime.MIDNIGHT, DateTimeZone.UTC).getMillis(),
                until.withTime(0, 0, 0, 0).toDateTime(DateTimeZone.UTC).getMillis())
                    .toStandardDays().getDays();
    }

    public static String getFormattedMessageForDate(LocalDateTime localDateTime,
                                                    @StringRes int stringRed) {
        int daysUntil = calculateDaysUntil(localDateTime);
        return MessageFormat.format(App.getAppContext().getString(stringRed),
                daysUntil, localDateTime.toDate());
    }

    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final int NOTIFICATION_ID_INITIAL_VALUE = -1;
    public static int getSharedUniqueId() {
        int notificationId = App.getSharedPreferences().getInt(NOTIFICATION_ID, NOTIFICATION_ID_INITIAL_VALUE);
        if (notificationId == Integer.MAX_VALUE) {
            notificationId = NOTIFICATION_ID_INITIAL_VALUE;
        }
        App.getSharedPreferences().edit().putInt(NOTIFICATION_ID, ++notificationId).commit();
        return notificationId;
    }

    /**
     * Adds duration to 'now' and sets the seconds and millis to 0.
     * If the duration falls in a DST gap, it moves it until it's out of the gap in 30' intervals
     * so:
     * If we are on 01:34AM and the plus is of 30 minutes with the gap at 1AM of 1 hour
     * it's moved from 02:34AM to 02:04AM
     * If we are on 12:30AM and the plus is of 30 minutes with the gap at 1AM of 30 minutes
     * it's moved from 01:00AM to 01:30AM
     * @param duration duration to add to now
     * @return safely adjusted local date time for alarms
     */
    public static LocalDateTime saneNowPlusDurationForAlarms(Duration duration) {
        LocalDateTime proposedLDT = Utils.getNow().plus(duration)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        while (DateTimeZone.getDefault().isLocalDateTimeGap(proposedLDT)) {
            proposedLDT = proposedLDT.plusMinutes(30);
        }

        return proposedLDT;
    }

    /**
     * Convert a Local DT to a DT, if the Local DT is invalid because is on a DST gap
     * move it to the next valid time
     * @param localDateTime local date time
     * @return UTC instant that corresponds to the local date time
     */
    public static DateTime convertLocalToDTSafely(LocalDateTime localDateTime) {
        DateTime convertedDT = null;
        try {
            convertedDT = localDateTime.toDateTime();
        } catch (IllegalInstantException e) {
            boolean failed = true;
            int iteration = 1;
            while (failed) {
                try {
                    convertedDT = localDateTime.plusMinutes(30 * iteration).toDateTime();
                    failed = false;
                } catch (IllegalInstantException e2) {
                    iteration++;
                }
            }

            Timber.i("Converted date from "+ localDateTime + " to "
                    + convertedDT + " because of DST gap");
        }

        return convertedDT;
    }
}
