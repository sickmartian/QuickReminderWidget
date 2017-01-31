package com.sickmartian.quickreminderwidget;

import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.view.ContextThemeWrapper;
import android.widget.Toast;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.text.MessageFormat;

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

    public static String getRemindedCreatedForMessage(LocalDateTime dateTime) {
        int daysUntil = calculateDaysUntil(dateTime);
        return MessageFormat.format(App.getAppContext().getString(R.string.reminder_created_for),
                daysUntil, dateTime.toDate());
    }

    public static String getRemindedDeletedForMessage(LocalDateTime dateTime) {
        int daysUntil = calculateDaysUntil(dateTime);
        return MessageFormat.format(App.getAppContext().getString(R.string.reminder_deleted_for),
                daysUntil, dateTime.toDate());
    }

}
